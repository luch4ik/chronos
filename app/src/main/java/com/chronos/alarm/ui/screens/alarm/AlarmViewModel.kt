package com.chronos.alarm.ui.screens.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chronos.alarm.data.repository.AlarmRepository
import com.chronos.alarm.domain.model.Alarm
import com.chronos.alarm.domain.model.ChallengeType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AlarmUiState(
    val alarm: Alarm? = null,
    val currentChallengeIndex: Int = 0,
    val isLoading: Boolean = true,
    val isCompleted: Boolean = false,
    val error: String? = null
)

class AlarmViewModel(
    private val alarmId: String,
    private val repository: AlarmRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmUiState())
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()

    init {
        loadAlarm()
    }

    private fun loadAlarm() {
        viewModelScope.launch {
            try {
                val alarm = repository.getAlarmById(alarmId)
                if (alarm != null) {
                    _uiState.value = _uiState.value.copy(
                        alarm = alarm,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Alarm not found",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Unknown error",
                    isLoading = false
                )
            }
        }
    }

    fun onChallengeCompleted() {
        val state = _uiState.value
        val alarm = state.alarm ?: return

        if (alarm.challenges.isEmpty()) {
            // No challenges, mark as completed
            _uiState.value = state.copy(isCompleted = true)
            return
        }

        val nextIndex = state.currentChallengeIndex + 1
        if (nextIndex >= alarm.challenges.size) {
            // All challenges completed
            _uiState.value = state.copy(isCompleted = true)
        } else {
            // Move to next challenge
            _uiState.value = state.copy(currentChallengeIndex = nextIndex)
        }
    }

    fun getCurrentChallengeType(): ChallengeType? {
        val state = _uiState.value
        val alarm = state.alarm ?: return null
        if (alarm.challenges.isEmpty()) return null
        if (state.currentChallengeIndex >= alarm.challenges.size) return null
        return alarm.challenges[state.currentChallengeIndex].type
    }

    fun getTotalChallenges(): Int {
        return _uiState.value.alarm?.challenges?.size ?: 0
    }

    fun getCurrentChallengeNumber(): Int {
        return _uiState.value.currentChallengeIndex + 1
    }
}

class AlarmViewModelFactory(
    private val alarmId: String,
    private val repository: AlarmRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlarmViewModel(alarmId, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
