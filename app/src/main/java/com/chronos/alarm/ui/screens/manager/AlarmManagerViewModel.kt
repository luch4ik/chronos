package com.chronos.alarm.ui.screens.manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chronos.alarm.data.repository.AlarmRepository
import com.chronos.alarm.domain.model.*
import com.chronos.alarm.domain.scheduler.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

data class AlarmManagerUiState(
    val alarm: Alarm? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)

class AlarmManagerViewModel(
    private val alarmId: String?,
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmManagerUiState())
    val uiState: StateFlow<AlarmManagerUiState> = _uiState.asStateFlow()

    init {
        loadAlarm()
    }

    private fun loadAlarm() {
        viewModelScope.launch {
            try {
                if (alarmId != null) {
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
                } else {
                    // New alarm
                    _uiState.value = _uiState.value.copy(
                        alarm = createDefaultAlarm(),
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

    fun updateTime(hour: Int, minute: Int) {
        val alarm = _uiState.value.alarm ?: return
        val timeString = String.format("%02d:%02d", hour, minute)
        _uiState.value = _uiState.value.copy(
            alarm = alarm.copy(time = timeString)
        )
    }

    fun updateLabel(label: String) {
        val alarm = _uiState.value.alarm ?: return
        _uiState.value = _uiState.value.copy(
            alarm = alarm.copy(label = label)
        )
    }

    fun toggleDay(dayIndex: Int) {
        val alarm = _uiState.value.alarm ?: return
        val days = alarm.days.toMutableList()
        if (days.contains(dayIndex)) {
            days.remove(dayIndex)
        } else {
            days.add(dayIndex)
            days.sort()
        }
        _uiState.value = _uiState.value.copy(
            alarm = alarm.copy(days = days)
        )
    }

    fun addChallenge(type: ChallengeType) {
        val alarm = _uiState.value.alarm ?: return
        val newChallenge = ChallengeConfig(
            id = UUID.randomUUID().toString(),
            type = type,
            params = getDefaultParamsForType(type)
        )
        _uiState.value = _uiState.value.copy(
            alarm = alarm.copy(challenges = alarm.challenges + newChallenge)
        )
    }

    fun removeChallenge(challengeId: String) {
        val alarm = _uiState.value.alarm ?: return
        _uiState.value = _uiState.value.copy(
            alarm = alarm.copy(
                challenges = alarm.challenges.filter { it.id != challengeId }
            )
        )
    }

    fun updateChallenge(challengeId: String, params: ChallengeParams) {
        val alarm = _uiState.value.alarm ?: return
        _uiState.value = _uiState.value.copy(
            alarm = alarm.copy(
                challenges = alarm.challenges.map { challenge ->
                    if (challenge.id == challengeId) {
                        challenge.copy(params = params)
                    } else {
                        challenge
                    }
                }
            )
        )
    }

    fun saveAlarm(onSaved: () -> Unit) {
        val alarm = _uiState.value.alarm ?: return
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSaving = true)
                
                if (alarmId != null) {
                    repository.updateAlarm(alarm)
                } else {
                    repository.insertAlarm(alarm)
                }
                
                // Schedule the alarm if active
                if (alarm.isActive) {
                    scheduler.scheduleAlarm(alarm)
                }
                
                _uiState.value = _uiState.value.copy(isSaving = false)
                onSaved()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to save alarm"
                )
            }
        }
    }

    private fun createDefaultAlarm(): Alarm {
        return Alarm(
            id = UUID.randomUUID().toString(),
            time = "07:00",
            label = "ALARM",
            isActive = true,
            days = emptyList(),
            challenges = emptyList(),
            wakeUpCheck = null,
            emergencyContact = null
        )
    }

    private fun getDefaultParamsForType(type: ChallengeType): ChallengeParams {
        return when (type) {
            ChallengeType.BURST -> ChallengeParams(count = 50)
            ChallengeType.MATH -> ChallengeParams(count = 5, difficulty = "NORMAL")
            ChallengeType.MEMORY -> ChallengeParams(rounds = 3)
            ChallengeType.TYPING -> ChallengeParams()
            ChallengeType.VELOCITY -> ChallengeParams(targetSpeed = 5)
            ChallengeType.BLUETOOTH -> ChallengeParams()
        }
    }
}

class AlarmManagerViewModelFactory(
    private val alarmId: String?,
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmManagerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlarmManagerViewModel(alarmId, repository, scheduler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
