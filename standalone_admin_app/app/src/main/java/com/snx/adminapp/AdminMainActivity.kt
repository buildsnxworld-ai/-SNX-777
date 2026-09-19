package com.example.admin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.io.FileOutputStream
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.*
import com.example.model.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

class AdminMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.snx.adminapp.data.SnxCloudSyncService.startAutoSync(this)
        setContent {
            MyApplicationTheme {
                AdminAppRoot()
            }
        }
    }
}

@Composable
fun AdminAppRoot() {
    val context = LocalContext.current
    val adminManager = remember { AdminManager.getInstance(context) }
    val isAdminLoggedIn by adminManager.isAdminLoggedIn.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        com.snx.adminapp.data.SnxCloudSyncService.pullFromCloud(context)
        SharedDataStore.pullFromOtherApp(context)
        adminManager.reloadFromStorage()
    }

    LaunchedEffect(Unit) {
        SharedDataStore.syncFlow.collect {
            adminManager.reloadFromStorage()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            SharedDataStore.pullFromOtherApp(context)
            adminManager.reloadFromStorage()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0A0E17)
    ) {
        if (!isAdminLoggedIn) {
            AdminLoginScreen(
                onLogin = { user, pass ->
                    val success = adminManager.loginAdmin(user, pass)
                    if (success) {
                        Toast.makeText(context, "স্বাগতম! এডমিন প্যানেল আনলক হয়েছে", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "ভুল ইউজারনেম বা পাসওয়ার্ড! সঠিক তথ্য দিন", Toast.LENGTH_LONG).show()
                    }
                    success
                }
            )
        } else {
            AdminDashboardScreen(
                adminManager = adminManager,
                onLogout = {
                    adminManager.logoutAdmin()
                    Toast.makeText(context, "এডমিন প্যানেল থেকে সফলভাবে লগআউট হয়েছে", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

/**
 * 1. SECURE ADMIN LOGIN SCREEN
 * As specified: "নাম হবে SNX ADMIN PANEL, পাসওয়ার্ড : ANX 20"
 */
@Composable
fun AdminLoginScreen(
    onLogin: (String, String) -> Boolean
) {
    var usernameInput by remember { mutableStateOf("SNX") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF0A0E17),
                        Color(0xFF030712)
                    )
                )
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .testTag("admin_login_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
            border = BorderStroke(
                1.5.dp,
                Brush.linearGradient(listOf(Color(0xFF00E676), Color(0xFFFFD54F), Color(0xFF0284C7)))
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Admin Shield Icon Badge
                Surface(
                    modifier = Modifier.size(68.dp),
                    shape = CircleShape,
                    color = Color(0xFF0F2E23),
                    border = BorderStroke(2.dp, Color(0xFF00E676))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Shield",
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title: SNX ADMIN PANEL
                Text(
                    text = "SNX ADMIN PANEL",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Executive Control & Management Center",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Username Input (Required: SNX)
                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = {
                        usernameInput = it
                        errorMessage = null
                    },
                    label = { Text("এডমিন ইউজারনেম (Username: SNX)", color = Color(0xFF94A3B8)) },
                    placeholder = { Text("যেমন: SNX", color = Color(0xFF64748B)) },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF00E676))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_username_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E676),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF0A0E17),
                        unfocusedContainerColor = Color(0xFF0A0E17)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Password Input: (Required: ANX 20)
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        errorMessage = null
                    },
                    label = { Text("সিকিউরিটি পাসওয়ার্ড (Password: ANX 20)", color = Color(0xFF94A3B8)) },
                    placeholder = { Text("পাসওয়ার্ড লিখুন...", color = Color(0xFF64748B)) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFFD54F))
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Visibility",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_password_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFFD54F),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF0A0E17),
                        unfocusedContainerColor = Color(0xFF0A0E17)
                    ),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Login Button
                Button(
                    onClick = {
                        if (usernameInput.trim().isEmpty()) {
                            errorMessage = "অনুগ্রহ করে এডমিন ইউজারনেম দিন (SNX)"
                        } else if (passwordInput.trim().isEmpty()) {
                            errorMessage = "অনুগ্রহ করে এডমিন পাসওয়ার্ড দিন (ANX 20)"
                        } else {
                            val ok = onLogin(usernameInput, passwordInput)
                            if (!ok) {
                                errorMessage = "ভুল ইউজারনেম বা পাসওয়ার্ড! সঠিক তথ্য: ইউজারনেম SNX, পাসওয়ার্ড ANX 20"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("admin_login_submit_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("এডমিন প্যানেলে প্রবেশ করুন", fontSize = 15.sp, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        val intent = Intent(context, com.example.MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("admin_open_game_website_btn"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8))
                ) {
                    Icon(Icons.Default.SportsEsports, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("গেমিং সাইটে যান (SNX 777)", fontSize = 13.sp, color = Color(0xFFE2E8F0))
                }
            }
        }
    }
}

/**
 * 2. FULL-FEATURED ADMIN DASHBOARD
 */
@Composable
fun AdminDashboardScreen(
    adminManager: AdminManager,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Payment Numbers, 1: Deposits, 2: Withdrawals, 3: Users, 4: Games, 5: Site Customization

    val paymentNumbers by adminManager.paymentNumbers.collectAsStateWithLifecycle()
    val depositRequests by adminManager.depositRequests.collectAsStateWithLifecycle()
    val withdrawalRequests by adminManager.withdrawalRequests.collectAsStateWithLifecycle()
    val siteConfig by adminManager.siteConfig.collectAsStateWithLifecycle()

    val pendingDepositsCount = depositRequests.count { it.status == TransactionStatus.PENDING }
    val pendingWithdrawalsCount = withdrawalRequests.count { it.status == TransactionStatus.PENDING }

    Scaffold(
        topBar = {
            AdminTopBar(
                siteName = siteConfig.siteName,
                onLogout = onLogout
            )
        },
        bottomBar = {
            AdminBottomNavigation(
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it },
                pendingDepositsCount = pendingDepositsCount,
                pendingWithdrawalsCount = pendingWithdrawalsCount
            )
        },
        containerColor = Color(0xFF0A0E17)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> AdminPaymentNumbersTab(adminManager = adminManager, numbers = paymentNumbers)
                1 -> AdminDepositApprovalTab(adminManager = adminManager, requests = depositRequests)
                2 -> AdminWithdrawApprovalTab(adminManager = adminManager, requests = withdrawalRequests)
                3 -> AdminUserManagementTab(adminManager = adminManager)
                4 -> AdminGameManagementTab(adminManager = adminManager)
                5 -> AdminSiteCustomizationTab(adminManager = adminManager, config = siteConfig)
            }
        }
    }
}

