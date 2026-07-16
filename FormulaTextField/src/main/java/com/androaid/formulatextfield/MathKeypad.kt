package com.androaid.formulatextfield

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

sealed class KeypadEvent {
    data class Character(val char: String) : KeypadEvent()
    data object Backspace : KeypadEvent()
    data object Done : KeypadEvent()
}

@Composable
fun MathKeypad(
    onEvent: (KeypadEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("MathKeypad")
            .background(MaterialTheme.colorScheme.surfaceVariant),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val rows = listOf(
                listOf("7", "8", "9", "/"),
                listOf("4", "5", "6", "*"),
                listOf("1", "2", "3", "-"),
                listOf("0", ".", null, "+") // null is for backspace placeholder
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { key ->
                        if (key != null) {
                            KeyButton(
                                text = key,
                                onClick = { onEvent(KeypadEvent.Character(key)) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            IconButton(
                                onClick = { onEvent(KeypadEvent.Backspace) },
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.5f)
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        MaterialTheme.shapes.medium
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Backspace"
                                )
                            }
                        }
                    }
                }
            }
            
            // Bottom Action Row
            TextButton(
                onClick = { onEvent(KeypadEvent.Done) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("CalculateButton")
                    .height(48.dp),
                shape = MaterialTheme.shapes.medium,
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text("Calculate")
            }
        }
    }
}

@Composable
private fun KeyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.aspectRatio(1.5f),
        shape = MaterialTheme.shapes.medium,
        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
            containerColor = if (text.matches(Regex("[0-9.]"))) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = if (text.matches(Regex("[0-9.]"))) 
                MaterialTheme.colorScheme.onSurface 
            else 
                MaterialTheme.colorScheme.onTertiaryContainer
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}
