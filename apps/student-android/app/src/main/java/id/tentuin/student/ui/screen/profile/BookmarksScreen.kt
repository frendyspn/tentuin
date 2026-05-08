package id.tentuin.student.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import id.tentuin.student.data.model.UniversityBookmark
import id.tentuin.student.ui.navigation.Route
import id.tentuin.student.ui.theme.Background
import id.tentuin.student.ui.theme.Border
import id.tentuin.student.ui.theme.Error
import id.tentuin.student.ui.theme.Primary
import id.tentuin.student.ui.theme.Surface
import id.tentuin.student.ui.theme.TentuinTypography
import id.tentuin.student.ui.theme.TextMuted
import id.tentuin.student.ui.theme.TextPrimary
import id.tentuin.student.ui.theme.TextSub

@Composable
fun BookmarksScreen(
    navController: NavController,
    viewModel: BookmarksViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        // Top bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface)
                .padding(vertical = 8.dp),
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = TextPrimary)
            }
            Text(
                text = "Bookmark Saya",
                style = TentuinTypography.titleLarge,
                color = TextPrimary,
            )
        }

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }

            state.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp),
                    ) {
                        Text(
                            text = state.error!!,
                            style = TentuinTypography.bodyMedium,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(16.dp))
                        TextButton(onClick = viewModel::loadBookmarks) {
                            Text("Coba Lagi", color = Primary)
                        }
                    }
                }
            }

            state.bookmarks.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(56.dp),
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Belum ada bookmark",
                            style = TentuinTypography.titleMedium,
                            color = TextPrimary,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Simpan universitas favoritmu dari halaman Explore",
                            style = TentuinTypography.bodyMedium,
                            color = TextSub,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 40.dp),
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.bookmarks, key = { it.id }) { bookmark ->
                        BookmarkCard(
                            bookmark = bookmark,
                            onCardClick = {
                                val uniId = bookmark.university?.id ?: bookmark.universityId
                                navController.navigate(Route.UniversityDetail.createRoute(uniId))
                            },
                            onRemove = { viewModel.removeBookmark(bookmark.universityId) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkCard(
    bookmark: UniversityBookmark,
    onCardClick: () -> Unit,
    onRemove: () -> Unit,
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    val uni = bookmark.university

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Hapus Bookmark?") },
            text = {
                Text(
                    "Hapus ${uni?.name ?: "universitas ini"} dari bookmark kamu?",
                    style = TentuinTypography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        onRemove()
                    },
                ) {
                    Text("Hapus", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Batal")
                }
            },
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp),
        ) {
            // University logo or placeholder
            if (!uni?.logoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = uni?.logoUrl,
                    contentDescription = uni?.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = uni?.name?.take(1)?.uppercase() ?: "U",
                        style = TentuinTypography.titleMedium,
                        color = Primary,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    text = uni?.name ?: bookmark.universityId,
                    style = TentuinTypography.titleSmall,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!uni?.city.isNullOrBlank() || !uni?.province.isNullOrBlank()) {
                    val location = listOfNotNull(uni?.city, uni?.province).joinToString(", ")
                    Text(
                        text = location,
                        style = TentuinTypography.bodySmall,
                        color = TextSub,
                    )
                }
                uni?.type?.let { type ->
                    Text(
                        text = type,
                        style = TentuinTypography.labelSmall,
                        color = Primary,
                    )
                }
            }

            IconButton(onClick = { showConfirmDialog = true }) {
                Icon(
                    Icons.Default.BookmarkRemove,
                    contentDescription = "Hapus bookmark",
                    tint = Error,
                )
            }
        }
    }
}
