package com.j4.texter2025.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.widget.Toast
import kotlin.math.roundToInt

private fun parseHexToColorOrNull(rawInput: String): Color? {
    val cleaned = rawInput.trim().removePrefix("#").uppercase()
    val normalized = when (cleaned.length) {
        6 -> "FF$cleaned"
        8 -> cleaned
        else -> return null
    }

    val argb = normalized.toLongOrNull(16) ?: return null
    return Color(argb.toInt())
}

@Composable
fun ColorControlsPanel(
    selectedColor: Color,
    red: Float,
    green: Float,
    blue: Float,
    alpha: Float,
    blur: Float,
    hue: Float,
    sv: Pair<Float, Float>,
    onColorChange: (Color) -> Unit,
    onRedChange: (Float) -> Unit,
    onGreenChange: (Float) -> Unit,
    onBlueChange: (Float) -> Unit,
    onAlphaChange: (Float) -> Unit,
    onBlurChange: (Float) -> Unit,
    onHueChange: (Float) -> Unit,
    onSvChange: (Pair<Float, Float>) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var hexInput by remember(selectedColor) { mutableStateOf(selectedColor.toHexString()) }
    var hasHexError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = hexInput,
                onValueChange = { value ->
                    val sanitized = value
                        .removePrefix("#")
                        .filter { it.isDigit() || it.uppercaseChar() in 'A'..'F' }
                        .uppercase()
                        .take(8)

                    hexInput = sanitized
                    val parsedColor = parseHexToColorOrNull(sanitized)
                    if (parsedColor != null) {
                        onColorChange(parsedColor)
                        hasHexError = false
                    } else {
                        hasHexError = sanitized.isNotEmpty() && sanitized.length != 6 && sanitized.length != 8
                    }
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Hex") },
                prefix = { Text("#") },
                isError = hasHexError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
            )

            TextButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString("#${selectedColor.toHexString()}"))
                    Toast.makeText(context, "Color code copied", Toast.LENGTH_SHORT).show()
                }
            ) {
                Text("Copy")
            }

            TextButton(
                onClick = {
                    val pasted = clipboardManager.getText()?.text?.toString().orEmpty()
                    val sanitized = pasted
                        .trim()
                        .removePrefix("#")
                        .filter { it.isDigit() || it.uppercaseChar() in 'A'..'F' }
                        .uppercase()
                        .take(8)

                    hexInput = sanitized
                    val parsedColor = parseHexToColorOrNull(sanitized)
                    if (parsedColor != null) {
                        onColorChange(parsedColor)
                        hasHexError = false
                    } else {
                        hasHexError = true
                        Toast.makeText(context, "Invalid hex color", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Paste")
            }
        }

        if (hasHexError) {
            Text(
                text = "Use 6 or 8 hex digits (RRGGBB or AARRGGBB)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Hue slider
        Text("Hue", style = MaterialTheme.typography.labelSmall)
        HueSlider(
            hue = hue,
            onHueChange = { newHue -> onHueChange(newHue / 360f) }
        )

        // Spectrum Picker
        Text("Spectrum Picker", style = MaterialTheme.typography.labelSmall)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            ColorSpectrumPicker(
                hue = hue,
                modifier = Modifier.size(156.dp),
                sv = sv,
                onSvChanged = onSvChange
            )
        }

        // Advanced Picker dropdown
        var advancedExpanded by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { advancedExpanded = !advancedExpanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Advanced Picker",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (advancedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (advancedExpanded) "Collapse" else "Expand"
                    )
                }

                AnimatedVisibility(
                    visible = advancedExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        ColorSlider("Red", red, onRedChange, Color.Red)
                        ColorSlider("Green", green, onGreenChange, Color.Green)
                        ColorSlider("Blue", blue, onBlueChange, Color.Blue)
                    }
                }
            }
        }

        // Alpha slider
        ColorSlider("Alpha", alpha, onAlphaChange, Color.Gray)

        // Blur slider (only when transparent)
        if (alpha < 1f) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Blur", modifier = Modifier.width(48.dp))
                Slider(
                    value = blur,
                    onValueChange = onBlurChange,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.LightGray,
                        activeTrackColor = Color.LightGray,
                        inactiveTrackColor = Color.LightGray.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${(blur * 50).roundToInt()}dp",
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.End
                )
            }
        }

        // Color preview swatch
        Surface(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.CenterHorizontally)
                .border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
            color = selectedColor,
            shape = MaterialTheme.shapes.small,
            tonalElevation = 2.dp
        ) {}
        Text(
            "Selected: #${selectedColor.toHexString()}",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
