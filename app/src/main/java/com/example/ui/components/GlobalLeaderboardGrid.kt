package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.model.Country
import com.example.ui.theme.GoldCrown
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.Formatters

@Composable
fun GlobalLeaderboardGrid(
    countries: List<Country>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedContinent: String,
    onContinentSelect: (String) -> Unit,
    isGridView: Boolean,
    onCountryBoostClick: (String) -> Unit,
    activeCountryId: String,
    modifier: Modifier = Modifier
) {
    val continents = listOf("All", "Asia", "Europe", "Americas", "Africa", "Oceania")
    val maxScore = countries.maxOfOrNull { it.score }?.coerceAtLeast(1L) ?: 1L

    val filteredCountries = countries.filter { country ->
        val matchesSearch = country.name.contains(searchQuery, ignoreCase = true) ||
                country.id.contains(searchQuery, ignoreCase = true)
        val matchesContinent = selectedContinent == "All" || country.continent.equals(selectedContinent, ignoreCase = true)
        matchesSearch && matchesContinent
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Title & Search Row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GLOBAL LEADERBOARD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "${filteredCountries.size} Countries",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = "Search Turkey, India, USA...",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("country_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = SurfaceCardBorder,
                    focusedContainerColor = SurfaceVariantDark,
                    unfocusedContainerColor = SurfaceVariantDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Continent Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(continents) { continent ->
                    val isSelected = continent == selectedContinent
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) NeonCyan else SurfaceVariantDark,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) NeonCyan else SurfaceCardBorder
                        ),
                        modifier = Modifier
                            .clickable { onContinentSelect(continent) }
                            .testTag("filter_chip_$continent")
                    ) {
                        Text(
                            text = continent,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.Black else TextSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Leaderboard Grid / List View
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            val columns = if (maxWidth > 600.dp) 3 else 2

            if (isGridView) {
                // Grid of Country Cards
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    filteredCountries.chunked(columns).forEach { rowChunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowChunk.forEach { country ->
                                CountryGridCard(
                                    country = country,
                                    maxScore = maxScore,
                                    isUserCountry = country.id == activeCountryId,
                                    onBoostClick = { onCountryBoostClick(country.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill remaining empty slots in row if uneven
                            if (rowChunk.size < columns) {
                                repeat(columns - rowChunk.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            } else {
                // Ranked List View
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredCountries.forEach { country ->
                        CountryListRow(
                            country = country,
                            maxScore = maxScore,
                            isUserCountry = country.id == activeCountryId,
                            onBoostClick = { onCountryBoostClick(country.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CountryGridCard(
    country: Country,
    maxScore: Long,
    isUserCountry: Boolean,
    onBoostClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (country.score.toFloat() / maxScore.toFloat()).coerceIn(0.05f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(400),
        label = "score_progress"
    )

    val rankBorderColor = when (country.currentRank) {
        1 -> GoldCrown
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> if (isUserCountry) NeonCyan else SurfaceCardBorder
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onBoostClick)
            .testTag("country_card_${country.id}"),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceCard,
        border = androidx.compose.foundation.BorderStroke(
            if (isUserCountry || country.currentRank <= 3) 1.5.dp else 1.dp,
            rankBorderColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Header: Rank badge + Rank Shift + User Country indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (country.currentRank) {
                                1 -> GoldCrown
                                2 -> Color(0xFFC0C0C0)
                                3 -> Color(0xFFCD7F32)
                                else -> SurfaceVariantDark
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "#${country.currentRank}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (country.currentRank <= 3) Color.Black else TextPrimary
                    )
                }

                // Rank Change Indicator (▲ +2, ▼ -1, etc.)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when {
                        country.rankDelta > 0 -> {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Rank up",
                                tint = NeonGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "+${country.rankDelta}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonGreen
                            )
                        }
                        country.rankDelta < 0 -> {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Rank down",
                                tint = LiveRed,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${country.rankDelta}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiveRed
                            )
                        }
                        else -> {
                            Text(
                                text = "-",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }

                    if (isUserCountry) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "YOU",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Country Flag + Name
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = country.flag,
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = country.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Score counter
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = Formatters.formatPoints(country.score),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = NeonCyan
                )
                Text(
                    text = "points",
                    fontSize = 9.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Relative Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = when (country.currentRank) {
                    1 -> GoldCrown
                    2 -> Color(0xFFC0C0C0)
                    3 -> Color(0xFFCD7F32)
                    else -> NeonCyan
                },
                trackColor = SurfaceVariantDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Interactive Direct Boost Button
            Button(
                onClick = onBoostClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .testTag("boost_button_${country.id}"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isUserCountry) NeonCyan else SurfaceVariantDark,
                    contentColor = if (isUserCountry) Color.Black else TextPrimary
                ),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Boost",
                        modifier = Modifier.size(14.dp),
                        tint = if (isUserCountry) Color.Black else GoldCrown
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "BOOST +50",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun CountryListRow(
    country: Country,
    maxScore: Long,
    isUserCountry: Boolean,
    onBoostClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (country.score.toFloat() / maxScore.toFloat()).coerceIn(0.05f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(400),
        label = "row_score_progress"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onBoostClick)
            .testTag("country_row_${country.id}"),
        shape = RoundedCornerShape(14.dp),
        color = SurfaceCard,
        border = androidx.compose.foundation.BorderStroke(
            if (isUserCountry) 1.5.dp else 1.dp,
            if (isUserCountry) NeonCyan else SurfaceCardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Rank & Flag & Country Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Rank number
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            when (country.currentRank) {
                                1 -> GoldCrown
                                2 -> Color(0xFFC0C0C0)
                                3 -> Color(0xFFCD7F32)
                                else -> SurfaceVariantDark
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${country.currentRank}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (country.currentRank <= 3) Color.Black else TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Flag
                Text(text = country.flag, fontSize = 24.sp)

                Spacer(modifier = Modifier.width(10.dp))

                // Name & Chatter
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = country.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        if (isUserCountry) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NeonCyan.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "YOU",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonCyan
                                )
                            }
                        }
                    }
                    Text(
                        text = "Top: ${country.topChatter} (${country.topChatterChats} chats)",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            // Right: Points & Boost Action Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Formatters.formatPoints(country.score),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = NeonCyan
                    )
                    Text(
                        text = "points",
                        fontSize = 9.sp,
                        color = TextMuted
                    )
                }

                Button(
                    onClick = onBoostClick,
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("boost_row_button_${country.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isUserCountry) NeonCyan else SurfaceVariantDark,
                        contentColor = if (isUserCountry) Color.Black else TextPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Boost",
                        modifier = Modifier.size(13.dp),
                        tint = if (isUserCountry) Color.Black else GoldCrown
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "+50", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
