package id.tentuin.student.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.student.ui.component.TentuinButton
import id.tentuin.student.ui.component.TentuinTextField
import id.tentuin.student.ui.theme.Background
import id.tentuin.student.ui.theme.Primary
import id.tentuin.student.ui.theme.Surface
import id.tentuin.student.ui.theme.TentuinTypography
import id.tentuin.student.ui.theme.TextMuted
import id.tentuin.student.ui.theme.TextPrimary

@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error in snackbar
    LaunchedEffect(state.error) {
        if (state.error != null) {
            snackbarHostState.showSnackbar(state.error!!)
            viewModel.clearError()
        }
    }

    // Show success feedback
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            snackbarHostState.showSnackbar("Profil berhasil disimpan")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
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
                    text = "Edit Profil",
                    style = TentuinTypography.titleLarge,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                if (state.isSaved) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Tersimpan",
                        tint = Primary,
                        modifier = Modifier.padding(end = 16.dp),
                    )
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Email (read-only)
                    state.profile?.let {
                        Text(
                            text = "Email (tidak dapat diubah)",
                            style = TentuinTypography.labelMedium,
                            color = TextMuted,
                        )
                        Text(
                            text = it.id, // email shown would require AuthUser, use id as fallback display
                            style = TentuinTypography.bodyMedium,
                            color = TextMuted,
                        )
                        Spacer(Modifier.height(4.dp))
                    }

                    TentuinTextField(
                        value = state.fullName,
                        onValueChange = viewModel::onFullNameChange,
                        label = "Nama Lengkap",
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next,
                        ),
                    )

                    // Nama Sekolah (Auto-complete)
                    Text(text = "Nama Sekolah (minimal 5 karakter)", style = TentuinTypography.labelMedium, color = TextMuted)
                    var schoolExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = schoolExpanded,
                        onExpandedChange = { if (state.schoolOptions.isNotEmpty()) schoolExpanded = !schoolExpanded }
                    ) {
                        OutlinedTextField(
                            value = state.schoolName,
                            onValueChange = {
                                viewModel.onSchoolNameChange(it)
                                if (it.length >= 5) schoolExpanded = true else schoolExpanded = false
                            },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = schoolExpanded) },
                            colors = OutlinedTextFieldDefaults.colors()
                        )
                        if (state.schoolOptions.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = schoolExpanded,
                                onDismissRequest = { schoolExpanded = false }
                            ) {
                                state.schoolOptions.forEach { school ->
                                    DropdownMenuItem(
                                        text = { Text(school) },
                                        onClick = {
                                            viewModel.onSchoolNameChange(school)
                                            schoolExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Kota (Auto-complete)
                    Text(text = "Kota (minimal 5 karakter)", style = TentuinTypography.labelMedium, color = TextMuted)
                    var cityExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = cityExpanded,
                        onExpandedChange = { if (state.cityOptions.isNotEmpty()) cityExpanded = !cityExpanded }
                    ) {
                        OutlinedTextField(
                            value = state.city,
                            onValueChange = {
                                viewModel.onCityChange(it)
                                if (it.length >= 5) cityExpanded = true else cityExpanded = false
                            },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
                            colors = OutlinedTextFieldDefaults.colors()
                        )
                        if (state.cityOptions.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = cityExpanded,
                                onDismissRequest = { cityExpanded = false }
                            ) {
                                state.cityOptions.forEach { city ->
                                    DropdownMenuItem(
                                        text = { Text(city) },
                                        onClick = {
                                            viewModel.onCityChange(city)
                                            cityExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Kelas
                    Text(text = "Kelas", style = TentuinTypography.labelMedium, color = TextMuted)
                    var gradeExpanded by remember { mutableStateOf(false) }
                    val grades = mapOf("10" to 10, "11" to 11, "12" to 12, "Sudah Lulus" to 0)
                    ExposedDropdownMenuBox(
                        expanded = gradeExpanded,
                        onExpandedChange = { gradeExpanded = !gradeExpanded }
                    ) {
                        OutlinedTextField(
                            value = grades.entries.find { it.value == state.grade }?.key ?: "",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                            colors = OutlinedTextFieldDefaults.colors()
                        )
                        ExposedDropdownMenu(
                            expanded = gradeExpanded,
                            onDismissRequest = { gradeExpanded = false }
                        ) {
                            grades.forEach { (label, value) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        viewModel.onGradeChange(value)
                                        gradeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    TentuinTextField(
                        value = state.nisn,
                        onValueChange = viewModel::onNisnChange,
                        label = "NISN (Opsional)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )

                    Spacer(Modifier.height(8.dp))

                    TentuinButton(
                        text = "Simpan Perubahan",
                        onClick = viewModel::saveProfile,
                        isLoading = state.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
