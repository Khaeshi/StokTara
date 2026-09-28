package com.ktabss.stoktara.data

import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryInventoryRepository : InventoryRepository {
    private val nextItemId = AtomicLong(1)
    private val nextLogId = AtomicLong(1)

    private val nena = Profile(1, "Nena", Role.OWNER)
    private val jun = Profile(2, "Jun", Role.STAFF)

    private val _businessName = MutableStateFlow("Nena\u2019s Bakery & Supplies")
    private val _currentProfile = MutableStateFlow(nena)
    private val _categories = MutableStateFlow(
        listOf(
            CategoryTag(1, "Baking Staples"),
            CategoryTag(2, "Dairy & Eggs"),
            CategoryTag(3, "Packaging"),
            CategoryTag(4, "Beverages"),
        ),
    )
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    private val _auditLog = MutableStateFlow<List<AuditLog>>(emptyList())

    override val businessName: StateFlow<String> = _businessName.asStateFlow()
    override val currentProfile: StateFlow<Profile> = _currentProfile.asStateFlow()
    override val categories: StateFlow<List<CategoryTag>> = _categories.asStateFlow()
    override val items: StateFlow<List<Item>> = _items.asStateFlow()
    override val auditLog: StateFlow<List<AuditLog>> = _auditLog.asStateFlow()

    init { seed() }

    override fun findItem(id: Long): Item? = _items.value.firstOrNull { it.id == id }

    override fun findItemBySku(sku: String): Item? =
        _items.value.firstOrNull { it.sku.equals(sku.trim(), ignoreCase = true) }

    override suspend fun addItem(draft: NewItem, actor: Profile): Item {
        val item = Item(
            id = nextItemId.getAndIncrement(),
            name = draft.name.trim(),
            categoryTagId = draft.categoryTagId,
            unit = draft.unit,
            quantity = draft.quantity,
            threshold = draft.threshold,
            sku = draft.sku,
            photoUri = draft.photoUri,
            dateAdded = System.currentTimeMillis(),
        )
        _items.update { it + item }
        append(item, AuditAction.ITEM_CREATED, draft.quantity, actor)
        return item
    }

    override suspend fun updateItem(updated: Item, actor: Profile): Item {
        val old = findItem(updated.id) ?: return updated
        if (old == updated) return old
        _items.update { list -> list.map { if (it.id == updated.id) updated else it } }
        val qtyDelta = updated.quantity - old.quantity
        if (qtyDelta != 0.0) append(updated, AuditAction.MANUAL_ADJUSTMENT, qtyDelta, actor)
        if (old.copy(quantity = updated.quantity) != updated) append(updated, AuditAction.DETAILS_UPDATED, null, actor)
        return updated
    }

    override suspend fun deleteItem(id: Long, actor: Profile): Boolean {
        val item = findItem(id) ?: return false
        _items.update { list -> list.filterNot { it.id == id } }
        append(item, AuditAction.ITEM_DELETED, null, actor)
        return true
    }

    override suspend fun recordStockChange(itemId: Long, delta: Double, actor: Profile): Item? {
        val item = findItem(itemId) ?: return null
        val newQty = (item.quantity + delta).coerceAtLeast(0.0)
        val applied = newQty - item.quantity
        if (applied == 0.0) return item
        val updated = item.copy(quantity = newQty)
        _items.update { list -> list.map { if (it.id == itemId) updated else it } }
        append(updated, if (applied > 0) AuditAction.STOCK_IN else AuditAction.STOCK_OUT, applied, actor)
        return updated
    }

    private fun append(item: Item, action: AuditAction, delta: Double?, actor: Profile, at: Long = System.currentTimeMillis()) {
        val entry = makeLog(item, action, delta, actor, at)
        _auditLog.update { listOf(entry) + it }
    }

    private fun makeLog(item: Item, action: AuditAction, delta: Double?, actor: Profile, at: Long) = AuditLog(
        id = nextLogId.getAndIncrement(),
        itemId = item.id,
        itemName = item.name,
        unit = item.unit,
        action = action,
        delta = delta,
        actorName = actor.nickname,
        actorRole = actor.role,
        timestamp = at,
    )

    /**
     * Demo data matching the Figma (8 items, 3 low, 1 out, 13 logged moves).
     * Ledger is coherent: each item's quantity equals the sum of its logged deltas.
     * Timestamps are relative to first launch so the "past 6 months" filter never empties the demo.
     */
    private fun seed() {
        val now = System.currentTimeMillis()
        val day = 24 * 60 * 60 * 1000L
        val newItems = mutableListOf<Item>()
        val newLogs = mutableListOf<AuditLog>()
        var seq = 0L

        fun add(
            name: String, tag: Long, unit: String, created: Double, threshold: Double, sku: String,
            vararg moves: Pair<Double, Int>, // (delta, daysAgo)
        ) {
            val createdAt = now - 30 * day - (seq++) * 3_600_000L
            var item = Item(nextItemId.getAndIncrement(), name, tag, unit, created, threshold, sku, null, createdAt)
            newLogs += makeLog(item, AuditAction.ITEM_CREATED, created, nena, createdAt)
            moves.forEachIndexed { i, (delta, daysAgo) ->
                item = item.copy(quantity = item.quantity + delta)
                val actor = if (i % 2 == 0) jun else nena
                val action = if (delta > 0) AuditAction.STOCK_IN else AuditAction.STOCK_OUT
                newLogs += makeLog(item, action, delta, actor, now - daysAgo * day - (seq++) * 3_600_000L)
            }
            newItems += item
        }

        add("All-Purpose Flour", 1, "kg", 20.0, 20.0, "BKY-0001", 25.0 to 26)
        add("White Sugar", 1, "kg", 10.0, 5.0, "BKY-0002", -9.5 to 12)
        add("Almond Milk", 2, "pc", 12.0, 6.0, "BKY-0003", -10.0 to 8)
        add("Eggs (Tray)", 2, "pc", 30.0, 5.0, "BKY-0004", -30.0 to 3)
        add("Instant Yeast", 1, "pack", 10.0, 10.0, "BKY-0005", -5.0 to 5)
        add("Bakery Boxes (Small)", 3, "pc", 120.0, 50.0, "BKY-0006")
        add("Unsalted Butter", 2, "kg", 12.0, 8.0, "BKY-0007")
        add("Bottled Water 500mL", 4, "pc", 60.0, 24.0, "BKY-0008")

        _items.value = newItems
        _auditLog.value = newLogs.sortedByDescending { it.timestamp }
    }
}
