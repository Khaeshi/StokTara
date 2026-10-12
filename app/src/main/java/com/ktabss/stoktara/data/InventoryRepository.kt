package com.ktabss.stoktara.data

import kotlinx.coroutines.flow.StateFlow

/**
 * The single data contract every screen builds against.
 * Today: InMemoryInventoryRepository. Later: a Room-backed implementation with the same interface,
 * so no screen code changes.
 *
 * RULE: every write that changes an item goes through this interface, and every write appends
 * exactly the audit entries it should. Screens never touch the lists directly.
 */
interface InventoryRepository {
    // --- Member 1 will own/replace these two (profiles + business settings) ---
    val businessName: StateFlow<String>
    val currentProfile: StateFlow<Profile>
    val categories: StateFlow<List<CategoryTag>>
    val profiles: StateFlow<List<Profile>>

    /** Who is "touching" the stock right now; audit entries are attributed to this profile. */
    fun switchProfile(profileId: Long)

    val items: StateFlow<List<Item>>

    /** Newest first. Append-only: nothing in the app edits or deletes entries. */
    val auditLog: StateFlow<List<AuditLog>>

    fun findItem(id: Long): Item?
    fun findItemBySku(sku: String): Item?

    /** Writes 1 audit entry: ITEM_CREATED. */
    suspend fun addItem(draft: NewItem, actor: Profile): Item

    /**
     * Full edit (Edit Item modal). Quantity changed -> MANUAL_ADJUSTMENT entry;
     * any other field changed -> DETAILS_UPDATED entry; nothing changed -> no entry.
     */
    suspend fun updateItem(updated: Item, actor: Profile): Item

    /** Removes the item, keeps its history, writes ITEM_DELETED. */
    suspend fun deleteItem(id: Long, actor: Profile): Boolean

    /**
     * THE shared stock in/out function. Quick Stock overlay, Scan tab and continuous scan mode
     * must all call this (positive delta = stock in, negative = stock out). Quantity never goes below 0.
     * Returns the updated item, or null if the id doesn't exist.
     */
    suspend fun recordStockChange(itemId: Long, delta: Double, actor: Profile): Item?
    suspend fun getItem(itemId: Long): Item?
}
