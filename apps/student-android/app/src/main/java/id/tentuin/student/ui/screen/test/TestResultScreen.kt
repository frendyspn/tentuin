package id.tentuin.student.ui.screen.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.student.ui.theme.riasecColor
import id.tentuin.student.core.util.riasecTypeDescription
import id.tentuin.student.core.util.riasecTypeName
import id.tentuin.student.ui.component.RiasecChip
import id.tentuin.student.ui.component.TentuinButton
import id.tentuin.student.ui.theme.Background
import id.tentuin.student.ui.theme.Border
import id.tentuin.student.ui.theme.Surface
import id.tentuin.student.ui.theme.TentuinTypography
import id.tentuin.student.ui.theme.TextPrimary
import id.tentuin.student.ui.theme.TextSub

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import id.tentuin.student.ui.theme.Primary
import id.tentuin.student.ui.theme.TextMuted
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.LaunchedEffect
import id.tentuin.student.ui.component.UniversityCard
import id.tentuin.student.ui.component.SkeletonBox

@Composable
fun TestResultScreen(
    navController: NavController,
    riasecCode: String,
    isHistorical: Boolean = false,
    viewModel: TestViewModel = hiltViewModel(),
    recommendationViewModel: TestResultViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val recommendationState by recommendationViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val codes = remember(riasecCode) { riasecCode.map { it.toString() } }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(riasecCode) {
        recommendationViewModel.loadRecommendations(riasecCode)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface)
                    .statusBarsPadding()
                    .padding(vertical = 8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = TextPrimary)
                    }
                    Text(
                        text = if (isHistorical) "Hasil Test" else "Hasil Test Kamu",
                        style = TentuinTypography.titleLarge,
                        color = TextPrimary,
                    )
                }
            }
        }

        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            ) {
                Text(
                    text = "Tipe Kepribadian Dominan:",
                    style = TentuinTypography.bodyMedium,
                    color = TextSub,
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    codes.forEach { code ->
                        RiasecChip(
                            code = code, 
                            modifier = Modifier.size(64.dp),
                            textStyle = TentuinTypography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = riasecCode,
                    style = TentuinTypography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                )
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Interpretasi RIASEC",
                    style = TentuinTypography.titleMedium,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(12.dp))

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Background,
                    contentColor = Primary,
                    divider = { HorizontalDivider(color = Border) },
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Primary
                        )
                    }
                ) {
                    codes.forEachIndexed { index, code ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = "$code - ${riasecTypeName(code)}",
                                    style = TentuinTypography.titleSmall,
                                    color = if (selectedTabIndex == index) Primary else TextMuted
                                )
                            }
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    text = riasecTypeDescription(codes[selectedTabIndex]),
                    style = TentuinTypography.bodyMedium,
                    color = TextSub,
                    lineHeight = 22.sp,
                )
                
                Spacer(Modifier.height(8.dp))
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                TentuinButton(
                    text = if (uiState.isGeneratingShareCard) "Membuat kartu..." else "Bagikan Hasilku 🎉",
                    onClick = { viewModel.shareResult(context) },
                    isLoading = uiState.isGeneratingShareCard,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color(0xFF10B981), // Success green
                )
                Spacer(Modifier.height(24.dp))
            }
        }

        item {
            Text(
                text = "Rekomendasi Jurusan & Kampus",
                style = TentuinTypography.titleLarge,
                color = TextPrimary,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
            )
        }

        if (recommendationState.loadingRecommendations) {
            items(3) {
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }
        } else {
            item {
                Text(
                    text = "Pilih maksimal 3 jurusan untuk filter kampus di bawah:",
                    style = TentuinTypography.bodySmall,
                    color = TextSub,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
                )
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    items(recommendationState.majors, key = { it.id }) { major ->
                        val isSelected = recommendationState.selectedMajorNames.contains(major.name)
                        FilterChip(
                            selected = isSelected,
                            onClick = { recommendationViewModel.toggleMajor(major.name) },
                            label = { Text(major.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            if (recommendationState.filteredUniversities.isEmpty()) {
                item {
                    Text(
                        text = "Tidak ada kampus yang ditemukan untuk jurusan yang dipilih.",
                        style = TentuinTypography.bodyMedium,
                        color = TextSub,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
            } else {
                items(
                    count = recommendationState.filteredUniversities.size,
                    key = { index -> recommendationState.filteredUniversities[index].id },
                    contentType = { "university_card" }
                ) { index ->
                    val uni = recommendationState.filteredUniversities[index]
                    val isBookmarked = recommendationState.bookmarkedUniversityIds.contains(uni.id)
                    // Note: UniversityCard does not currently accept isBookmarked/onBookmarkClick
                    // If you want to support bookmarking here without changing UniversityCard,
                    // we could wrap it in a Box with a bookmark icon. Let's do that!
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                        UniversityCard(
                            university = uni,
                            onClick = { navController.navigate(id.tentuin.student.ui.navigation.Route.UniversityDetail.createRoute(uni.id)) }
                        )
                        if (!recommendationState.isGuest) {
                            IconButton(
                                onClick = { recommendationViewModel.toggleBookmark(uni.id) },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) androidx.compose.material.icons.Icons.Default.Bookmark else androidx.compose.material.icons.Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (isBookmarked) Primary else TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(32.dp))
        }
    }
}
