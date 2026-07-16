package com.androaid.formulatextfield

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun FormulaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onEvaluateResult: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Calculation Input"
) {
    // 💡 Track cursor position and selection using TextFieldValue
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        )
    }

    // Sync internal state when external value changes (e.g., from evaluation)
    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(
                text = value,
                selection = TextRange(value.length)
            )
        }
    }

    val triggerEvaluation = {
        if (value.isNotBlank()) {
            try {
                val result = evaluateMathExpression(value)
                val formattedResult = if (result % 1 == 0.0) result.toInt().toString() else result.toString()
                onEvaluateResult(formattedResult)
            } catch (e: Exception) {
                onEvaluateResult("Error")
            }
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                onValueChange(newValue.text)
            },
            modifier = Modifier
                .fillMaxWidth()
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyUp) {
                        triggerEvaluation()
                        true
                    } else {
                        false
                    }
                },
            label = { Text(text = label) },
            placeholder = { Text(text = "e.g., 12 + 45 * 3") },
            visualTransformation = remember { FormulaVisualTransformation() },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
            keyboardOptions = KeyboardOptions(
                // 💡 Changed to Decimal to show numeric pad by default
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { triggerEvaluation() }
            )
        )

        // 💡 Specialized Math Toolbar for operators
        MathOperatorToolbar(
            onOperatorClick = { operator ->
                val selection = textFieldValue.selection
                val currentText = textFieldValue.text
                
                val newText = StringBuilder(currentText)
                    .replace(selection.start, selection.end, operator)
                    .toString()
                
                val newCursorPosition = selection.start + operator.length
                
                textFieldValue = textFieldValue.copy(
                    text = newText,
                    selection = TextRange(newCursorPosition)
                )
                onValueChange(newText)
            }
        )
    }
}
