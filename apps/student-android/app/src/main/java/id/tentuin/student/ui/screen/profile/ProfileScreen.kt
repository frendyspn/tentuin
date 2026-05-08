package id.tentuin.student.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
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
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface)
                .statusBarsPadding()
                .padding(top = 24.dp, bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (state.isGuest) {
                    GuestHeader(onLoginClick = { navController.navigate(Route.Login.route) })
                } else {
                    UserHeader(state)
                }
            }
        }

        if (!state.isGuest) {
            // Stats Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface)
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "Hasil Test", value = state.testCount.toString())
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Border))
                StatItem(label = "Bookmark", value = state.bookmarkCount.toString())
            }

            Spacer(Modifier.height(24.dp))
        }

        // Menu Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Surface)
        ) {
            if (!state.isGuest) {
                MenuItem(
                    icon = Icons.Default.Edit,
                    label = "Edit Profil",
                    onClick = { navController.navigate(Route.EditProfile.route) }
                )
                HorizontalDivider(color = Border, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                MenuItem(
                    icon = Icons.Default.BookmarkBorder,
                    label = "Bookmark Saya",
                    onClick = { navController.navigate(Route.Bookmarks.route) }
                )
                HorizontalDivider(color = Border, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                MenuItem(
                    icon = Icons.Default.History,
                    label = "Riwayat Test",
                    onClick = { navController.navigate(Route.TestHistory.route) }
                )
                HorizontalDivider(color = Border, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
            }

            MenuItem(
                icon = Icons.Default.Logout,
                label = if (state.isGuest) "Masuk / Daftar" else "Keluar",
                color = if (state.isGuest) Primary else Error,
                onClick = {
                    if (state.isGuest) {
                        navController.navigate(Route.Login.route)
                    } else {
                        viewModel.logout()
                        navController.navigate(Route.Splash.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
        
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun GuestHeader(onLoginClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(Primary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(40.dp))
    }
    Spacer(Modifier.height(16.dp))
    Text(text = "Belum Masuk", style = TentuinTypography.headlineSmall, color = TextPrimary)
    Text(
        text = "Masuk untuk menyimpan hasil test dan bookmark",
        style = TentuinTypography.bodyMedium,
        color = TextSub,
        modifier = Modifier.padding(horizontal = 40.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
    Spacer(Modifier.height(16.dp))
    Button(
        onClick = onLoginClick,
        colors = ButtonDefaults.buttonColors(containerColor = Primary),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = "Masuk Sekarang")
    }
}

@Composable
private fun UserHeader(state: ProfileUiState) {
    if (!state.profile?.avatarUrl.isNullOrBlank()) {
        AsyncImage(
            model = state.profile?.avatarUrl,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.profile?.fullName?.take(1)?.uppercase() ?: "U",
                style = TentuinTypography.displayMedium,
                color = Primary
            )
        }
    }
    Spacer(Modifier.height(16.dp))
    Text(text = state.profile?.fullName ?: "User", style = TentuinTypography.headlineSmall, color = TextPrimary)
    Text(text = state.profile?.schoolName ?: "Siswa", style = TentuinTypography.bodyMedium, color = TextSub)
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = TentuinTypography.headlineSmall, color = Primary)
        Text(text = label, style = TentuinTypography.labelSmall, color = TextMuted)
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color = TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(text = label, style = TentuinTypography.titleMedium, color = color, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
    }
}
