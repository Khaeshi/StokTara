package com.ktabss.stoktara.ui.placeholder

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ktabss.stoktara.data.Role
import com.ktabss.stoktara.ui.components.ScreenHeader
import com.ktabss.stoktara.ui.intro.BusinessProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(
    title: String = "Business Profile",
    owner: String = "Member 1",
    hint: String = "",
    onBack: (() -> Unit)? = null,
    viewModel: BusinessProfileViewModel = viewModel()
) {
    val businessName by viewModel.businessName.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()
    val profiles by viewModel.profiles.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var editedBusinessName by remember(businessName) { mutableStateOf(businessName) }
    var nickname by remember(currentProfile) { mutableStateOf(currentProfile.nickname) }
    var pin by remember(currentProfile) { mutableStateOf(currentProfile.password) }

    var newCategoryText by remember { mutableStateOf("") }
    var newStaffName by remember { mutableStateOf("") }
    var newStaffPin by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title, onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- EDIT OWN ACCOUNT (OWNER & STAFF) ---
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Your Credentials (${currentProfile.role})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = { Text("Nickname") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it },
                        label = { Text("PIN / Password") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            viewModel.updateProfile(currentProfile.id, nickname, pin)
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Save Credentials")
                    }
                }
            }

            // --- OWNER ONLY CONTROLS ---
            if (currentProfile.role == Role.OWNER) {
                // 1. Edit Business Name
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Business Info", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = editedBusinessName,
                            onValueChange = { editedBusinessName = it },
                            label = { Text("Business Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = { viewModel.updateBusinessName(editedBusinessName) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Update Business Name")
                        }
                    }
                }

                // 2. Category Tag CRUD
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Category Tags", style = MaterialTheme.typography.titleMedium)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newCategoryText,
                                onValueChange = { newCategoryText = it },
                                label = { Text("New Category") },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                if (newCategoryText.isNotBlank()) {
                                    viewModel.addCategory(newCategoryText)
                                    newCategoryText = ""
                                }
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Category")
                            }
                        }

                        categories.forEach { tag ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tag.label)
                                IconButton(onClick = { viewModel.deleteCategory(tag.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Category")
                                }
                            }
                        }
                    }
                }

                // 3. Add New Staff
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Staff Management", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = newStaffName,
                            onValueChange = { newStaffName = it },
                            label = { Text("Staff Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newStaffPin,
                            onValueChange = { newStaffPin = it },
                            label = { Text("Staff PIN") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                if (newStaffName.isNotBlank() && newStaffPin.isNotBlank()) {
                                    viewModel.addStaff(newStaffName, newStaffPin)
                                    newStaffName = ""
                                    newStaffPin = ""
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Add Staff Account")
                        }

                        Text("Existing Staff Profiles:", style = MaterialTheme.typography.bodyMedium)
                        profiles.filter { it.role == Role.STAFF }.forEach { staff ->
                            Text("• ${staff.nickname}")
                        }
                    }
                }
            }
        }
    }
}