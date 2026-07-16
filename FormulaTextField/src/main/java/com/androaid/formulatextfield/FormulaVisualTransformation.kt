package com.androaid.formulatextfield

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle

/**
 * A custom [VisualTransformation] that applies real-time syntax highlighting
 * exclusively to basic mathematical arithmetic expressions.
 */
class FormulaVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val rawText = text.text

        val highlightedString = buildAnnotatedString {
            var lastIndex = 0

            // Regex patterns to isolate:
            // 1. Numeric constants (integers and decimals) -> (\b\d+(\.\d+)?\b)
            // 2. Basic mathematical operators (+, -, *, /) -> ([\+\-\*\/])
            val regex = """(\b\d+(\.\d+)?\b)|([\+\-\*\/])""".toRegex()

            regex.findAll(rawText).forEach { matchResult ->
                val matchStart = matchResult.range.first
                val matchEnd = matchResult.range.last + 1
                val value = matchResult.value

                // Append unformatted characters (like spaces) leading up to the match
                if (matchStart > lastIndex) {
                    append(rawText.substring(lastIndex, matchStart))
                }

                when {
                    // Highlight numbers (Dark Green)
                    value.matches("""^\d+(\.\d+)?$""".toRegex()) -> {
                        withStyle(SpanStyle(color = Color(0xFF137333))) {
                            append(value)
                        }
                    }
                    // Highlight operators (Burgundy/Bold)
                    value.matches("""^[\+\-\*\/]$""".toRegex()) -> {
                        withStyle(SpanStyle(color = Color(0xFFB06000), fontWeight = FontWeight.Bold)) {
                            append(value)
                        }
                    }
                    else -> append(value)
                }
                lastIndex = matchEnd
            }

            // Append any trailing characters left over at the end of the string
            if (lastIndex < rawText.length) {
                append(rawText.substring(lastIndex))
            }
        }

        // OffsetMapping.Identity means the cursor positions remain 1:1 identical 
        // because we are only changing character styles, not adding or removing characters.
        return TransformedText(highlightedString, OffsetMapping.Identity)
    }
}