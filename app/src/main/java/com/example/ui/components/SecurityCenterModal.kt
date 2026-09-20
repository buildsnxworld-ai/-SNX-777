package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AppLanguage
import com.example.model.UserProfile
import com.example.ui.theme.*
import com.example.util.StringRes

@Composable
fun SecurityCenterModal(
    isOpen: Boolean,
    userProfile: UserProfile,
    language: AppLanguage,
    onUpdatePhone: (newPhone: String, pass: String) -> Pair<Boolean, String>,
    onUpdatePassword: (oldPass: String, newPass: String) -> Pair<Boolean, String>,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Change Phone, 1: Change Password

    // Phone state
    var newPhoneInput by remember { mutableStateOf("") }
    var phoneVerifyPassInput by remember { mutableStateOf("") }
    var isPhonePassVisible by remember { mutableStateOf(false) }
    var phoneStatusMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    // Password state
    var oldPassInput by remember { mutableStateOf("") }
    var newPassInput by remember { mutableStateOf("") }
    var confirmPassInput by remember { mutableStateOf("") }
    var isOldPassVisible by remember { mutableStateOf(false) }
    var isNewPassVisible by remember { mutableStateOf(false) }
    var isConfirmPassVisible by remember { mutableStateOf(false) }
    var passStatusMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    val safeDismiss = {
        focusManager.clearFocus()
        keyboardController?.hide()
        onDismiss()
    }

    Dialog(
        onDismissRequest = safeDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .testTag("security_center_modal"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CasinoSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(GoldPrimary, AccentEmerald, GoldLight))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header with Security Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(listOf(GoldPrimary.copy(alpha = 0.3f), Slate800))
                                )
                                .border(1.5.dp, GoldPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🛡️", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = StringRes.t(language, "সিকিউরিটি সেন্টার", "Security Center"),
                                color = GoldLight,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp
                            )
                            Text(
                                text = StringRes.t(language, "অ্যাকাউন্ট নিরাপত্তা ও পাসওয়ার্ড ব্যবস্থাপনা", "Account Security & Credentials"),
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = safeDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("close_security_center")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current Profile Info Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Slate800,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(CasinoBorderSubtle, CasinoBorderSubtle))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = StringRes.t(language, "ব্যবহারকারী:", "Account:"),
                                color = Slate400,
                                fontSize = 11.sp
                            )
                            Text(
                                text = userProfile.username.ifBlank { "Player" },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = StringRes.t(language, "বর্তমান মোবাইল:", "Current Mobile:"),
                                color = Slate400,
                                fontSize = 11.sp
                            )
                            Text(
                                text = if (userProfile.phone.isNotBlank()) userProfile.phone else "01XXXXXXXXX",
                                color = GoldLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate900)
                        .padding(4.dp)
                ) {
                    // Tab 0: Phone Change
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == 0) GoldPrimary else Color.Transparent)
                            .clickable {
                                selectedTab = 0
                                phoneStatusMessage = null
                                passStatusMessage = null
                            }
                            .padding(vertical = 8.dp)
                            .testTag("tab_change_phone"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📱", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = StringRes.t(language, "মোবাইল পরিবর্তন", "Change Phone"),
                                color = if (selectedTab == 0) Color.Black else Slate300,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Tab 1: Password Change
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == 1) GoldPrimary else Color.Transparent)
                            .clickable {
                                selectedTab = 1
                                phoneStatusMessage = null
                                passStatusMessage = null
                            }
                            .padding(vertical = 8.dp)
                            .testTag("tab_change_password"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🔒", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = StringRes.t(language, "পাসওয়ার্ড পরিবর্তন", "Change Password"),
                                color = if (selectedTab == 1) Color.Black else Slate300,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Form Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (selectedTab == 0) {
                        // === CHANGE PHONE NUMBER ===
                        Text(
                            text = StringRes.t(language, "নতুন মোবাইল নম্বর যুক্ত করুন", "Enter New Mobile Number"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = newPhoneInput,
                            onValueChange = { if (it.length <= 11) newPhoneInput = it.filter { c -> c.isDigit() } },
                            label = { Text(StringRes.t(language, "নতুন মোবাইল নম্বর (১১ ডিজিট)", "New Mobile Number")) },
                            placeholder = { Text("01XXXXXXXXX") },
                            leadingIcon = { Text(text = "🇧🇩 +88", color = GoldLight, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_new_phone"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = CasinoBorderSubtle,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = StringRes.t(language, "অ্যাকাউন্ট ভেরিফিকেশন পাসওয়ার্ড", "Current Account Password"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = phoneVerifyPassInput,
                            onValueChange = { phoneVerifyPassInput = it },
                            label = { Text(StringRes.t(language, "বর্তমান পাসওয়ার্ড দিন", "Enter Current Password")) },
                            singleLine = true,
                            visualTransformation = if (isPhonePassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isPhonePassVisible = !isPhonePassVisible }) {
                                    Icon(
                                        if (isPhonePassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password",
                                        tint = TextSecondary
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_phone_verify_pass"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = CasinoBorderSubtle,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (phoneStatusMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (phoneStatusMessage!!.first) AccentEmerald.copy(alpha = 0.2f) else AccentCrimson.copy(alpha = 0.2f),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(
                                        listOf(if (phoneStatusMessage!!.first) AccentEmerald else AccentCrimson, Slate700)
                                    )
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = phoneStatusMessage!!.second,
                                    color = if (phoneStatusMessage!!.first) AccentEmerald else AccentCrimson,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(10.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                if (newPhoneInput.length < 10) {
                                    phoneStatusMessage = Pair(false, if (language == AppLanguage.BN) "অনুগ্রহ করে সঠিক ১১ ডিজিটের মোবাইল নম্বর দিন" else "Please enter valid 11 digit phone number")
                                    return@Button
                                }
                                val res = onUpdatePhone(newPhoneInput, phoneVerifyPassInput)
                                phoneStatusMessage = res
                                if (res.first) {
                                    newPhoneInput = ""
                                    phoneVerifyPassInput = ""
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_submit_change_phone"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = StringRes.t(language, "মোবাইল নম্বর সংরক্ষণ করুন", "Update Phone Number"),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        // === CHANGE PASSWORD ===
                        Text(
                            text = StringRes.t(language, "বর্তমান পাসওয়ার্ড", "Current Password"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = oldPassInput,
                            onValueChange = { oldPassInput = it },
                            label = { Text(StringRes.t(language, "বর্তমান পাসওয়ার্ড দিন", "Enter Current Password")) },
                            singleLine = true,
                            visualTransformation = if (isOldPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isOldPassVisible = !isOldPassVisible }) {
                                    Icon(
                                        if (isOldPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle",
                                        tint = TextSecondary
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_old_password"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = CasinoBorderSubtle,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = StringRes.t(language, "নতুন পাসওয়ার্ড", "New Password"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = newPassInput,
                            onValueChange = { newPassInput = it },
                            label = { Text(StringRes.t(language, "কমপক্ষে ৪ অক্ষরের নতুন পাসওয়ার্ড", "New Password (min 4 chars)")) },
                            singleLine = true,
                            visualTransformation = if (isNewPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isNewPassVisible = !isNewPassVisible }) {
                                    Icon(
                                        if (isNewPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle",
                                        tint = TextSecondary
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_new_password"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = CasinoBorderSubtle,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = StringRes.t(language, "নতুন পাসওয়ার্ড নিশ্চিত করুন", "Confirm New Password"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = confirmPassInput,
                            onValueChange = { confirmPassInput = it },
                            label = { Text(StringRes.t(language, "নতুন পাসওয়ার্ড পুনরায় দিন", "Re-type New Password")) },
                            singleLine = true,
                            visualTransformation = if (isConfirmPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isConfirmPassVisible = !isConfirmPassVisible }) {
                                    Icon(
                                        if (isConfirmPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle",
                                        tint = TextSecondary
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_confirm_password"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = CasinoBorderSubtle,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (passStatusMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (passStatusMessage!!.first) AccentEmerald.copy(alpha = 0.2f) else AccentCrimson.copy(alpha = 0.2f),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(
                                        listOf(if (passStatusMessage!!.first) AccentEmerald else AccentCrimson, Slate700)
                                    )
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = passStatusMessage!!.second,
                                    color = if (passStatusMessage!!.first) AccentEmerald else AccentCrimson,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(10.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                if (newPassInput.length < 4) {
                                    passStatusMessage = Pair(false, if (language == AppLanguage.BN) "পাসওয়ার্ড কমপক্ষে ৪ অক্ষরের হতে হবে" else "Password must be at least 4 characters")
                                    return@Button
                                }
                                if (newPassInput != confirmPassInput) {
                                    passStatusMessage = Pair(false, if (language == AppLanguage.BN) "নতুন পাসওয়ার্ড দুটি মিলছে না" else "New passwords do not match")
                                    return@Button
                                }
                                val res = onUpdatePassword(oldPassInput, newPassInput)
                                passStatusMessage = res
                                if (res.first) {
                                    oldPassInput = ""
                                    newPassInput = ""
                                    confirmPassInput = ""
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_submit_change_password"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = StringRes.t(language, "পাসওয়ার্ড পরিবর্তন করুন", "Change Password"),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Security Tip
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Slate900,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(Slate700, Slate800))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "💡", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = StringRes.t(
                                    language,
                                    "নিরাপত্তার স্বার্থে আপনার পাসওয়ার্ড ও ট্রানজেকশন তথ্য কারো সাথে শেয়ার করবেন না।",
                                    "For your safety, never share your password or OTP with anyone."
                                ),
                                color = Slate400,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
