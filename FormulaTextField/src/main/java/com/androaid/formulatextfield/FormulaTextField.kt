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
import androidx.compose.runtime.Stable
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

@Stable
class FormulaTextFieldState(
    initialValue: String,
    private val onValueChange: (String) -> Unit,
    private val onEvaluateResult: (String) -> Unit
) {
    var textFieldValue by mutableStateOf(
        TextFieldValue(
            text = initialValue,
            selection = TextRange(initialValue.length)
        )
    )
        private set

    fun updateTextFieldValue(newValue: TextFieldValue) {
        textFieldValue = newValue
        onValueChange(newValue.text)
    }

    fun syncValue(externalValue: String) {
        if (externalValue != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(
                text = externalValue,
                selection = TextRange(externalValue.length)
            )
        }
    }

    fun triggerEvaluation() {
        if (!textFieldValue.text.startsWith("=")) return
        
        val expression = textFieldValue.text.drop(1)
        if (expression.isBlank()) return

        try {
            val result = evaluateMathExpression(expression)
            val formattedResult = if (result % 1 == 0.0) {
                result.toInt().toString()
            } else {
                result.toString()
            }
            onEvaluateResult(formattedResult)
        } catch (e: Exception) {
            onEvaluateResult("Error $e")
        }
    }

    fun toggleFormulaMode() {
        if (!textFieldValue.text.startsWith("=")) {
            val newText = "=" + textFieldValue.text
            updateTextFieldValue(
                textFieldValue.copy(
                    text = newText,
                    selection = TextRange(newText.length)
                )
            )
        }
    }

    fun handleKeypadEvent(event: KeypadEvent) {
        when (event) {
            is KeypadEvent.Character -> appendCharacter(event.char)
            KeypadEvent.Backspace -> handleBackspace()
            KeypadEvent.Done -> {
                // Done event is handled specifically in the UI layer for focus/keyboard control.
            }
        }
    }

    private fun appendCharacter(char: String) {
        val selection = textFieldValue.selection
        val newText = StringBuilder(textFieldValue.text)
            .replace(selection.start, selection.end, char)
            .toString()
        val newCursorPosition = selection.start + char.length
        updateTextFieldValue(
            textFieldValue.copy(
                text = newText,
                selection = TextRange(newCursorPosition)
            )
        )
    }

    private fun handleBackspace() {
        val selection = textFieldValue.selection
        val newText = if (!selection.collapsed) {
            StringBuilder(textFieldValue.text).delete(selection.start, selection.end).toString()
        } else if (selection.start > 0) {
            StringBuilder(textFieldValue.text).deleteAt(selection.start - 1).toString()
        } else {
            return
        }

        val newCursorPosition = if (!selection.collapsed) selection.start else selection.start - 1
        updateTextFieldValue(
            textFieldValue.copy(
                text = newText,
                selection = TextRange(newCursorPosition)
            )
        )
    }
}

@Composable
fun rememberFormulaTextFieldState(
    value: String,
    onValueChange: (String) -> Unit,
    onEvaluateResult: (String) -> Unit
): FormulaTextFieldState {
    return remember {
        FormulaTextFieldState(value, onValueChange, onEvaluateResult)
    }
}

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
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val state = rememberFormulaTextFieldState(value, onValueChange, onEvaluateResult)
    val isFormulaMode = isFocused && state.textFieldValue.text.startsWith("=")

    LaunchedEffect(isFormulaMode, isFocused) {
        if (isFormulaMode) keyboardController?.hide()
        else if (isFocused) keyboardController?.show()
    }

    LaunchedEffect(value) {
        state.syncValue(value)
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = state.textFieldValue,
            onValueChange = { state.updateTextFieldValue(it) },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyUp) {
                        state.triggerEvaluation()
                        true
                    } else false
                },
            interactionSource = interactionSource,
            label = { Text(text = label) },
            placeholder = { Text(text = "e.g., =12 + 45 * 3") },
            leadingIcon = {
                FormulaLeadingIcon(
                    isFormulaMode = isFormulaMode,
                    onClick = {
                        focusRequester.requestFocus()
                        state.toggleFormulaMode()
                    }
                )
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
                    state.triggerEvaluation()
                    focusManager.clearFocus()
                }
            )
        )

        FormulaMathKeypad(
            isVisible = isFormulaMode,
            onCalculate = {
                state.triggerEvaluation()
                focusManager.clearFocus()
                keyboardController?.hide()
            },
            onEvent = { state.handleKeypadEvent(it) }
        )
    }
}

@Composable
private fun FormulaLeadingIcon(
    isFormulaMode: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.testTag("FormulaButton")
    ) {
        Text(
            text = "=",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (isFormulaMode) MaterialTheme.colorScheme.primary 
                    else MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun FormulaMathKeypad(
    isVisible: Boolean,
    onCalculate: () -> Unit,
    onEvent: (KeypadEvent) -> Unit
) {
    if (isVisible) {
        MathKeypad(
            onEvent = { event ->
                if (event is KeypadEvent.Done) {
                    onCalculate()
                } else {
                    onEvent(event)
                }
            }
        )
    }
}
