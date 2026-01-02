package com.chronos.alarm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chronos.alarm.domain.model.ChallengeConfig
import com.chronos.alarm.domain.model.ChallengeParams
import com.chronos.alarm.domain.model.ChallengeType
import com.chronos.alarm.ui.theme.BrutalistButton
import com.chronos.alarm.ui.theme.BrutalistTextField

@Composable
fun ChallengeConfigurator(
    challenges: List<ChallengeConfig>,
    onAddChallenge: (ChallengeType) -> Unit,
    onRemoveChallenge: (String) -> Unit,
    onUpdateChallenge: (String, ChallengeParams) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CHALLENGES",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { showAddDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Challenge"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (challenges.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color.Black)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No challenges - alarm dismisses immediately",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                challenges.forEachIndexed { index, challenge ->
                    ChallengeItem(
                        index = index + 1,
                        challenge = challenge,
                        onRemove = { onRemoveChallenge(challenge.id) },
                        onUpdate = { params -> onUpdateChallenge(challenge.id, params) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddChallengeDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { type ->
                onAddChallenge(type)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ChallengeItem(
    index: Int,
    challenge: ChallengeConfig,
    onRemove: () -> Unit,
    onUpdate: (ChallengeParams) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color.Black)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$index. ${challenge.type.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = getChallengeDescription(challenge),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "COLLAPSE" else "EXPAND")
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Challenge",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        if (expanded) {
            Divider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 2.dp,
                color = Color.Black
            )
            ChallengeParams(
                challenge = challenge,
                onUpdate = onUpdate,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun ChallengeParams(
    challenge: ChallengeConfig,
    onUpdate: (ChallengeParams) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (challenge.type) {
            ChallengeType.BURST -> {
                val count = challenge.params.count ?: 50
                ParamSlider(
                    label = "Tap Count",
                    value = count,
                    range = 10..200,
                    onValueChange = { onUpdate(challenge.params.copy(count = it)) }
                )
            }
            ChallengeType.MATH -> {
                val count = challenge.params.count ?: 5
                ParamSlider(
                    label = "Problem Count",
                    value = count,
                    range = 1..10,
                    onValueChange = { onUpdate(challenge.params.copy(count = it)) }
                )
                
                val difficulty = challenge.params.difficulty ?: "NORMAL"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BrutalistButton(
                        onClick = { onUpdate(challenge.params.copy(difficulty = "NORMAL")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "NORMAL",
                            fontWeight = if (difficulty == "NORMAL") FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    BrutalistButton(
                        onClick = { onUpdate(challenge.params.copy(difficulty = "HARD")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "HARD",
                            fontWeight = if (difficulty == "HARD") FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            ChallengeType.MEMORY -> {
                val rounds = challenge.params.rounds ?: 3
                ParamSlider(
                    label = "Rounds",
                    value = rounds,
                    range = 1..5,
                    onValueChange = { onUpdate(challenge.params.copy(rounds = it)) }
                )
            }
            ChallengeType.TYPING -> {
                val text = challenge.params.text ?: ""
                BrutalistTextField(
                    value = text,
                    onValueChange = { onUpdate(challenge.params.copy(text = it)) },
                    placeholder = { Text("Custom phrase (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            ChallengeType.VELOCITY -> {
                val speed = challenge.params.targetSpeed ?: 5
                ParamSlider(
                    label = "Speed (km/h)",
                    value = speed,
                    range = 1..20,
                    onValueChange = { onUpdate(challenge.params.copy(targetSpeed = it)) }
                )
            }
            ChallengeType.BLUETOOTH -> {
                val deviceName = challenge.params.deviceName ?: ""
                BrutalistTextField(
                    value = deviceName,
                    onValueChange = { onUpdate(challenge.params.copy(deviceName = it)) },
                    placeholder = { Text("Device name (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ParamSlider(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = range.last - range.first - 1
        )
    }
}

@Composable
private fun AddChallengeDialog(
    onDismiss: () -> Unit,
    onConfirm: (ChallengeType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "ADD CHALLENGE",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ChallengeType.entries) { type ->
                    BrutalistButton(
                        onClick = { onConfirm(type) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = type.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = getChallengeTypeDescription(type),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

private fun getChallengeDescription(challenge: ChallengeConfig): String {
    return when (challenge.type) {
        ChallengeType.BURST -> "Tap ${challenge.params.count ?: 50} times"
        ChallengeType.MATH -> "${challenge.params.count ?: 5} problems (${challenge.params.difficulty ?: "NORMAL"})"
        ChallengeType.MEMORY -> "${challenge.params.rounds ?: 3} rounds"
        ChallengeType.TYPING -> if (challenge.params.text.isNullOrEmpty()) "Random phrase" else "Custom phrase"
        ChallengeType.VELOCITY -> "Reach ${challenge.params.targetSpeed ?: 5} km/h"
        ChallengeType.BLUETOOTH -> if (challenge.params.deviceName.isNullOrEmpty()) "Any device" else challenge.params.deviceName!!
    }
}

private fun getChallengeTypeDescription(type: ChallengeType): String {
    return when (type) {
        ChallengeType.BURST -> "Tap rapidly to dismiss"
        ChallengeType.MATH -> "Solve math problems"
        ChallengeType.MEMORY -> "Remember the pattern"
        ChallengeType.TYPING -> "Type a phrase correctly"
        ChallengeType.VELOCITY -> "Start moving (requires GPS)"
        ChallengeType.BLUETOOTH -> "Connect to device"
    }
}
