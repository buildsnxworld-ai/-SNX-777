package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AppLanguage
import com.example.ui.theme.*
import com.example.util.StringRes

@Composable
fun AuthModal(
    isOpen: Boolean,
    initialTab: Int,
    language: AppLanguage,
    canDismiss: Boolean = false,
    onDismiss: () -> Unit,
    onLogin: (String, String) -> Pair<Boolean, String?>,
    onRegister: (String, String, String, String) -> Pair<Boolean, String?>
) {
    if (!isOpen) return

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val safeDismiss = {
        focusManager.clearFocus()
        keyboardController?.hide()
        onDismiss()
    }

    var currentTab by remember(initialTab) { mutableStateOf(initialTab) } // 0: Login, 1: Register

    // Form states
    var phoneInput by remember { mutableStateOf("") }
    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = safeDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            decorFitsSystemWindows = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .imePadding()
                .testTag("auth_dialog_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(GoldPrimary.copy(alpha = 0.5f), CasinoBorderSubtle)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button (Dismissable as requested)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SNX",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Slate100
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "777",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = GoldPrimary
                        )
                    }

                    IconButton(
                        onClick = safeDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_auth_modal")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Slate400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Switcher (Login / Register)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CasinoSurface)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (currentTab == 0) GoldPrimary else Color.Transparent)
                            .clickable {
                                currentTab = 0
                                errorMessage = null
                            }
                            .padding(vertical = 10.dp)
                            .testTag("auth_tab_login"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = StringRes.t(language, "লগইন", "Login"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (currentTab == 0) Color.Black else Slate400
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (currentTab == 1) GoldPrimary else Color.Transparent)
                            .clickable {
                                currentTab = 1
                                errorMessage = null
                            }
                            .padding(vertical = 10.dp)
                            .testTag("auth_tab_register"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = StringRes.t(language, "রেজিস্ট্রেশন", "Register"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (currentTab == 1) Color.Black else Slate400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error alert
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = AccentCrimson,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Inputs
                if (currentTab == 1) {
                    // Registration Username
                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { 
                            usernameInput = it
                            errorMessage = null
                        },
                        label = { Text(StringRes.t(language, "ব্যবহারকারীর নাম", "Username")) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GoldLight) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_register_username"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = CasinoBorderSubtle,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Phone Input (Used for Login and Registration)
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { 
                        phoneInput = it
                        errorMessage = null
                    },
                    label = { 
                        Text(StringRes.t(language, "মোবাইল নম্বর", "Mobile Number"))
                    },
                    placeholder = { Text("01XXXXXXXXX", color = Slate400, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = GoldLight) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_auth_phone"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = CasinoBorderSubtle,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Password Input
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { 
                        passwordInput = it
                        errorMessage = null
                    },
                    label = { Text(StringRes.t(language, "পাসওয়ার্ড", "Password")) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GoldLight) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Password",
                                tint = Slate400
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_auth_password"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = CasinoBorderSubtle,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    )
                )

                if (currentTab == 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                    // Confirm Password
                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = { 
                            confirmPasswordInput = it
                            errorMessage = null
                        },
                        label = { Text(StringRes.t(language, "পাসওয়ার্ড নিশ্চিত করুন", "Confirm Password")) },
                        leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = GoldLight) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_register_confirm_password"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = CasinoBorderSubtle,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (currentTab == 0) {
                            if (phoneInput.isBlank() || passwordInput.isBlank()) {
                                errorMessage = StringRes.t(language, "মোবাইল নম্বর ও পাসওয়ার্ড দিন", "Please enter phone & password")
                            } else {
                                val res = onLogin(phoneInput, passwordInput)
                                if (!res.first) {
                                    errorMessage = res.second
                                } else {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                            }
                        } else {
                            val cleanPhone = phoneInput.trim()
                            if (cleanPhone.isBlank() || cleanPhone.length < 10) {
                                errorMessage = StringRes.t(language, "সঠিক মোবাইল নম্বর প্রদান করুন", "Please enter a valid mobile number")
                            } else if (passwordInput.isBlank() || passwordInput.length < 4) {
                                errorMessage = StringRes.t(language, "কমপক্ষে ৪ অক্ষরের পাসওয়ার্ড দিন", "Password must be at least 4 characters")
                            } else if (confirmPasswordInput.isNotBlank() && passwordInput != confirmPasswordInput) {
                                errorMessage = StringRes.t(language, "পাসওয়ার্ড দুটি মেলেনি", "Passwords do not match")
                            } else {
                                val res = onRegister(usernameInput, cleanPhone, passwordInput, "")
                                if (!res.first) {
                                    errorMessage = res.second
                                } else {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_submit_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = if (currentTab == 0)
                            StringRes.t(language, "লগইন করুন", "Login Now")
                        else
                            StringRes.t(language, "রেজিস্ট্রেশন করুন", "Register Now"),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
