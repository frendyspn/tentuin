package id.tentuin.agent.ui.screen.withdrawal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.agent.core.network.friendlyApiError
import id.tentuin.agent.data.model.Agent
import id.tentuin.agent.data.model.AgentWithdrawal
import id.tentuin.agent.data.repository.AgentRepository
import id.tentuin.agent.data.repository.CommissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

const val MIN_WITHDRAWAL_AMOUNT = 50_000

data class WithdrawalUiState(
    val agent:            Agent?              = null,
    val availableBalance: Int                 = 0,
    val totalWithdrawn:   Int                 = 0,
    val pendingAmount:    Int                 = 0,
    val withdrawals:      List<AgentWithdrawal> = emptyList(),
    val amountInput:      String              = "",
    val isLoading:        Boolean             = true,
    val isSubmitting:     Boolean             = false,
    val error:            String?             = null,
    val successMessage:   String?             = null,
) {
    val hasBankInfo: Boolean
        get() = !agent?.bankName.isNullOrBlank() &&
                !agent?.bankAccountNumber.isNullOrBlank() &&
                !agent?.bankAccountName.isNullOrBlank()

    val parsedAmount: Int
        get() = amountInput.toIntOrNull() ?: 0
}

@HiltViewModel
class WithdrawalViewModel @Inject constructor(
    private val commissionRepository: CommissionRepository,
    private val agentRepository:      AgentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WithdrawalUiState())
    val uiState: StateFlow<WithdrawalUiState> = _uiState.asStateFlow()

    init { load() }

    fun refresh() = load()

    fun onAmountChange(text: String) {
        // hanya digit, max 12 char
        val digitsOnly = text.filter { it.isDigit() }.take(12)
        _uiState.update { it.copy(amountInput = digitsOnly, error = null) }
    }

    fun clearMessage() = _uiState.update { it.copy(error = null, successMessage = null) }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val year = Calendar.getInstance().get(Calendar.YEAR)
            val agent       = agentRepository.getCurrentAgent().getOrNull()
            val commissions = commissionRepository.getCommissions(year).getOrDefault(emptyList())
            val withdrawals = commissionRepository.getWithdrawals().getOrDefault(emptyList())

            val totalPaid       = commissions.filter { it.status == "paid" }.sumOf { it.totalAmount }
            val pending         = withdrawals
                .filter { it.status in listOf("requested", "approved") }
                .sumOf { it.amount }
            val withdrawn       = withdrawals
                .filter { it.status == "transferred" }
                .sumOf { it.amount }
            val available       = (totalPaid - pending - withdrawn).coerceAtLeast(0)

            _uiState.update {
                it.copy(
                    agent            = agent,
                    availableBalance = available,
                    totalWithdrawn   = withdrawn,
                    pendingAmount    = pending,
                    withdrawals      = withdrawals,
                    isLoading        = false,
                )
            }
        }
    }

    fun submitWithdrawal() {
        val current = _uiState.value
        val amount = current.parsedAmount

        when {
            !current.hasBankInfo -> {
                _uiState.update { it.copy(error = "Atur rekening bank di menu Profil dulu.") }
                return
            }
            amount < MIN_WITHDRAWAL_AMOUNT -> {
                _uiState.update { it.copy(error = "Minimal penarikan Rp ${MIN_WITHDRAWAL_AMOUNT.formatThousand()}.") }
                return
            }
            amount > current.availableBalance -> {
                _uiState.update { it.copy(error = "Saldo tidak cukup.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            commissionRepository.requestWithdrawal(amount)
                .onSuccess { withdrawal ->
                    _uiState.update {
                        it.copy(
                            isSubmitting     = false,
                            amountInput      = "",
                            pendingAmount    = it.pendingAmount + amount,
                            availableBalance = (it.availableBalance - amount).coerceAtLeast(0),
                            withdrawals      = listOf(withdrawal) + it.withdrawals,
                            successMessage   = "Permintaan penarikan berhasil diajukan.",
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error        = friendlyApiError(e, "Gagal mengajukan penarikan."),
                        )
                    }
                }
        }
    }
}

private fun Int.formatThousand(): String =
    "%,d".format(this).replace(',', '.')
