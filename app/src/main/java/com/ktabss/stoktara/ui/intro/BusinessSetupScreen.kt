package com.ktabss.stoktara.ui.intro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ktabss.stoktara.ui.theme.Ink
import com.ktabss.stoktara.ui.theme.Paper
import com.ktabss.stoktara.ui.theme.Primary

private val SetupFont = FontFamily.SansSerif

@Composable
fun BusinessSetupScreen(
    onSetupComplete: (businessName: String, ownerName: String, pin: String) -> Unit
) {
    var businessName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val isFormValid = businessName.isNotBlank() && ownerName.isNotBlank() && pinCode.length >= 4

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to StokTara",
            fontFamily = SetupFont,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = Ink
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Set up your business to get started.",
            fontFamily = SetupFont,
            fontSize = 14.sp,
            color = Ink.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = businessName,
            onValueChange = { businessName = it },
            label = { Text("Business Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                focusedLabelColor = Primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = ownerName,
            onValueChange = { ownerName = it },
            label = { Text("Owner / Admin Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                focusedLabelColor = Primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = pinCode,
            onValueChange = { if (it.length <= 6) pinCode = it },
            label = { Text("Admin PIN (4-6 digits)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                focusedLabelColor = Primary
            )
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (isFormValid) Primary else Primary.copy(alpha = 0.4f))
                .clickable(enabled = isFormValid) {
                    if (isFormValid) {
                        onSetupComplete(businessName.trim(), ownerName.trim(), pinCode.trim())
                    } else {
                        errorMessage = "Please fill in all fields correctly."
                    }
                }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Complete Setup",
                fontFamily = SetupFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}