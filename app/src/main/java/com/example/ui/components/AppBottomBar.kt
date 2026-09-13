package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.ui.theme.*
import com.example.util.StringRes

@Composable
fun AppBottomBar(
    activeTab: Int,
    onTabSelected: (Int) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(CasinoSurface)
            .border(
                width = 1.dp,
                color = CasinoBorderSubtle,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // 0: Home
            BottomNavItem(
                icon = if (activeTab == 0) Icons.Default.Home else Icons.Outlined.Home,
                label = StringRes.t(language, "হোম", "Home"),
                isSelected = activeTab == 0,
                onClick = { onTabSelected(0) },
                testTag = "tab_home"
            )

            // 1: Games
            BottomNavItem(
                icon = if (activeTab == 1) Icons.Default.SportsEsports else Icons.Outlined.SportsEsports,
                label = StringRes.t(language, "গেমস", "Games"),
                isSelected = activeTab == 1,
                onClick = { onTabSelected(1) },
                testTag = "tab_games"
            )

            // 2: Deposit (Highlighted Center Button: amber-600 to yellow-400)
            Box(
                modifier = Modifier
                    .offset(y = (-10).dp)
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFD97706), Color(0xFFFACC15))
                        )
                    )
                    .border(2.dp, CasinoBg, CircleShape)
                    .clickable { onTabSelected(2) }
                    .testTag("tab_deposit"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Deposit",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = StringRes.t(language, "ডিপোজিট", "Deposit"),
                        color = Color.Black,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // 3: Withdraw
            BottomNavItem(
                icon = if (activeTab == 3) Icons.Default.Payments else Icons.Outlined.Payments,
                label = StringRes.t(language, "উত্তোলন", "Withdraw"),
                isSelected = activeTab == 3,
                onClick = { onTabSelected(3) },
                testTag = "tab_withdraw"
            )

            // 4: Profile
            BottomNavItem(
                icon = if (activeTab == 4) Icons.Default.Person else Icons.Outlined.Person,
                label = StringRes.t(language, "প্রোফাইল", "Profile"),
                isSelected = activeTab == 4,
                onClick = { onTabSelected(4) },
                testTag = "tab_profile"
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) GoldPrimary else Slate500,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) GoldPrimary else Slate500
        )
    }
}
