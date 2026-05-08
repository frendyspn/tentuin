package id.tentuin.student.ui.screen.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import id.tentuin.student.data.model.MajorRow
import id.tentuin.student.ui.theme.Background
import id.tentuin.student.ui.theme.Border
import id.tentuin.student.ui.theme.Primary
import id.tentuin.student.ui.theme.Surface
import id.tentuin.student.ui.theme.TentuinTypography
import id.tentuin.student.ui.theme.TextPrimary
import id.tentuin.student.ui.theme.TextSub

@Composable
fun UniversityDetailScreen(
    navController: NavController,
    universityId: String,
    viewModel: UniversityDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(universityId) {
        viewModel.loadDetail(universityId)
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Primary)
        } else if (state.university != null) {
            val uni = state.university!!
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    UniversityHeader(
                        imageUrl = uni.logoUrl,
                        name = uni.name,
                        city = uni.city,
                        isPartner = uni.isPartner,
                        onBack = { navController.popBackStack() },
                        isBookmarked = state.isBookmarked,
                        onBookmark = { viewModel.toggleBookmark() }
                    )
                }

                item {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Tentang Kampus", style = TentuinTypography.titleMedium, color = TextPrimary)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = uni.description ?: "Tidak ada deskripsi tersedia.",
                            style = TentuinTypography.bodyMedium,
                            color = TextSub
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(text = "Daftar Jurusan", style = TentuinTypography.titleMedium, color = TextPrimary)
                        Spacer(Modifier.height(12.dp))
                    }
                }

                val majors = uni.majors
                if (majors.isEmpty()) {
                    item {
                        Text(
                            text = "Belum ada data jurusan.",
                            style = TentuinTypography.bodySmall,
                            color = TextSub,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                } else {
                    items(majors) { major ->
                        MajorItem(major = major)
                    }
                }
            }
        }
    }
}

@Composable
private fun UniversityHeader(
    imageUrl: String?,
    name: String,
    city: String?,
    isPartner: Boolean,
    onBack: () -> Unit,
    isBookmarked: Boolean,
    onBookmark: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onBookmark) {
                    Icon(
                        if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) Primary else TextSub
                    )
                }
            }

            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Background),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    style = TentuinTypography.headlineSmall,
                    color = TextPrimary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                if (isPartner) {
                    Spacer(Modifier.size(4.dp))
                    Icon(Icons.Default.Verified, contentDescription = "Partner", tint = Primary, modifier = Modifier.size(20.dp))
                }
            }
            if (!city.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSub, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(4.dp))
                    Text(text = city, style = TentuinTypography.bodyMedium, color = TextSub)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MajorItem(major: MajorRow) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = major.name, style = TentuinTypography.titleSmall, color = TextPrimary)
                if (!major.faculty.isNullOrBlank()) {
                    Text(text = major.faculty, style = TentuinTypography.bodySmall, color = TextSub)
                }
            }
            if (!major.riasecCodes.isNullOrEmpty()) {
                Text(
                    text = major.riasecCodes.joinToString(""),
                    style = TentuinTypography.labelMedium,
                    color = Primary,
                    modifier = Modifier
                        .background(Primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = Border)
    }
}
