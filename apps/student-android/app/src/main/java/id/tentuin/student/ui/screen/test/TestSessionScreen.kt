package id.tentuin.student.ui.screen.test

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.student.ui.theme.riasecColor
import id.tentuin.student.core.util.riasecTypeName
import id.tentuin.student.ui.component.RiasecChip
import id.tentuin.student.ui.navigation.Route
import id.tentuin.student.ui.theme.Background
import id.tentuin.student.ui.theme.Border
import id.tentuin.student.ui.theme.Primary
import id.tentuin.student.ui.theme.Surface
import id.tentuin.student.ui.theme.TentuinTypography
import id.tentuin.student.ui.theme.TextMuted
import id.tentuin.student.ui.theme.TextPrimary
import id.tentuin.student.ui.theme.TextSub

private val answerSizes = listOf(44, 50, 56, 62, 68)

@Composable
fun TestSessionScreen(
    navController: NavController,
    viewModel: TestViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler { showExitDialog = true }

    LaunchedEffect(state.phase) {
        if (state.phase == TestPhase.DONE) {
            navController.navigate(Route.TestResult.createRoute(state.finalCode)) {
                popUpTo(Route.Test.route)
            }
        }
    }

    when (state.phase) {
        TestPhase.LOADING, TestPhase.SUBMITTING -> {
            Box(Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Primary)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (state.phase == TestPhase.LOADING) "Memuat soal..." else "Menyimpan hasil...",
                        style = TentuinTypography.bodyMedium,
                        color = TextSub,
                    )
                }
            }
        }
        TestPhase.ERROR -> {
            Box(Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Text(text = state.error ?: "Terjadi kesalahan", style = TentuinTypography.bodyMedium, color = TextPrimary, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = { viewModel.loadQuestions() }) {
                        Text("Coba Lagi", color = Primary)
                    }
                }
            }
        }
        else -> {
            val questions = state.questions
            if (questions.isEmpty()) return

            val current  = questions[state.currentIndex]
            val progress = (state.currentIndex + 1).toFloat() / questions.size
            val accentColor = riasecColor(current.category.firstOrNull()?.uppercase() ?: "R")

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
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                ) {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Keluar", tint = TextPrimary)
                    }
                    Text(
                        text = "${state.currentIndex + 1} / ${questions.size}",
                        style = TentuinTypography.labelLarge,
                        color = TextSub,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                    RiasecChip(
                        code = current.category.first().uppercase(),
                        modifier = Modifier.padding(end = 16.dp),
                    )
                }

                // Progress bar
                LinearProgressIndicator(
                    progress = { progress },
                    color = accentColor,
                    trackColor = accentColor.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                )

                // Question
                AnimatedContent(
                    targetState = state.currentIndex,
                    transitionSpec = {
                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                    },
                    label = "question_transition",
                    modifier = Modifier.weight(1f),
                ) { _ ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 28.dp),
                    ) {
                        Text(
                            text = current.text,
                            style = TentuinTypography.headlineSmall,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                // Answer buttons
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface)
                        .padding(vertical = 24.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    ) {
                        (1..5).forEach { value ->
                            val size   = answerSizes[value - 1].dp
                            val isSelected = state.answers[current.id] == value
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(size)
                                    .clip(CircleShape)
                                    .background(if (isSelected) accentColor else accentColor.copy(alpha = 0.10f))
                                    .border(
                                        width = if (isSelected) 0.dp else 1.5.dp,
                                        color = accentColor.copy(alpha = 0.4f),
                                        shape = CircleShape,
                                    )
                                    .clickable { viewModel.answer(current.id, value) },
                            ) {
                                Text(
                                    text = value.toString(),
                                    style = TentuinTypography.titleMedium,
                                    color = if (isSelected) Color.White else accentColor,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    ) {
                        Text(text = "Sangat Tidak Setuju", style = TentuinTypography.bodySmall, color = TextMuted)
                        Text(text = "Sangat Setuju",       style = TentuinTypography.bodySmall, color = TextMuted)
                    }
                    if (state.currentIndex > 0) {
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.previousQuestion() }) {
                            Text("← Soal sebelumnya", color = TextMuted, style = TentuinTypography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Keluar dari test?", style = TentuinTypography.titleLarge) },
            text  = { Text("Progresmu tidak akan tersimpan.", style = TentuinTypography.bodyMedium, color = TextSub) },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    navController.popBackStack()
                }) { Text("Keluar", color = id.tentuin.student.ui.theme.Error) }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("Lanjutkan", color = Primary) }
            },
            shape = RoundedCornerShape(16.dp),
        )
    }
}
