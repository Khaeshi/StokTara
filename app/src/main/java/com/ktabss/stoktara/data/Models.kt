package com.ktabss.stoktara.data

enum class Role(val label: String) { OWNER("Owner"), STAFF("Staff") }

data class Profile(
    val id: Long,
    val nickname: String,
    val role: Role,
    val password: String = ""
)

data class CategoryTag(val id: Long, val label: String)

enum class StockStatus { OK, LOW, OUT }

data class Item(
    val id: Long,
    val name: String,
    val categoryTagId: Long?,
    val unit: String,
    val quantity: Double,
    val threshold: Double,
    val sku: String? = null,
    val photoUri: String? = null,
    val dateAdded: Long,
) {
    val status: StockStatus
        get() = when {
            quantity <= 0.0 -> StockStatus.OUT
            quantity <= threshold -> StockStatus.LOW
            else -> StockStatus.OK
        }

    /** 0.0 = empty, 1.0 = exactly at threshold. Used to sort "worst first". */
    val fillRatio: Double get() = if (threshold > 0) quantity / threshold else 0.0
}

/** Items at/below threshold, Out of stock first, then the emptiest relative to their threshold. */
fun List<Item>.needsRestocking(): List<Item> =
    filter { it.status != StockStatus.OK }
        .sortedWith(compareBy<Item>({ it.status != StockStatus.OUT }, { it.fillRatio }))

/** Everything the Add Item form collects (Member 2). */
data class NewItem(
    val name: String,
    val categoryTagId: Long?,
    val unit: String,
    val quantity: Double,
    val threshold: Double,
    val sku: String? = null,
    val photoUri: String? = null,
)

data class ItemFormErrors(
    val name: String? = null,
    val unit: String? = null,
    val quantity: String? = null,
    val threshold: String? = null,
    val sku: String? = null
)

enum class AuditAction(val label: String) {
    ITEM_CREATED("Item Created"),
    STOCK_IN("Stock In"),
    STOCK_OUT("Stock Out"),
    MANUAL_ADJUSTMENT("Manual Adjustment"),
    DETAILS_UPDATED("Details Updated"),
    ITEM_DELETED("Item Deleted"),
}

/**
 * One row of the append-only audit trail. Item and actor fields are snapshots on purpose:
 * history must still read correctly after an item is renamed/deleted or a profile is removed.
 */
data class AuditLog(
    val id: Long,
    val itemId: Long?,
    val itemName: String,
    val unit: String,
    val action: AuditAction,
    val delta: Double?,
    val actorName: String,
    val actorRole: Role,
    val timestamp: Long,
) {
    val actorLabel: String get() = "$actorName (${actorRole.label})"
}
