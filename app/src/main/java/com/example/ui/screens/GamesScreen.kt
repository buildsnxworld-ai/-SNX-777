package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.GameCategory
import com.example.model.GameItem
import com.example.model.UserProfile
import com.example.ui.theme.*
import com.example.util.StringRes

@Composable
fun GamesScreen(
    games: List<GameItem>,
    selectedCategory: GameCategory,
    onSelectCategory: (GameCategory) -> Unit,
    language: AppLanguage,
    onOpenGame: (String) -> Unit,
    userProfile: UserProfile? = null,
    onOpenAuth: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredGames = remember(selectedCategory, searchQuery, games) {
        games.filter { game ->
            val matchesCategory = (selectedCategory == GameCategory.ALL) ||
                    (game.category == selectedCategory) ||
                    (selectedCategory == GameCategory.HOT && game.badge != null)

            val matchesSearch = searchQuery.isBlank() ||
                    game.titleBn.contains(searchQuery, ignoreCase = true) ||
                    game.titleEn.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CasinoBg)
            .padding(horizontal = 10.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = StringRes.t(language, "গেমের নাম দিয়ে খুঁজুন...", "Search games..."),
                    fontSize = 13.sp,
                    color = Slate400
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = GoldLight)
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("games_search_field"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Slate800,
                unfocusedContainerColor = Slate800,
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = CasinoBorderSubtle,
                focusedTextColor = Slate100,
                unfocusedTextColor = Slate100
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Category Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(GameCategory.values()) { cat ->
                val isSelected = cat == selectedCategory
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) GoldPrimary else Slate800,
                    border = if (isSelected) null else CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CasinoBorderSubtle, CasinoBorderSubtle))),
                    modifier = Modifier.clickable { onSelectCategory(cat) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = cat.icon, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.BN) cat.bn else cat.en,
                            color = if (isSelected) Color.Black else Slate400,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid of filtered games
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .testTag("games_grid"),
            contentPadding = PaddingValues(bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredGames, key = { it.id }) { game ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            if (userProfile?.isLoggedIn != true) {
                                onOpenAuth?.invoke(1)
                            } else {
                                onOpenGame(game.id)
                            }
                        }
                        .testTag("game_item_${game.id}"),
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CasinoBorderSubtle, CasinoBorderSubtle)))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Slate900),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = game.iconEmoji, fontSize = 38.sp)
                            if (game.badge != null) {
                                Surface(
                                    shape = RoundedCornerShape(bottomStart = 8.dp),
                                    color = if (game.badge == "JACKPOT") GoldPrimary else AccentCrimson,
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Text(
                                        text = game.badge,
                                        color = if (game.badge == "JACKPOT") Color.Black else Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (language == AppLanguage.BN) game.titleBn else game.titleEn,
                            color = Slate100,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (language == AppLanguage.BN) "প্লেয়ার: ${game.playersCount}" else "Players: ${game.playersCount}",
                                color = Slate400,
                                fontSize = 9.sp
                            )
                            Text(
                                text = if (language == AppLanguage.BN) "মিনিমাম: ৳${game.minBet.toInt()}" else "Min: ৳${game.minBet.toInt()}",
                                color = GoldLight,
                                fontSize = 9.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { onOpenGame(game.id) },
                            modifier = Modifier.fillMaxWidth().height(30.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = StringRes.t(language, "খেলুন", "PLAY"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
