package com.androaid.formulatextfield

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MathOperatorToolbar(
    onOperatorClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val operators = listOf("+", "-", "*", "/")
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        operators.forEach { operator ->
            TextButton(
                onClick = { onOperatorClick(operator) },
                modifier = Modifier.padding(horizontal = 2.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(text = operator, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
