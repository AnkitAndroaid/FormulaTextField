package com.androaid.formulatextfield

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun FormulaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onEvaluateResult: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Calculation Input"
) {
    val totalOpenBrackets = value.count { it == '(' }
    val totalCloseBrackets = value.count { it == ')' }
    val isBracketMismatched = totalOpenBrackets != totalCloseBrackets

    val triggerEvaluation = {
        if (value.isNotBlank() && !isBracketMismatched) {
            try {
                val result = evaluateMathExpression(value)
                val formattedResult = if (result % 1 == 0.0) result.toInt().toString() else result.toString()
                onEvaluateResult(formattedResult)
            } catch (e: Exception) {
                onEvaluateResult("Error")
            }
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { keyEvent ->
                // 💡 FIX 2: Only trigger evaluation on KeyUp event type to prevent double execution
                if (keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyUp) {
                    triggerEvaluation()
                    true 
                } else {
                    false
                }
            },
        label = { Text(text = label) },
        placeholder = { Text(text = "e.g., (12 + 45) * 3") },
        visualTransformation = remember { FormulaVisualTransformation() },
        isError = isBracketMismatched,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
        keyboardOptions = KeyboardOptions(
            // 💡 FIX 1: Change to Text type so the user can access standard symbols (+, -, *, /)
            keyboardType = KeyboardType.Text, 
            imeAction = ImeAction.Done 
        ),
        keyboardActions = KeyboardActions(
            onDone = { triggerEvaluation() }
        ),
        trailingIcon = {
            if (isBracketMismatched) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Mismatched parentheses error",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        supportingText = {
            if (isBracketMismatched) {
                Text(
                    text = "Mismatched Parentheses: Opened $totalOpenBrackets but Closed $totalCloseBrackets",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}