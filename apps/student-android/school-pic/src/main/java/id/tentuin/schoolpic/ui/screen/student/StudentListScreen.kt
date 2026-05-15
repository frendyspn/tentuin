package id.tentuin.schoolpic.ui.screen.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import id.tentuin.schoolpic.data.model.Profile
import id.tentuin.schoolpic.ui.component.EmptyState
import id.tentuin.schoolpic.ui.component.ErrorBanner
import id.tentuin.schoolpic.ui.theme.*

@Composable
fun StudentListScreen(
    navController: NavController,
    viewModel: StudentListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        Surface(color = Primary, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Daftar Siswa", color = Color.White, style = TentuinSchoolPicTypography.headlineMedium)
                Text(
                    "${state.items.size} siswa terdaftar",
                    color = Color.White.copy(alpha = 0.85f),
                    style = TentuinSchoolPicTypography.bodyMedium,
                )
            }
        }

        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            label = { Text("Cari nama siswa") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )

        if (state.error != null) {
            ErrorBanner(state.error!!, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))
        }

        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = Primary) }

            state.items.isEmpty() -> EmptyState(
                title = if (state.query.isBlank()) "Belum ada siswa terdaftar"
                        else "Tidak ada siswa dengan nama \"${state.query}\"",
                subtitle = if (state.query.isBlank())
                    "Siswa yang memilih sekolah Anda saat onboarding akan muncul di sini."
                else null,
            )

            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.items, key = { it.id }) { p -> StudentRow(p) }
            }
        }
    }
}

@Composable
private fun StudentRow(profile: Profile) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (profile.avatarUrl != null) {
            AsyncImage(
                model = profile.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceVar),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrimaryLight),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    (profile.fullName?.firstOrNull()?.uppercase() ?: "?"),
                    color = Primary,
                    style = TentuinSchoolPicTypography.titleMedium,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(profile.fullName ?: "(tanpa nama)",
                style = TentuinSchoolPicTypography.titleMedium, color = TextPrimary)
            val sub = listOfNotNull(profile.city, profile.birthYear?.toString())
                .joinToString(" • ").ifBlank { "—" }
            Text(sub, style = TentuinSchoolPicTypography.bodyMedium, color = TextSub)
        }
    }
}
