package com.ktabss.stoktara.data

import kotlinx.coroutines.flow.StateFlow

/**
 * The single data contract every screen builds against.
 */
interface InventoryRepository {
    // --- Member 1 will own/replace these two (profiles + business settings) ---
    val businessName: StateFlow<String>
    val currentProfile: StateFlow<Profile>
    val categories: StateFlow<List<CategoryTag>>
    val profiles: StateFlow<List<Profile>>

    fun switchProfile(profileId: Long)
    fun setBusinessName(name: String)
    fun createOwnerProfile(name: String, pin: String): Profile

    fun updateProfile(id: Long, newNickname: String, newPin: String)
    fun createStaffProfile(name: String, pin: String): Profile
    fun addCategoryTag(label: String): CategoryTag
    fun deleteCategoryTag(id: Long)

    val items: StateFlow<List<Item>>
    val auditLog: StateFlow<List<AuditLog>>

    fun findItem(id: Long): Item?
    fun findItemBySku(sku: String): Item?
    suspend fun addItem(draft: NewItem, actor: Profile): Item
    suspend fun updateItem(updated: Item, actor: Profile): Item
    suspend fun deleteItem(id: Long, actor: Profile): Boolean
    suspend fun recordStockChange(itemId: Long, delta: Double, actor: Profile): Item?
    suspend fun getItem(itemId: Long): Item?
}
