package com.androaid.formulatextfield

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
    val keyboardController = LocalSoftwareKeyboardController.current
    var isFocused by remember { mutableStateOf(false) }

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
                .onFocusChanged { 
                    isFocused = it.isFocused
                    if (it.isFocused) {
                        keyboardController?.hide()
                    }
                }
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
                keyboardType = KeyboardType.Password, // Hack to minimize keyboard popping up
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { 
                    triggerEvaluation()
                    isFocused = false
                }
            )
        )

        // 💡 Unified Custom Math Keypad
        AnimatedVisibility(
            visible = isFocused,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            MathKeypad(
                onEvent = { event ->
                    when (event) {
                        is KeypadEvent.Character -> {
                            val selection = textFieldValue.selection
                            val currentText = textFieldValue.text
                            val newText = StringBuilder(currentText)
                                .replace(selection.start, selection.end, event.char)
                                .toString()
                            val newCursorPosition = selection.start + event.char.length
                            textFieldValue = textFieldValue.copy(
                                text = newText,
                                selection = TextRange(newCursorPosition)
                            )
                            onValueChange(newText)
                        }
                        KeypadEvent.Backspace -> {
                            val selection = textFieldValue.selection
                            if (!selection.collapsed) {
                                val newText = StringBuilder(textFieldValue.text)
                                    .delete(selection.start, selection.end)
                                    .toString()
                                textFieldValue = textFieldValue.copy(
                                    text = newText,
                                    selection = TextRange(selection.start)
                                )
                                onValueChange(newText)
                            } else if (selection.start > 0) {
                                val newText = StringBuilder(textFieldValue.text)
                                    .deleteAt(selection.start - 1)
                                    .toString()
                                textFieldValue = textFieldValue.copy(
                                    text = newText,
                                    selection = TextRange(selection.start - 1)
                                )
                                onValueChange(newText)
                            }
                        }
                        KeypadEvent.Done -> {
                            triggerEvaluation()
                            isFocused = false
                            keyboardController?.hide()
                        }
                    }
                }
            )
        }
    }
}
