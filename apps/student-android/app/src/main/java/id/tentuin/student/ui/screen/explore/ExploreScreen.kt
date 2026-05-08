package id.tentuin.student.ui.screen.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.student.data.model.ExploreTab
import id.tentuin.student.ui.component.EmptyState
import id.tentuin.student.ui.component.MajorCard
import id.tentuin.student.ui.component.SkeletonBox
import id.tentuin.student.ui.component.UniversityCard
import id.tentuin.student.ui.navigation.Route
import id.tentuin.student.ui.theme.Background
import id.tentuin.student.ui.theme.Border
import id.tentuin.student.ui.theme.Primary
import id.tentuin.student.ui.theme.Surface
import id.tentuin.student.ui.theme.TentuinTypography
import id.tentuin.student.ui.theme.TextMuted
import id.tentuin.student.ui.theme.TextSub

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    navController: NavController,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Infinite scroll detection
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
            lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadMoreVisibleItems()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Search Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface)
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 8.dp)
        ) {
            TextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchChanged,
                placeholder = {
                    Text(
                        text = if (state.activeTab == ExploreTab.UNIVERSITIES) "Cari universitas atau kota..." else "Cari jurusan...",
                        style = TentuinTypography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted)
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = TextMuted)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Background,
                    unfocusedContainerColor = Background,
                    disabledContainerColor = Background,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                singleLine = true
            )
        }

        // Tabs
        TabRow(
            selectedTabIndex = state.activeTab.ordinal,
            containerColor = Surface,
            contentColor = Primary,
            divider = { Box(Modifier.fillMaxWidth().height(1.dp).background(Border)) },
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[state.activeTab.ordinal]),
                    color = Primary
                )
            }
        ) {
            ExploreTab.entries.forEach { tab ->
                Tab(
                    selected = state.activeTab == tab,
                    onClick = { viewModel.onTabChanged(tab) },
                    text = {
                        Text(
                            text = if (tab == ExploreTab.UNIVERSITIES) "Universitas" else "Jurusan",
                            style = TentuinTypography.titleMedium,
                            color = if (state.activeTab == tab) Primary else TextMuted
                        )
                    }
                )
            }
        }

        // Filters
        FilterSection(state, viewModel)

        // Result Count
        Text(
            text = if (state.activeTab == ExploreTab.UNIVERSITIES) "${state.filteredUniversitiesCount} kampus ditemukan" 
                   else "${state.filteredMajorsCount} jurusan ditemukan",
            style = TentuinTypography.labelMedium,
            color = TextSub,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )

        // List
        if (state.isEmptyVisible) {
            EmptyState(
                title = "Tidak ada hasil",
                subtitle = "Coba ubah kata kunci atau filter pencarianmu"
            )
        } else {
            LazyColumn(
                state = listState,
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp, 0.dp, 20.dp, 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (state.isLoadingVisible) {
                    items(5) { SkeletonBox(modifier = Modifier.fillMaxWidth().height(80.dp)) }
                } else {
                    if (state.activeTab == ExploreTab.UNIVERSITIES) {
                        items(state.visibleUniversities, key = { it.id }) { university ->
                            UniversityCard(
                                university = university,
                                onClick = { navController.navigate(Route.UniversityDetail.createRoute(university.id)) }
                            )
                        }
                    } else {
                        items(state.visibleMajors, key = { it.id }) { major ->
                            MajorCard(
                                major = major,
                                onClick = { /* Navigate to Major Detail if exists */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSection(state: ExploreUiState, viewModel: ExploreViewModel) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface)
            .padding(vertical = 8.dp)
    ) {
        if (state.activeTab == ExploreTab.UNIVERSITIES) {
            val types = listOf("Semua", "Negeri", "Swasta")
            items(types) { type ->
                FilterChip(
                    selected = state.typeFilter == type,
                    onClick = { viewModel.onUniversityFilterChanged(type) },
                    label = { Text(type) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        } else {
            val riasecs = listOf("Semua", "R", "I", "A", "S", "E", "C")
            items(riasecs) { code ->
                val label = if (code == "Semua") "Semua" else code
                FilterChip(
                    selected = if (code == "Semua") state.riasecCode == null else state.riasecCode == code,
                    onClick = { viewModel.onRiasecFilterChanged(if (code == "Semua") null else code) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}
