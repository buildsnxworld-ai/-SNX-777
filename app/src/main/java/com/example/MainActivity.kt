package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.SnxViewModel
import com.example.model.AppLanguage
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppHeader
import com.example.ui.components.AuthModal
import com.example.ui.components.CustomerSupportModal
import com.example.ui.components.InsufficientBalanceModal
import com.example.ui.components.NoInternetOverlay
import com.example.ui.components.PromotionalAdsModal
import com.example.ui.games.AviatorCrashGame
import com.example.ui.games.CricketBettingGame
import com.example.ui.games.DiceRollGame
import com.example.ui.games.DragonTigerGame
import com.example.ui.games.LuckyWheelGame
import com.example.ui.games.SlotMachineGame
import com.example.ui.games.TeenPattiGame
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.util.NetworkMonitor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SnxApp()
            }
        }
    }
}

@Composable
fun SnxApp(viewModel: SnxViewModel = viewModel()) {
    val context = LocalContext.current
    val networkMonitor = remember { NetworkMonitor(context) }
    var isOnline by remember { mutableStateOf(networkMonitor.isCurrentlyConnected()) }

    LaunchedEffect(networkMonitor) {
        networkMonitor.isOnlineFlow.collect { connected ->
            isOnline = connected
        }
    }

    val language by viewModel.language.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val activeModalGame by viewModel.activeModalGame.collectAsStateWithLifecycle()
    val isAuthModalOpen by viewModel.isAuthModalOpen.collectAsStateWithLifecycle()
    val authModalInitialTab by viewModel.authModalInitialTab.collectAsStateWithLifecycle()
    val isSupportModalOpen by viewModel.isSupportModalOpen.collectAsStateWithLifecycle()
    val isPromotionalAdsOpen by viewModel.isPromotionalAdsOpen.collectAsStateWithLifecycle()
    val isInsufficientBalanceModalOpen by viewModel.isInsufficientBalanceModalOpen.collectAsStateWithLifecycle()
    val pendingGameId by viewModel.pendingGameId.collectAsStateWithLifecycle()
    val jackpotPool by viewModel.jackpotPool.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CasinoBg,
        topBar = {
            AppHeader(
                userProfile = userProfile,
                language = language,
                onToggleLanguage = { viewModel.toggleLanguage() },
                onOpenAuth = { tab -> viewModel.openAuthModal(tab) },
                onOpenDeposit = {
                    if (!userProfile.isLoggedIn) {
                        viewModel.showToast(
                            if (language == AppLanguage.BN)
                                "ডিপোজিট করতে অনুগ্রহ করে প্রথমে বিনামূল্যে রেজিস্ট্রেশন করুন"
                            else
                                "Please register first to deposit"
                        )
                        viewModel.openAuthModal(1)
                    } else {
                        viewModel.setActiveTab(2)
                    }
                },
                onOpenSupport = { viewModel.openSupportModal() }
            )
        },
        bottomBar = {
            AppBottomBar(
                activeTab = activeTab,
                onTabSelected = { tab ->
                    if (!userProfile.isLoggedIn && tab != 0) {
                        viewModel.showToast(
                            if (language == AppLanguage.BN)
                                "এই অপশনটি চালু করতে অনুগ্রহ করে প্রথমে বিনামূল্যে একাউন্ট রেজিস্ট্রেশন করুন"
                            else
                                "Please register first to access this section"
                        )
                        viewModel.openAuthModal(1)
                    } else {
                        viewModel.setActiveTab(tab)
                    }
                },
                language = language
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Screens based on active tab
            when (activeTab) {
                0 -> HomeScreen(
                    games = viewModel.allGames,
                    selectedCategory = selectedCategory,
                    onSelectCategory = { viewModel.setCategory(it) },
                    jackpotPool = jackpotPool,
                    language = language,
                    userProfile = userProfile,
                    onOpenAuth = { tab -> viewModel.openAuthModal(tab) },
                    onOpenGame = { viewModel.openGame(it) },
                    onOpenDeposit = { viewModel.setActiveTab(2) },
                    onOpenWithdraw = { viewModel.setActiveTab(3) },
                    onClaimDailyBonus = { viewModel.claimDailyBonus() },
                    onOpenWheel = { viewModel.openGame("lucky_wheel") },
                    onShare = { viewModel.setActiveTab(4) },
                    onShowToast = { viewModel.showToast(it) },
                    onClaimCommission = { viewModel.claimReferralCommission() },
                    onApplyCoupon = { viewModel.applyDiscountCoupon(it) },
                    onInviteShared = { viewModel.recordFriendInvite() }
                )
                1 -> GamesScreen(
                    games = viewModel.allGames,
                    selectedCategory = selectedCategory,
                    onSelectCategory = { viewModel.setCategory(it) },
                    language = language,
                    onOpenGame = { viewModel.openGame(it) },
                    userProfile = userProfile,
                    onOpenAuth = { tab -> viewModel.openAuthModal(tab) }
                )
                2 -> DepositScreen(
                    userProfile = userProfile,
                    language = language,
                    onSubmitDeposit = { method, amount, phone, trx ->
                        viewModel.submitDeposit(method, amount, phone, trx)
                    },
                    onShowToast = { viewModel.showToast(it) },
                    onOpenAuth = { tab -> viewModel.openAuthModal(tab) }
                )
                3 -> WithdrawScreen(
                    userProfile = userProfile,
                    transactions = transactions,
                    language = language,
                    onSubmitWithdrawal = { method, amount, phone ->
                        viewModel.submitWithdrawal(method, amount, phone)
                    },
                    onOpenAuth = { tab -> viewModel.openAuthModal(tab) }
                )
                4 -> ProfileScreen(
                    userProfile = userProfile,
                    transactions = transactions,
                    language = language,
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onOpenAuth = { tab -> viewModel.openAuthModal(tab) },
                    onLogout = { viewModel.logout() },
                    onClaimDailyBonus = { viewModel.claimDailyBonus() },
                    onOpenSupport = { viewModel.openSupportModal() },
                    onShowToast = { viewModel.showToast(it) },
                    onClaimCommission = { viewModel.claimReferralCommission() },
                    onApplyCoupon = { viewModel.applyDiscountCoupon(it) }
                )
            }

            // In-app interactive game modals
            SlotMachineGame(
                isOpen = activeModalGame == "slot_777" || activeModalGame == "gold_rush_slot" || activeModalGame == "fruit_frenzy",
                currentBalance = userProfile.balanceBDT,
                language = language,
                onBalanceChange = { viewModel.adjustBalance(it) },
                onDismiss = { viewModel.closeGame() }
            )

            AviatorCrashGame(
                isOpen = activeModalGame == "aviator_crash",
                currentBalance = userProfile.balanceBDT,
                language = language,
                onBalanceChange = { viewModel.adjustBalance(it) },
                onDismiss = { viewModel.closeGame() }
            )

            LuckyWheelGame(
                isOpen = activeModalGame == "lucky_wheel",
                currentBalance = userProfile.balanceBDT,
                language = language,
                lastDailySpinTimestamp = userProfile.lastDailySpinTimestamp,
                onAwardBonus = { viewModel.awardDailySpinBonus(it) },
                onDismiss = { viewModel.closeGame() }
            )

            CricketBettingGame(
                isOpen = activeModalGame == "cricket_live",
                currentBalance = userProfile.balanceBDT,
                language = language,
                onBalanceChange = { viewModel.adjustBalance(it) },
                onDismiss = { viewModel.closeGame() }
            )

            TeenPattiGame(
                isOpen = activeModalGame == "teen_patti" || activeModalGame == "roulette_pro" || activeModalGame == "crazy_time" || activeModalGame == "fish_hunter",
                currentBalance = userProfile.balanceBDT,
                language = language,
                onBalanceChange = { viewModel.adjustBalance(it) },
                onDismiss = { viewModel.closeGame() }
            )

            DragonTigerGame(
                isOpen = activeModalGame == "dragon_tiger",
                currentBalance = userProfile.balanceBDT,
                language = language,
                onBalanceChange = { viewModel.adjustBalance(it) },
                onDismiss = { viewModel.closeGame() }
            )

            DiceRollGame(
                isOpen = activeModalGame == "dice_roll",
                currentBalance = userProfile.balanceBDT,
                language = language,
                onBalanceChange = { viewModel.adjustBalance(it) },
                onDismiss = { viewModel.closeGame() }
            )

            // Auth Modal (Login / Register) - opens on user action
            AuthModal(
                isOpen = isAuthModalOpen,
                initialTab = authModalInitialTab,
                language = language,
                canDismiss = true,
                onDismiss = { viewModel.closeAuthModal() },
                onLogin = { p, pw -> viewModel.login(p, pw) },
                onRegister = { u, p, pw, r -> viewModel.register(u, p, pw, r) }
            )

            // Customer Live Support Modal
            CustomerSupportModal(
                isOpen = isSupportModalOpen,
                language = language,
                onDismiss = { viewModel.closeSupportModal() }
            )

            // Promotional Entry Ads Pop-up
            PromotionalAdsModal(
                isOpen = isPromotionalAdsOpen,
                language = language,
                onDismiss = { viewModel.closePromotionalAds() },
                onOpenRegister = {
                    viewModel.closePromotionalAds()
                    viewModel.openAuthModal(1)
                },
                onGoToDeposit = {
                    viewModel.closePromotionalAds()
                    if (!userProfile.isLoggedIn) {
                        viewModel.openAuthModal(1)
                    } else {
                        viewModel.setActiveTab(2)
                    }
                }
            )

            // Insufficient Balance & Deposit Alert Modal
            InsufficientBalanceModal(
                isOpen = isInsufficientBalanceModalOpen,
                language = language,
                gameName = pendingGameId ?: "",
                currentBalance = userProfile.balanceBDT,
                onGoToDeposit = { viewModel.goToDepositFromGameModal() },
                onDismiss = { viewModel.closeInsufficientBalanceModal() }
            )

            // Floating Toast notification
            AnimatedVisibility(
                visible = toastMessage != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp, start = 16.dp, end = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Slate800,
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(GoldPrimary.copy(alpha = 0.5f), CasinoBorderSubtle))),
                    shadowElevation = 8.dp,
                    modifier = Modifier.testTag("app_toast_message")
                ) {
                    Text(
                        text = toastMessage ?: "",
                        color = Slate100,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            // Strict Full-Screen Blocking Offline Overlay
            // Completely blocks user interaction when internet/network connection is off
            NoInternetOverlay(
                isOffline = !isOnline,
                language = language,
                onRetry = {
                    val rechecked = networkMonitor.isCurrentlyConnected()
                    isOnline = rechecked
                    if (!rechecked) {
                        viewModel.showToast(
                            if (language == AppLanguage.BN)
                                "কোনো ইন্টারনেট সংযোগ পাওয়া যায়নি! অনুগ্রহ করে ডাটা বা ওয়াইফাই চালু করুন।"
                            else
                                "No internet connection detected! Please turn on mobile data or Wi-Fi."
                        )
                    } else {
                        viewModel.showToast(
                            if (language == AppLanguage.BN) "ইন্টারনেট সংযোগ চালু হয়েছে!"
                            else "Internet connection restored!"
                        )
                    }
                }
            )
        }
    }
}

// Retained for Screenshot Test compatibility
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        SnxApp()
    }
}
