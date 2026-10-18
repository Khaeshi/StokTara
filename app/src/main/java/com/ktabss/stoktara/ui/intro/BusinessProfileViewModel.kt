package com.ktabss.stoktara.ui.intro

import androidx.lifecycle.ViewModel
import com.ktabss.stoktara.data.AppGraph
import com.ktabss.stoktara.data.CategoryTag
import com.ktabss.stoktara.data.InventoryRepository
import com.ktabss.stoktara.data.Profile
import kotlinx.coroutines.flow.StateFlow

class BusinessProfileViewModel(
    private val repo: InventoryRepository = AppGraph.repository
) : ViewModel() {

    val businessName: StateFlow<String> = repo.businessName
    val currentProfile: StateFlow<Profile> = repo.currentProfile
    val profiles: StateFlow<List<Profile>> = repo.profiles
    val categories: StateFlow<List<CategoryTag>> = repo.categories

    fun updateBusinessName(newName: String) = repo.setBusinessName(newName)

    fun updateProfile(id: Long, nickname: String, pin: String) {
        repo.updateProfile(id, nickname, pin)
    }

    fun addStaff(name: String, pin: String) {
        repo.createStaffProfile(name, pin)
    }

    fun addCategory(label: String) {
        if (label.isNotBlank()) repo.addCategoryTag(label.trim())
    }

    fun deleteCategory(id: Long) {
        repo.deleteCategoryTag(id)
    }
}