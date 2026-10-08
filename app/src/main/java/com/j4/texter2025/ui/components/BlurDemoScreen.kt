package com.j4.texter2025.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Demo screen to showcase the blur/glass effect functionality
 */
@Composable
fun BlurDemoScreen() {
    var showColorPicker by remember { mutableStateOf(false) }
    var selectedColorWithBlur by remember { mutableStateOf(ColorWithBlur(Color(0x80FF5722), 0.3f)) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🎨 Blur/Glass Effect Demo",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Text(
            text = "This demonstrates the new blur/glass effect functionality for transparent backgrounds.",
            style = MaterialTheme.typography.bodyMedium
        )
        
        // Demo card with blur effect
        BlurredSurface(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            color = selectedColorWithBlur.color,
            blur = selectedColorWithBlur.blur,
            elevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "✨ Glassy Surface",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Blur: ${(selectedColorWithBlur.blur * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Alpha: ${(selectedColorWithBlur.color.alpha * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
        // Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🎛️ Controls",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                Button(
                    onClick = { showColorPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🎨 Pick Color & Blur")
                }
                
                // Quick presets
                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedColorWithBlur = ColorWithBlur(Color(0x80FF5722), 0.2f) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Orange")
                    }
                    Button(
                        onClick = { selectedColorWithBlur = ColorWithBlur(Color(0x802196F3), 0.4f) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Blue")
                    }
                    Button(
                        onClick = { selectedColorWithBlur = ColorWithBlur(Color(0x804CAF50), 0.3f) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Green")
                    }
                }
            }
        }
        
        // Information card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ℹ️ How it works",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "• Blur effect only appears when transparency (alpha < 100%) is enabled",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "• Higher blur values create more pronounced glassy effects",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "• The ColorPickerDialog now includes a blur slider below the alpha slider",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
    
    // Color picker dialog
    if (showColorPicker) {
        ColorPickerDialog(
            show = showColorPicker,
            currentColor = selectedColorWithBlur.color,
            currentBlur = selectedColorWithBlur.blur,
            onColorSelected = { colorWithBlur ->
                selectedColorWithBlur = colorWithBlur
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }
}
