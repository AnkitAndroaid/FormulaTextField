package com.androaid.formulatextfield
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    
    // 💡 Use InteractionSource for more reliable focus tracking
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // 💡 Track cursor position and selection using TextFieldValue
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        )
    }

    val isFormulaMode = isFocused && textFieldValue.text.startsWith("=")

    // Manage keyboard transitions
    LaunchedEffect(isFormulaMode, isFocused) {
        if (isFormulaMode) {
            keyboardController?.hide()
        } else if (isFocused) {
            // Show system keyboard if focused but not in formula mode
            keyboardController?.show()
        }
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
        if (textFieldValue.text.startsWith("=")) {
            val expression = textFieldValue.text.drop(1)
            if (expression.isNotBlank()) {
                try {
                    val result = evaluateMathExpression(expression)
                    val formattedResult = if (result % 1 == 0.0) result.toInt().toString() else result.toString()
                    onEvaluateResult(formattedResult)
                } catch (e: Exception) {
                    onEvaluateResult("Error $e")
                }
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
                .focusRequester(focusRequester)
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyUp) {
                        triggerEvaluation()
                        true
                    } else {
                        false
                    }
                },
            interactionSource = interactionSource,
            label = { Text(text = label) },
            placeholder = { Text(text = "e.g., =12 + 45 * 3") },
            leadingIcon = {
                IconButton(
                    onClick = {
                        focusRequester.requestFocus()
                        if (!textFieldValue.text.startsWith("=")) {
                            val newText = "=" + textFieldValue.text
                            textFieldValue = textFieldValue.copy(
                                text = newText,
                                selection = TextRange(newText.length)
                            )
                            onValueChange(newText)
                        }
                    },
                    modifier = Modifier.testTag("FormulaButton")
                ) {
                    Text(
                        text = "=",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isFormulaMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            },
            visualTransformation = remember { FormulaVisualTransformation() },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { 
                    triggerEvaluation()
                    focusManager.clearFocus()
                }
            )
        )

        // 💡 Unified Custom Math Keypad - Only visible in Formula Mode
        if (isFormulaMode) {
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
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    }
                }
            )
        }
    }
}
