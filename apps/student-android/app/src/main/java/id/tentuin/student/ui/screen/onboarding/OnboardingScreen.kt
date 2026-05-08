package id.tentuin.student.ui.screen.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.student.ui.component.TentuinButton
import id.tentuin.student.ui.component.TentuinTextButton
import id.tentuin.student.ui.navigation.Route
import id.tentuin.student.ui.theme.Background
import id.tentuin.student.ui.theme.Primary
import id.tentuin.student.ui.theme.PrimaryLight
import id.tentuin.student.ui.theme.TentuinTypography
import id.tentuin.student.ui.theme.TextMuted
import id.tentuin.student.ui.theme.TextPrimary
import id.tentuin.student.ui.theme.TextSub
import kotlinx.coroutines.launch

private data class OnboardingSlide(
    val icon: ImageVector,
    val iconBg: Color,
    val title: String,
    val subtitle: String,
)

private val slides = listOf(
    OnboardingSlide(
        icon = Icons.Default.AutoAwesome,
        iconBg = Color(0xFFEEEDFF),
        title = "Kenali Kepribadianmu",
        subtitle = "Ikuti tes RIASEC dan temukan jurusan kuliah yang paling sesuai dengan karakter dan minatmu.",
    ),
    OnboardingSlide(
        icon = Icons.Default.Explore,
        iconBg = Color(0xFFD1FAE5),
        title = "Jelajahi Ribuan Jurusan",
        subtitle = "Cari universitas dan jurusan impianmu dari ratusan pilihan di seluruh Indonesia.",
    ),
    OnboardingSlide(
        icon = Icons.Default.School,
        iconBg = Color(0xFFFFEDD5),
        title = "Raih Masa Depanmu",
        subtitle = "Simpan jurusan favorit dan mulai rencanakan perjalanan kariermu bersama Tentuin.",
    ),
)

@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val pagerState  = rememberPagerState(pageCount = { slides.size })
    val scope       = rememberCoroutineScope()
    val isLastPage  = pagerState.currentPage == slides.lastIndex

    fun finishOnboarding() {
        viewModel.completeOnboarding {
            navController.navigate(Route.Login.route) {
                popUpTo(Route.Onboarding.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        // Skip button
        Box(
            contentAlignment = Alignment.CenterEnd,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            if (!isLastPage) {
                TentuinTextButton(text = "Lewati", onClick = { finishOnboarding() })
            }
        }

        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { page ->
            SlideContent(slide = slides[page])
        }

        // Dots + button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
        ) {
            // Dot indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(slides.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 28.dp else 6.dp,
                        animationSpec = tween(300),
                        label = "dot_width",
                    )
                    val color by animateColorAsState(
                        targetValue = if (isSelected) Primary else Primary.copy(alpha = 0.25f),
                        animationSpec = tween(300),
                        label = "dot_color",
                    )
                    Box(
                        modifier = Modifier
                            .width(width)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(color),
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            TentuinButton(
                text = if (isLastPage) "Mulai" else "Lanjut",
                onClick = {
                    if (isLastPage) {
                        finishOnboarding()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SlideContent(slide: OnboardingSlide) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(slide.iconBg),
        ) {
            Icon(
                imageVector = slide.icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(56.dp),
            )
        }

        Spacer(Modifier.height(40.dp))

        Text(
            text = slide.title,
            style = TentuinTypography.headlineMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = slide.subtitle,
            style = TentuinTypography.bodyMedium,
            color = TextSub,
            textAlign = TextAlign.Center,
        )
    }
}