@Composable
fun AdminTopBar(
    siteName: String,
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    Surface(
        color = Color(0xFF10192A),
        border = BorderStroke(0.5.dp, Color(0xFF1E293B)),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF00E676),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("SNX", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "SNX ADMIN PANEL",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Live Dashboard • $siteName",
                        color = Color(0xFF00E676),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Quick Launch Player Website
                IconButton(
                    onClick = {
                        val launchIntent = context.packageManager.getLaunchIntentForPackage("com.aistudio.snx777.game")
                        if (launchIntent != null) {
                            context.startActivity(launchIntent)
                        } else {
                            val intent = Intent(context, com.example.MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.size(36.dp).testTag("top_admin_open_game_btn")
                ) {
                    Icon(Icons.Default.SportsEsports, contentDescription = "Open Game Website", tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier.size(36.dp).testTag("top_admin_logout_btn")
                ) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = "Logout", tint = Color(0xFFFF5252), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun AdminBottomNavigation(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    pendingDepositsCount: Int,
    pendingWithdrawalsCount: Int
) {
    Surface(
        color = Color(0xFF0E1626),
        border = BorderStroke(0.5.dp, Color(0xFF1E293B)),
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AdminNavItem(
                icon = Icons.Default.Payments,
                label = "পেমেন্ট নম্বর",
                badgeCount = 0,
                isSelected = selectedTab == 0,
                onClick = { onSelectTab(0) }
            )
            AdminNavItem(
                icon = Icons.Default.MoveToInbox,
                label = "ডিপোজিট",
                badgeCount = pendingDepositsCount,
                isSelected = selectedTab == 1,
                onClick = { onSelectTab(1) }
            )
            AdminNavItem(
                icon = Icons.Default.Outbox,
                label = "উইথড্র",
                badgeCount = pendingWithdrawalsCount,
                isSelected = selectedTab == 2,
                onClick = { onSelectTab(2) }
            )
            AdminNavItem(
                icon = Icons.Default.People,
                label = "ইউজার",
                badgeCount = 0,
                isSelected = selectedTab == 3,
                onClick = { onSelectTab(3) }
            )
            AdminNavItem(
                icon = Icons.Default.SportsEsports,
                label = "গেমস",
                badgeCount = 0,
                isSelected = selectedTab == 4,
                onClick = { onSelectTab(4) }
            )
            AdminNavItem(
                icon = Icons.Default.Tune,
                label = "সাইট",
                badgeCount = 0,
                isSelected = selectedTab == 5,
                onClick = { onSelectTab(5) }
            )
        }
    }
}

@Composable
fun AdminNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    badgeCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge(
                        containerColor = Color(0xFFE52338),
                        contentColor = Color.White
                    ) {
                        Text(text = "$badgeCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (isSelected) Color(0xFF00E676) else Color(0xFF64748B),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isSelected) Color(0xFF00E676) else Color(0xFF64748B),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

/**
 * TAB 0: MULTIPLE PAYMENT NUMBER MANAGEMENT (10 bKash + 10 Nagad = 20 Total)
 */
@Composable
fun AdminPaymentNumbersTab(
    adminManager: AdminManager,
    numbers: List<AdminPaymentNumber>
) {
    val context = LocalContext.current
    var isAddDialogOpen by remember { mutableStateOf(false) }
    var editingNumber by remember { mutableStateOf<AdminPaymentNumber?>(null) }
    var selectedMethodFilter by remember { mutableStateOf<PaymentMethod?>(null) }

    val filteredList = remember(numbers, selectedMethodFilter) {
        if (selectedMethodFilter == null) numbers else numbers.filter { it.method == selectedMethodFilter }
    }

    val bkashCount = numbers.count { it.method == PaymentMethod.BKASH }
    val nagadCount = numbers.count { it.method == PaymentMethod.NAGAD }
    val activeCount = numbers.count { it.isActive }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Top Header with summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
            border = BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ডাইনামিক পেমেন্ট নম্বর ম্যানেজার",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "মোট: ${numbers.size}টি (বিকাশ: $bkashCount, নগদ: $nagadCount, একটিভ: $activeCount)",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = { isAddDialogOpen = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("admin_add_number_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("নতুন নম্বর", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Method Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedMethodFilter == null,
                onClick = { selectedMethodFilter = null },
                label = { Text("সব নম্বর (${numbers.size})", fontSize = 11.sp) }
            )
            FilterChip(
                selected = selectedMethodFilter == PaymentMethod.BKASH,
                onClick = { selectedMethodFilter = PaymentMethod.BKASH },
                label = { Text("বিকাশ ($bkashCount)", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFE2136E).copy(alpha = 0.2f),
                    selectedLabelColor = Color(0xFFE2136E)
                )
            )
            FilterChip(
                selected = selectedMethodFilter == PaymentMethod.NAGAD,
                onClick = { selectedMethodFilter = PaymentMethod.NAGAD },
                label = { Text("নগদ ($nagadCount)", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFF7941D).copy(alpha = 0.2f),
                    selectedLabelColor = Color(0xFFF7941D)
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Numbers List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredList, key = { it.id }) { item ->
                PaymentNumberRowCard(
                    item = item,
                    onToggleActive = { adminManager.togglePaymentNumberStatus(item.id) },
                    onEdit = { editingNumber = item },
                    onDelete = {
                        adminManager.deletePaymentNumber(item.id)
                        Toast.makeText(context, "নম্বর ডিলিট করা হয়েছে", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // Add Number Dialog
    if (isAddDialogOpen) {
        AddOrEditPaymentNumberDialog(
            existing = null,
            onDismiss = { isAddDialogOpen = false },
            onSave = { method, number, label ->
                adminManager.addPaymentNumber(method, number, label)
                isAddDialogOpen = false
                Toast.makeText(context, "নতুন পেমেন্ট নম্বর যুক্ত হয়েছে", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Edit Number Dialog
    editingNumber?.let { existing ->
        AddOrEditPaymentNumberDialog(
            existing = existing,
            onDismiss = { editingNumber = null },
            onSave = { method, number, label ->
                adminManager.updatePaymentNumber(existing.id, number, label, existing.isActive)
                editingNumber = null
                Toast.makeText(context, "নম্বর আপডেট করা হয়েছে", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun PaymentNumberRowCard(
    item: AdminPaymentNumber,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isBkash = item.method == PaymentMethod.BKASH
    val brandColor = if (isBkash) Color(0xFFE2136E) else Color(0xFFF7941D)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isActive) Color(0xFF131D31) else Color(0xFF1A1F2C)
        ),
        border = BorderStroke(
            1.dp,
            if (item.isActive) brandColor.copy(alpha = 0.35f) else Color(0xFF334155)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = brandColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, brandColor.copy(alpha = 0.4f)),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isBkash) "bK" else "NG",
                            color = brandColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.number,
                            color = if (item.isActive) Color.White else Color(0xFF94A3B8),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (item.isActive) Color(0xFF0F2E23) else Color(0xFF261D1D)
                        ) {
                            Text(
                                text = if (item.isActive) "সক্রিয় (Active)" else "নিষ্ক্রিয়",
                                color = if (item.isActive) Color(0xFF00E676) else Color(0xFFFF5252),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.agentLabel} • ${item.createdAt}",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Active toggle switch
                Switch(
                    checked = item.isActive,
                    onCheckedChange = { onToggleActive() },
                    modifier = Modifier.scaleModifier(0.8f)
                )

                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun AddOrEditPaymentNumberDialog(
    existing: AdminPaymentNumber?,
    onDismiss: () -> Unit,
    onSave: (PaymentMethod, String, String) -> Unit
) {
    var selectedMethod by remember { mutableStateOf(existing?.method ?: PaymentMethod.BKASH) }
    var numberText by remember { mutableStateOf(existing?.number ?: "") }
    var labelText by remember { mutableStateOf(existing?.agentLabel ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
            border = BorderStroke(1.dp, Color(0xFF00E676))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (existing == null) "নতুন ডিপোজিট নম্বর যোগ করুন" else "ডিপোজিট নম্বর এডিট করুন",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Select Method
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(PaymentMethod.BKASH, PaymentMethod.NAGAD).forEach { m ->
                        val isSel = m == selectedMethod
                        val color = if (m == PaymentMethod.BKASH) Color(0xFFE2136E) else Color(0xFFF7941D)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) color.copy(alpha = 0.2f) else Color(0xFF0A0E17),
                            border = BorderStroke(1.dp, if (isSel) color else Color(0xFF334155)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedMethod = m }
                        ) {
                            Text(
                                text = if (m == PaymentMethod.BKASH) "বিকাশ (bKash)" else "নগদ (Nagad)",
                                color = if (isSel) color else Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Number Input
                OutlinedTextField(
                    value = numberText,
                    onValueChange = { numberText = it },
                    label = { Text("মোবাইল নম্বর (যেমন: 01712-348901)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Label Input
                OutlinedTextField(
                    value = labelText,
                    onValueChange = { labelText = it },
                    label = { Text("লেবেল/শাখা (যেমন: Agent Dhaka 01)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("বাতিল", color = Color(0xFF94A3B8))
                    }
                    Button(
                        onClick = {
                            if (numberText.trim().length >= 10) {
                                onSave(selectedMethod, numberText, labelText)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black)
                    ) {
                        Text("সেভ করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * TAB 1: DEPOSIT APPROVAL PIPELINE
 */
@Composable
fun AdminDepositApprovalTab(
    adminManager: AdminManager,
    requests: List<DepositRequest>
) {
    val context = LocalContext.current
    var statusFilter by remember { mutableStateOf<TransactionStatus?>(TransactionStatus.PENDING) }

    val filteredList = remember(requests, statusFilter) {
        if (statusFilter == null) requests else requests.filter { it.status == statusFilter }
    }

    val pendingCount = requests.count { it.status == TransactionStatus.PENDING }
    val approvedCount = requests.count { it.status == TransactionStatus.APPROVED }
    val rejectedCount = requests.count { it.status == TransactionStatus.REJECTED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Header
        Text(
            text = "ডিপোজিট এপ্রুভাল রিকোয়েস্ট",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "ইউজারদের পাঠানো TrxID যাচাই করে ব্যালেন্স এপ্রুভ করুন",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Status Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = statusFilter == TransactionStatus.PENDING,
                onClick = { statusFilter = TransactionStatus.PENDING },
                label = { Text("পেন্ডিং ($pendingCount)", fontSize = 11.sp) }
            )
            FilterChip(
                selected = statusFilter == TransactionStatus.APPROVED,
                onClick = { statusFilter = TransactionStatus.APPROVED },
                label = { Text("এপ্রুভড ($approvedCount)", fontSize = 11.sp) }
            )
            FilterChip(
                selected = statusFilter == TransactionStatus.REJECTED,
                onClick = { statusFilter = TransactionStatus.REJECTED },
                label = { Text("বাতিল ($rejectedCount)", fontSize = 11.sp) }
            )
            FilterChip(
                selected = statusFilter == null,
                onClick = { statusFilter = null },
                label = { Text("সব (${requests.size})", fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "কোন ডিপোজিট রিকোয়েস্ট পাওয়া যায়নি",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList, key = { it.id }) { req ->
                    DepositRequestCard(
                        request = req,
                        onApprove = {
                            val ok = adminManager.approveDeposit(req.id)
                            if (ok) {
                                Toast.makeText(context, "ডিপোজিট এপ্রুভ হয়েছে! ইউজারের ব্যালেন্সে ৳${req.amount} যোগ করা হয়েছে", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onReject = {
                            adminManager.rejectDeposit(req.id)
                            Toast.makeText(context, "ডিপোজিট বাতিল করা হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DepositRequestCard(
    request: DepositRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val context = LocalContext.current
    val isPending = request.status == TransactionStatus.PENDING
    val isBkash = request.method == PaymentMethod.BKASH
    val brandColor = if (isBkash) Color(0xFFE2136E) else Color(0xFFF7941D)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
        border = BorderStroke(
            1.dp,
            when (request.status) {
                TransactionStatus.APPROVED -> Color(0xFF00E676).copy(alpha = 0.4f)
                TransactionStatus.PENDING -> Color(0xFFFFD54F).copy(alpha = 0.5f)
                TransactionStatus.REJECTED -> Color(0xFFFF5252).copy(alpha = 0.4f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = brandColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isBkash) "bK" else "NG",
                                color = brandColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "৳%,.0f".format(Locale.US, request.amount),
                        color = Color(0xFFFFD54F),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (request.status) {
                        TransactionStatus.APPROVED -> Color(0xFF0F2E23)
                        TransactionStatus.PENDING -> Color(0xFF332709)
                        TransactionStatus.REJECTED -> Color(0xFF2E1215)
                    }
                ) {
                    Text(
                        text = when (request.status) {
                            TransactionStatus.APPROVED -> "✔ APPROVED"
                            TransactionStatus.PENDING -> "⏳ PENDING"
                            TransactionStatus.REJECTED -> "✖ REJECTED"
                        },
                        color = when (request.status) {
                            TransactionStatus.APPROVED -> Color(0xFF00E676)
                            TransactionStatus.PENDING -> Color(0xFFFFD54F)
                            TransactionStatus.REJECTED -> Color(0xFFFF5252)
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // User & Trx Details
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "ইউজার: ${request.username} (${request.userPhone})", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(text = request.createdAt, color = Color(0xFF64748B), fontSize = 10.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0A0E17))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "ট্রানজেকশন আইডি (TrxID):", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    Text(text = request.trxId, color = Color(0xFF38BDF8), fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                }
                IconButton(
                    onClick = {
                        val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cb.setPrimaryClip(ClipData.newPlainText("TrxID", request.trxId))
                        Toast.makeText(context, "TrxID কপি হয়েছে: ${request.trxId}", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy TrxID", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ইউজার প্রেরক নম্বর: ${request.userAccountNo} • রিসিভ এজেন্ট নম্বর: ${request.agentNumberUsed}",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp
            )

            if (isPending) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                        border = BorderStroke(1.dp, Color(0xFFFF5252))
                    ) {
                        Text("✖ বাতিল (Reject)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black)
                    ) {
                        Text("✔ এপ্রুভ (Approve)", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

/**
 * TAB 2: WITHDRAW APPROVAL PIPELINE
 */
@Composable
fun AdminWithdrawApprovalTab(
    adminManager: AdminManager,
    requests: List<WithdrawalRequest>
) {
    val context = LocalContext.current
    var statusFilter by remember { mutableStateOf<TransactionStatus?>(TransactionStatus.PENDING) }

    val filteredList = remember(requests, statusFilter) {
        if (statusFilter == null) requests else requests.filter { it.status == statusFilter }
    }

    val pendingCount = requests.count { it.status == TransactionStatus.PENDING }
    val approvedCount = requests.count { it.status == TransactionStatus.APPROVED }
    val rejectedCount = requests.count { it.status == TransactionStatus.REJECTED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Text(
            text = "উইথড্র এপ্রুভাল রিকোয়েস্ট",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "ইউজারের বিকাশ বা নগদ একাউন্টে টাকা পাঠিয়ে এপ্রুভ করুন",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = statusFilter == TransactionStatus.PENDING,
                onClick = { statusFilter = TransactionStatus.PENDING },
                label = { Text("পেন্ডিং ($pendingCount)", fontSize = 11.sp) }
            )
            FilterChip(
                selected = statusFilter == TransactionStatus.APPROVED,
                onClick = { statusFilter = TransactionStatus.APPROVED },
                label = { Text("এপ্রুভড ($approvedCount)", fontSize = 11.sp) }
            )
            FilterChip(
                selected = statusFilter == TransactionStatus.REJECTED,
                onClick = { statusFilter = TransactionStatus.REJECTED },
                label = { Text("বাতিল ($rejectedCount)", fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "কোন উইথড্র রিকোয়েস্ট নেই",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList, key = { it.id }) { req ->
                    WithdrawRequestCard(
                        request = req,
                        onApprove = {
                            adminManager.approveWithdrawal(req.id)
                            Toast.makeText(context, "উইথড্র এপ্রুভ সফল! টাকা পাঠানো নিশ্চিত হয়েছে", Toast.LENGTH_SHORT).show()
                        },
                        onReject = {
                            adminManager.rejectWithdrawal(req.id)
                            Toast.makeText(context, "উইথড্র বাতিল এবং ব্যালেন্স রিফান্ড করা হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WithdrawRequestCard(
    request: WithdrawalRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val context = LocalContext.current
    val isPending = request.status == TransactionStatus.PENDING
    val isBkash = request.method == PaymentMethod.BKASH
    val brandColor = if (isBkash) Color(0xFFE2136E) else Color(0xFFF7941D)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
        border = BorderStroke(
            1.dp,
            when (request.status) {
                TransactionStatus.APPROVED -> Color(0xFF00E676).copy(alpha = 0.4f)
                TransactionStatus.PENDING -> Color(0xFFFFD54F).copy(alpha = 0.5f)
                TransactionStatus.REJECTED -> Color(0xFFFF5252).copy(alpha = 0.4f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "৳%,.0f".format(Locale.US, request.amount),
                    color = Color(0xFF00E676),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (request.status) {
                        TransactionStatus.APPROVED -> Color(0xFF0F2E23)
                        TransactionStatus.PENDING -> Color(0xFF332709)
                        TransactionStatus.REJECTED -> Color(0xFF2E1215)
                    }
                ) {
                    Text(
                        text = when (request.status) {
                            TransactionStatus.APPROVED -> "✔ COMPLETED"
                            TransactionStatus.PENDING -> "⏳ PENDING"
                            TransactionStatus.REJECTED -> "✖ REJECTED & REFUNDED"
                        },
                        color = when (request.status) {
                            TransactionStatus.APPROVED -> Color(0xFF00E676)
                            TransactionStatus.PENDING -> Color(0xFFFFD54F)
                            TransactionStatus.REJECTED -> Color(0xFFFF5252)
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "ইউজার: ${request.username} (${request.userPhone})", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(4.dp))

            // Receiver Target Account Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0A0E17))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "প্রাপক নম্বর (${if (isBkash) "bKash Personal" else "Nagad Personal"}):", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    Text(text = request.targetAccountNo, color = brandColor, fontSize = 15.sp, fontWeight = FontWeight.Black)
                }
                IconButton(
                    onClick = {
                        val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cb.setPrimaryClip(ClipData.newPlainText("Receiver Phone", request.targetAccountNo))
                        Toast.makeText(context, "নম্বর কপি হয়েছে: ${request.targetAccountNo}", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Phone", tint = brandColor, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "রিকোয়েস্ট টাইম: ${request.createdAt}", color = Color(0xFF64748B), fontSize = 10.sp)

            if (isPending) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                        border = BorderStroke(1.dp, Color(0xFFFF5252))
                    ) {
                        Text("✖ বাতিল ও রিফান্ড", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black)
                    ) {
                        Text("✔ টাকা পাঠানো হয়েছে", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

/**
 * TAB 3: USER MANAGEMENT & LIVE REGISTRATION LIST
 */
@Composable
fun AdminUserManagementTab(
    adminManager: AdminManager
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var editingUser by remember { mutableStateOf<RegisteredAccount?>(null) }
    var editingStatusUser by remember { mutableStateOf<RegisteredAccount?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        SharedDataStore.syncFlow.collect {
            refreshTrigger++
        }
    }

    val allUsers = remember(refreshTrigger) {
        adminManager.getAllRegisteredUsers()
    }

    val filteredUsers = remember(allUsers, searchQuery) {
        if (searchQuery.isBlank()) allUsers
        else allUsers.filter {
            it.username.contains(searchQuery, ignoreCase = true) ||
            it.phone.contains(searchQuery)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ইউজার ম্যানেজমেন্ট ও রেজিস্ট্রেশন লিস্ট",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "মোট নিবন্ধিত ইউজার: ${allUsers.size} জন",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
            IconButton(onClick = { refreshTrigger++ }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color(0xFF00E676))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ইউজারনেম বা ফোন নম্বর দিয়ে খুঁজুন...", color = Color(0xFF64748B)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (allUsers.isEmpty()) "এখনো কোনো ইউজার একাউন্ট খুলেনি" else "কোন ইউজার মেলেনি",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredUsers, key = { it.phone + it.username }) { u ->
                    UserAccountCard(
                        user = u,
                        onEditBalance = { editingUser = u },
                        onEditStatus = { editingStatusUser = u }
                    )
                }
            }
        }
    }

    // Edit Balance Dialog
    editingUser?.let { targetUser ->
        AdminEditUserBalanceDialog(
            user = targetUser,
            onDismiss = { editingUser = null },
            onSaveBalance = { newBal ->
                adminManager.updateUserBalance(targetUser.phone, newBal)
                editingUser = null
                refreshTrigger++
                Toast.makeText(context, "${targetUser.username} এর ব্যালেন্স ৳$newBal এ আপডেট করা হয়েছে", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Edit Status Dialog
    editingStatusUser?.let { targetUser ->
        AdminEditUserStatusDialog(
            user = targetUser,
            onDismiss = { editingStatusUser = null },
            onSaveStatus = { newStatus ->
                adminManager.updateUserStatus(targetUser.phone, newStatus)
                editingStatusUser = null
                refreshTrigger++
                val statusLabel = when (newStatus) {
                    "ACTIVE" -> "সক্রিয়"
                    "SUSPENDED" -> "স্থগিত"
                    "BANNED" -> "নিষিদ্ধ"
                    else -> newStatus
                }
                Toast.makeText(context, "${targetUser.username} এর স্ট্যাটাস '$statusLabel' করা হয়েছে", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun UserAccountCard(
    user: RegisteredAccount,
    onEditBalance: () -> Unit,
    onEditStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF0F2E23),
                        border = BorderStroke(1.dp, Color(0xFF00E676)),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = user.username.take(2).uppercase(),
                                color = Color(0xFF00E676),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = user.username, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            // Status badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (user.status) {
                                    "SUSPENDED" -> Color(0xFFE65100).copy(alpha = 0.2f)
                                    "BANNED" -> Color(0xFFD32F2F).copy(alpha = 0.2f)
                                    else -> Color(0xFF00E676).copy(alpha = 0.2f)
                                },
                                border = BorderStroke(
                                    1.dp,
                                    when (user.status) {
                                        "SUSPENDED" -> Color(0xFFFF9800)
                                        "BANNED" -> Color(0xFFFF5252)
                                        else -> Color(0xFF00E676)
                                    }
                                )
                            ) {
                                Text(
                                    text = when (user.status) {
                                        "SUSPENDED" -> "⏸ স্থগিত"
                                        "BANNED" -> "🚫 ব্যান"
                                        else -> "✔ সক্রিয়"
                                    },
                                    color = when (user.status) {
                                        "SUSPENDED" -> Color(0xFFFFB74D)
                                        "BANNED" -> Color(0xFFFF5252)
                                        else -> Color(0xFF00E676)
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(text = "📱 ${user.phone}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(text = "📅 রেজিস্ট্রেশন: ${user.registeredDate}", color = Color(0xFF38BDF8), fontSize = 10.sp)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF0A0E17),
                    border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "৳%,.2f".format(Locale.US, user.balanceBDT),
                        color = Color(0xFFFFD54F),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "মোট ডিপোজিট: ৳%,.0f • উত্তোলন: ৳%,.0f".format(Locale.US, user.totalDeposited, user.totalWithdrawn),
                    color = Color(0xFF64748B),
                    fontSize = 10.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onEditStatus,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8))
                    ) {
                        Text("স্ট্যাটাস", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                    }

                    Button(
                        onClick = onEditBalance,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7), contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ব্যালেন্স", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminEditUserBalanceDialog(
    user: RegisteredAccount,
    onDismiss: () -> Unit,
    onSaveBalance: (Double) -> Unit
) {
    var balanceInput by remember { mutableStateOf(user.balanceBDT.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
            border = BorderStroke(1.5.dp, Color(0xFF0284C7))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ইউজার ব্যালেন্স পরিবর্তন",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${user.username} (${user.phone})",
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = balanceInput,
                    onValueChange = { balanceInput = it },
                    label = { Text("নতুন ব্যালেন্স (BDT)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick increment buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(100.0, 500.0, 1000.0).forEach { delta ->
                        OutlinedButton(
                            onClick = {
                                val cur = balanceInput.toDoubleOrNull() ?: 0.0
                                balanceInput = (cur + delta).toString()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(2.dp)
                        ) {
                            Text("+৳%,.0f".format(delta), fontSize = 10.sp, color = Color(0xFF00E676))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("বাতিল", color = Color(0xFF94A3B8))
                    }

                    Button(
                        onClick = {
                            val newBal = balanceInput.toDoubleOrNull()
                            if (newBal != null && newBal >= 0) {
                                onSaveBalance(newBal)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black)
                    ) {
                        Text("আপডেট করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminEditUserStatusDialog(
    user: RegisteredAccount,
    onDismiss: () -> Unit,
    onSaveStatus: (String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(user.status.ifBlank { "ACTIVE" }) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
            border = BorderStroke(1.5.dp, Color(0xFF00E676))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ইউজার একাউন্ট স্ট্যাটাস",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${user.username} (${user.phone})",
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                val statuses = listOf(
                    Triple("ACTIVE", "🟢 সক্রিয় (ACTIVE)", "ইউজার স্বাভাবিকভাবে লগইন, ডিপোজিট ও খেলতে পারবেন"),
                    Triple("SUSPENDED", "🟠 স্থগিত (SUSPENDED)", "সাময়িক বন্ধ, ডিপোজিট বা গেম খেলা স্থগিত থাকবে"),
                    Triple("BANNED", "🔴 নিষিদ্ধ (BANNED)", "একাউন্ট স্থায়ীভাবে ব্যান থাকবে")
                )

                statuses.forEach { (statusKey, statusTitle, statusDesc) ->
                    val isSelected = selectedStatus == statusKey
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFF1E2E4A) else Color(0xFF0A0E17),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF00E676) else Color(0xFF1E293B)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedStatus = statusKey }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedStatus = statusKey },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00E676))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(text = statusTitle, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(text = statusDesc, color = Color(0xFF94A3B8), fontSize = 10.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("বাতিল", color = Color(0xFF94A3B8))
                    }

                    Button(
                        onClick = { onSaveStatus(selectedStatus) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black)
                    ) {
                        Text("সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * TAB 4: DYNAMIC GAME MANAGEMENT
 */
@Composable
fun AdminGameManagementTab(
    adminManager: AdminManager
) {
    val context = LocalContext.current
    val gamesList by adminManager.gamesList.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<GameCategory?>(null) }
    var isAddGameDialogOpen by remember { mutableStateOf(false) }
    var editingGame by remember { mutableStateOf<GameItem?>(null) }
    var deletingGame by remember { mutableStateOf<GameItem?>(null) }

    val filteredGames = remember(gamesList, searchQuery, selectedCategoryFilter) {
        gamesList.filter { game ->
            val matchesCategory = selectedCategoryFilter == null || game.category == selectedCategoryFilter
            val matchesSearch = searchQuery.isBlank() ||
                    game.titleBn.contains(searchQuery, ignoreCase = true) ||
                    game.titleEn.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ডাইনামিক গেম ম্যানেজমেন্ট",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "মোট গেম: ${gamesList.size} টি • চালু: ${gamesList.count { it.isActive }} টি",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = { isAddGameDialogOpen = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("নতুন গেম", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("গেমের নাম দিয়ে খুঁজুন...", color = Color(0xFF64748B)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedCategoryFilter == null,
                    onClick = { selectedCategoryFilter = null },
                    label = { Text("সব (${gamesList.size})", fontSize = 11.sp) }
                )
            }
            items(GameCategory.values().toList()) { cat ->
                FilterChip(
                    selected = selectedCategoryFilter == cat,
                    onClick = { selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat },
                    label = { Text("${cat.icon} ${cat.bn}", fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredGames.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "কোনো গেম মেলেনি",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredGames, key = { it.id }) { game ->
                    AdminGameCard(
                        game = game,
                        onToggleActive = { adminManager.toggleGameActive(game.id) },
                        onStatusChange = { newStatus ->
                            adminManager.setGameServerStatus(game.id, newStatus)
                            val statusLabel = when (newStatus) {
                                GameServerStatus.ACTIVE -> "সক্রিয়"
                                GameServerStatus.SERVER_UPDATE -> "সার্ভার আপডেট"
                                GameServerStatus.SERVER_ERROR -> "সার্ভার এরর"
                                GameServerStatus.OFFLINE -> "বন্ধ/নিষ্ক্রিয়"
                            }
                            Toast.makeText(context, "${game.titleBn} এর স্ট্যাটাস '$statusLabel' করা হয়েছে", Toast.LENGTH_SHORT).show()
                        },
                        onEdit = { editingGame = game },
                        onDelete = { deletingGame = game }
                    )
                }
            }
        }
    }

    // Add Game Dialog
    if (isAddGameDialogOpen) {
        AdminAddEditGameDialog(
            gameToEdit = null,
            onDismiss = { isAddGameDialogOpen = false },
            onSave = { newGame ->
                adminManager.addGame(newGame)
                isAddGameDialogOpen = false
                Toast.makeText(context, "${newGame.titleBn} গেমটি সফলভাবে যোগ করা হয়েছে!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Edit Game Dialog
    editingGame?.let { game ->
        AdminAddEditGameDialog(
            gameToEdit = game,
            onDismiss = { editingGame = null },
            onSave = { updatedGame ->
                adminManager.updateGame(updatedGame)
                editingGame = null
                Toast.makeText(context, "${updatedGame.titleBn} গেমটি আপডেট করা হয়েছে!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Confirmation Dialog
    deletingGame?.let { game ->
        AlertDialog(
            onDismissRequest = { deletingGame = null },
            containerColor = Color(0xFF131D31),
            title = { Text("গেম ডিলিট নিশ্চিত করুন", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("আপনি কি নিশ্চিত যে '${game.titleBn}' গেমটি তালিকা থেকে ডিলিট করতে চান?", color = Color(0xFF94A3B8)) },
            confirmButton = {
                Button(
                    onClick = {
                        adminManager.deleteGame(game.id)
                        deletingGame = null
                        Toast.makeText(context, "গেমটি ডিলিট করা হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("ডিলিট করুন", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deletingGame = null }) {
                    Text("বাতিল", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
fun AdminGameCard(
    game: GameItem,
    onToggleActive: () -> Unit,
    onStatusChange: (GameServerStatus) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = Color(game.serverStatus.colorHex)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
        border = BorderStroke(1.dp, if (game.isActive) Color(0xFF1E293B) else Color(0xFFEF4444).copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0A0E17),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier
                            .size(52.dp)
                            .clickable { onEdit() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (game.imageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = game.imageUrl,
                                    contentDescription = game.titleEn,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Text(text = game.iconEmoji, fontSize = 26.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = game.titleBn,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (game.badge != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFFFD54F)
                                ) {
                                    Text(
                                        text = game.badge,
                                        color = Color.Black,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        // Server Status Badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = statusColor.copy(alpha = 0.18f),
                            border = BorderStroke(0.8.dp, statusColor.copy(alpha = 0.6f)),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = when (game.serverStatus) {
                                    GameServerStatus.ACTIVE -> "🟢 সক্রিয় (Active)"
                                    GameServerStatus.SERVER_UPDATE -> "🟠 সার্ভার আপডেট (Update)"
                                    GameServerStatus.SERVER_ERROR -> "🔴 সার্ভার এরর (Error)"
                                    GameServerStatus.OFFLINE -> "⚫ অফলাইন (Offline)"
                                },
                                color = statusColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        Text(
                            text = "${game.category.bn} • ${game.titleEn}",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = "মিনিমাম বাজি: ৳${game.minBet.toInt()} • প্লেয়ার: ${game.playersCount}",
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp
                        )
                    }
                }

                // Active toggle switch
                Column(horizontalAlignment = Alignment.End) {
                    Switch(
                        checked = game.isActive,
                        onCheckedChange = { onToggleActive() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF00E676),
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFF334155)
                        )
                    )
                    Text(
                        text = if (game.isActive) "চালু" else "বন্ধ",
                        color = if (game.isActive) Color(0xFF00E676) else Color(0xFFEF4444),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Server Status One-Tap Switcher
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0A0E17),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "সার্ভার স্ট্যাটাস কন্ট্রোল (এক ক্লিকে পরিবর্তন):",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        GameServerStatus.values().forEach { st ->
                            val isSel = game.serverStatus == st
                            val stColor = Color(st.colorHex)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) stColor.copy(alpha = 0.25f) else Color(0xFF131D31),
                                border = BorderStroke(1.dp, if (isSel) stColor else Color(0xFF334155)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onStatusChange(st) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 5.dp, horizontal = 2.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(stColor)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = when (st) {
                                            GameServerStatus.ACTIVE -> "সক্রিয়"
                                            GameServerStatus.SERVER_UPDATE -> "আপডেট"
                                            GameServerStatus.SERVER_ERROR -> "এরর"
                                            GameServerStatus.OFFLINE -> "বন্ধ"
                                        },
                                        color = if (isSel) Color.White else Color(0xFF94A3B8),
                                        fontSize = 9.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Change Picture / Banner Button
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp),
                    border = BorderStroke(1.dp, Color(0xFF00E676))
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF00E676))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ছবি / ডিজাইন বদলান", fontSize = 10.sp, color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("এডিট", fontSize = 10.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF4444))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ডিলিট", fontSize = 10.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAddEditGameDialog(
    gameToEdit: GameItem?,
    onDismiss: () -> Unit,
    onSave: (GameItem) -> Unit
) {
    val context = LocalContext.current
    var titleBn by remember { mutableStateOf(gameToEdit?.titleBn ?: "") }
    var titleEn by remember { mutableStateOf(gameToEdit?.titleEn ?: "") }
    var selectedCategory by remember { mutableStateOf(gameToEdit?.category ?: GameCategory.SLOTS) }
    var imageUrl by remember { mutableStateOf(gameToEdit?.imageUrl ?: "") }
    var iconEmoji by remember { mutableStateOf(gameToEdit?.iconEmoji ?: "🎰") }
    var minBetText by remember { mutableStateOf(gameToEdit?.minBet?.toInt()?.toString() ?: "10") }
    var badgeText by remember { mutableStateOf(gameToEdit?.badge ?: "") }
    var isActive by remember { mutableStateOf(gameToEdit?.isActive ?: true) }
    var serverStatus by remember { mutableStateOf(gameToEdit?.serverStatus ?: GameServerStatus.ACTIVE) }

    // Photo Picker Launcher for uploading from device / phone gallery
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val coversDir = File(context.filesDir, "game_covers")
                if (!coversDir.exists()) coversDir.mkdirs()
                val targetFile = File(coversDir, "game_img_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
                imageUrl = targetFile.absolutePath
                Toast.makeText(context, "ছবি সফলভাবে আপলোড করা হয়েছে!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "ছবি আপলোড করতে ব্যর্থ: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
            border = BorderStroke(1.5.dp, Color(0xFF00E676)),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (gameToEdit == null) "নতুন গেম যোগ করুন" else "গেম ডিজাইন ও স্ট্যাটাস এডিট",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "কাস্টম ছবি আপলোড, ইমোজি ও সার্ভার স্ট্যাটাস",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Real-time Card Preview Box
                Text("লাইভ প্রিভিউ (ওয়েবসাইট ও অ্যাপে যেমন দেখাবে):", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0A0E17),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (imageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = "Preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Text(text = iconEmoji, fontSize = 30.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (titleBn.isNotBlank()) titleBn else "গেমের শিরোনাম",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (badgeText.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFFFD54F)
                                    ) {
                                        Text(
                                            text = badgeText,
                                            color = Color.Black,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "${selectedCategory.bn} • ${if (titleEn.isNotBlank()) titleEn else "Title En"}",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )

                            // Status Chip in Preview
                            val previewStatusColor = Color(serverStatus.colorHex)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = previewStatusColor.copy(alpha = 0.2f),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = when (serverStatus) {
                                        GameServerStatus.ACTIVE -> "🟢 সক্রিয় (Active)"
                                        GameServerStatus.SERVER_UPDATE -> "🟠 সার্ভার আপডেট"
                                        GameServerStatus.SERVER_ERROR -> "🔴 সার্ভার এরর"
                                        GameServerStatus.OFFLINE -> "⚫ বন্ধ (Offline)"
                                    },
                                    color = previewStatusColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Image / Icon Uploader Section
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🖼️ গেম ইমেজ ও আইকন আপলোডার",
                                color = Color(0xFF00E676),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (imageUrl.isNotBlank()) {
                                TextButton(
                                    onClick = { imageUrl = "" },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("মুছুন / রিসেট", color = Color(0xFFEF4444), fontSize = 10.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Button 1: Gallery / Device File Picker
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "গ্যালারি / ফোন থেকে ছবি আপলোড করুন",
                                color = Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Option 2: Image URL input
                        OutlinedTextField(
                            value = imageUrl,
                            onValueChange = { imageUrl = it },
                            label = { Text("অথবা ছবির অনলাইন URL দিন") },
                            placeholder = { Text("https://example.com/plane.png", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Option 3: Curated Quick Presets (Spribe Plane, Rocket, 777 Slots, Cricket, etc.)
                        Text(
                            text = "জনপ্রিয় প্রি-সেট ইমোজি ও গ্রাফিক্স (ক্লিক করে সেট করুন):",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val presets = listOf(
                                "✈️" to "বিমান ১",
                                "🚀" to "রকেট",
                                "🛩️" to "জেট ২",
                                "🛸" to "ইউএফও",
                                "🎰" to "স্লট ৭৭৭",
                                "🎡" to "লাকি হুইল",
                                "🏏" to "ক্রিকেট",
                                "🃏" to "তিন পাত্তি",
                                "🐉" to "ড্রাগন",
                                "🎯" to "রুলেট",
                                "🎲" to "ডাইস",
                                "🍒" to "ফ্রুট",
                                "🐟" to "ফিশ"
                            )
                            items(presets) { (emo, lbl) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (iconEmoji == emo && imageUrl.isBlank()) Color(0xFF00E676).copy(alpha = 0.2f) else Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, if (iconEmoji == emo && imageUrl.isBlank()) Color(0xFF00E676) else Color(0xFF334155)),
                                    modifier = Modifier.clickable {
                                        iconEmoji = emo
                                        imageUrl = ""
                                    }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(emo, fontSize = 20.sp)
                                        Text(lbl, fontSize = 8.sp, color = Color(0xFFCBD5E1))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Server Status Selector Section
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(serverStatus.colorHex).copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "⚡ সার্ভার স্ট্যাটাস কন্ট্রোল (Server Status)",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "গেমটি চালু, সার্ভার এরর বা মেইনটেন্যান্স নোটিসে রাখুন",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            GameServerStatus.values().forEach { st ->
                                val isSel = serverStatus == st
                                val stCol = Color(st.colorHex)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSel) stCol.copy(alpha = 0.25f) else Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, if (isSel) stCol else Color(0xFF334155)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            serverStatus = st
                                            if (st == GameServerStatus.OFFLINE) isActive = false
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(stCol)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = when (st) {
                                                GameServerStatus.ACTIVE -> "সক্রিয়"
                                                GameServerStatus.SERVER_UPDATE -> "আপডেট"
                                                GameServerStatus.SERVER_ERROR -> "এরর"
                                                GameServerStatus.OFFLINE -> "বন্ধ"
                                            },
                                            color = if (isSel) Color.White else Color(0xFF94A3B8),
                                            fontSize = 9.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title Inputs
                OutlinedTextField(
                    value = titleBn,
                    onValueChange = { titleBn = it },
                    label = { Text("গেমের নাম (বাংলা)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = titleEn,
                    onValueChange = { titleEn = it },
                    label = { Text("গেমের নাম (English)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("ক্যাটাগরি নির্বাচন করুন:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(GameCategory.values().toList()) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text("${cat.icon} ${cat.bn}", fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = iconEmoji,
                        onValueChange = { iconEmoji = it },
                        label = { Text("ডিফল্ট ইমোজি") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = minBetText,
                        onValueChange = { minBetText = it },
                        label = { Text("মিনিমাম বাজি (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Badge selector row
                Text("ব্যাজ (Badge):", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("" to "কোনোটি না", "HOT" to "🔥 HOT", "LIVE" to "🟢 LIVE", "JACKPOT" to "💰 JACKPOT", "NEW" to "✨ NEW").forEach { (bVal, bLabel) ->
                        FilterChip(
                            selected = badgeText == bVal,
                            onClick = { badgeText = bVal },
                            label = { Text(bLabel, fontSize = 9.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Active toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isActive) "🟢 ওয়েবসাইটে প্রদর্শিত হবে (Active)" else "🔴 সাময়িক বন্ধ থাকবে (Inactive)",
                        color = if (isActive) Color(0xFF00E676) else Color(0xFFEF4444),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Switch(
                        checked = isActive,
                        onCheckedChange = {
                            isActive = it
                            if (!it) serverStatus = GameServerStatus.OFFLINE
                            else if (serverStatus == GameServerStatus.OFFLINE) serverStatus = GameServerStatus.ACTIVE
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF00E676))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("বাতিল", color = Color(0xFF94A3B8))
                    }

                    Button(
                        onClick = {
                            if (titleBn.isBlank()) return@Button
                            val gameId = gameToEdit?.id ?: "game_${System.currentTimeMillis()}"
                            val newGame = GameItem(
                                id = gameId,
                                titleBn = titleBn.trim(),
                                titleEn = if (titleEn.isBlank()) titleBn.trim() else titleEn.trim(),
                                category = selectedCategory,
                                imageUrl = imageUrl.trim(),
                                iconEmoji = if (iconEmoji.isBlank()) "🎰" else iconEmoji.trim(),
                                badge = if (badgeText.isBlank()) null else badgeText.trim(),
                                minBet = minBetText.toDoubleOrNull() ?: 10.0,
                                playersCount = gameToEdit?.playersCount ?: (100..500).random(),
                                isActive = (isActive && serverStatus != GameServerStatus.OFFLINE),
                                serverStatus = serverStatus
                            )
                            onSave(newGame)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black)
                    ) {
                        Text(if (gameToEdit == null) "গেম যোগ করুন" else "সেভ করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * TAB 4: SITE & GAME CUSTOMIZATION
 */
@Composable
fun AdminSiteCustomizationTab(
    adminManager: AdminManager,
    config: SiteCustomization
) {
    val context = LocalContext.current
    var siteNameInput by remember { mutableStateOf(config.siteName) }
    var marqueeTickerInput by remember { mutableStateOf(config.marqueeTicker) }
    var bannerNoticeInput by remember { mutableStateOf(config.bannerNotice) }
    var aviatorTitleInput by remember { mutableStateOf(config.aviatorGameTitle) }
    var isMaintenance by remember { mutableStateOf(config.isMaintenanceMode) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "সাইট ও গেম কাস্টমাইজেশন কন্ট্রোল",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "এখান থেকে ওয়েবসাইট নোটিশ, লোগো নাম ও গেম টাইটেল পরিবর্তন করুন",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Site Name
        OutlinedTextField(
            value = siteNameInput,
            onValueChange = { siteNameInput = it },
            label = { Text("ওয়েবসাইট নাম / Brand Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Marquee Ticker
        OutlinedTextField(
            value = marqueeTickerInput,
            onValueChange = { marqueeTickerInput = it },
            label = { Text("লাইভ টিকার নোটিশ (Ticker Marquee)") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Top Banner Notice
        OutlinedTextField(
            value = bannerNoticeInput,
            onValueChange = { bannerNoticeInput = it },
            label = { Text("টপ ব্যানার প্রোমোশনাল অফার (Banner Notice)") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Aviator Game Title
        OutlinedTextField(
            value = aviatorTitleInput,
            onValueChange = { aviatorTitleInput = it },
            label = { Text("এভিয়েটর গেম টাইটেল (Game Branding)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Maintenance Mode Toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
            border = BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "জরুরী মেইনটেন্যান্স মোড", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(text = "চালু থাকলে ইউজাররা সাময়িক রক্ষণাবেক্ষণ বার্তা দেখতে পাবে", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
                Switch(checked = isMaintenance, onCheckedChange = { isMaintenance = it })
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Save Button
        Button(
            onClick = {
                adminManager.updateSiteConfig(
                    SiteCustomization(
                        siteName = siteNameInput.trim(),
                        marqueeTicker = marqueeTickerInput.trim(),
                        bannerNotice = bannerNoticeInput.trim(),
                        aviatorGameTitle = aviatorTitleInput.trim(),
                        isMaintenanceMode = isMaintenance
                    )
                )
                Toast.makeText(context, "সাইট সেটিংস সফলভাবে সেভ হয়েছে!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("admin_save_site_settings_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black)
        ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("💾 সেভ করুন (Save Changes)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun Modifier.scaleModifier(scale: Float): Modifier = this
