package com.j4.texter2025.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.j4.texter2025.data.BackupOptions
import com.j4.texter2025.data.BackupSummary

/**
 * Dialog for selecting what to export in a backup
 */
@Composable
fun ExportOptionsDialog(
    summary: BackupSummary,
    onDismiss: () -> Unit,
    onExport: (BackupOptions) -> Unit
) {
    var includeTextFiles by remember { mutableStateOf(true) }
    var includeColorPresets by remember { mutableStateOf(true) }
    var includeSettings by remember { mutableStateOf(true) }
    var includePhotoBackgrounds by remember { mutableStateOf(true) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Export Backup",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Select what to include in the backup:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Text Files option
                BackupOptionRow(
                    title = "Text Files",
                    subtitle = "${summary.textFileCount} file(s)",
                    checked = includeTextFiles,
                    onCheckedChange = { includeTextFiles = it },
                    enabled = summary.textFileCount > 0
                )
                
                // Color Presets option
                BackupOptionRow(
                    title = "Color Presets",
                    subtitle = "${summary.colorPresetCount} preset(s)",
                    checked = includeColorPresets,
                    onCheckedChange = { includeColorPresets = it },
                    enabled = summary.colorPresetCount > 0
                )
                
                // Settings option
                BackupOptionRow(
                    title = "App Settings",
                    subtitle = "Layout, behavior, display settings",
                    checked = includeSettings,
                    onCheckedChange = { includeSettings = it },
                    enabled = summary.hasSettings
                )
                
                // Photo Backgrounds option
                BackupOptionRow(
                    title = "Photo Backgrounds",
                    subtitle = "${summary.photoCount} photo(s)",
                    checked = includePhotoBackgrounds,
                    onCheckedChange = { includePhotoBackgrounds = it },
                    enabled = summary.photoCount > 0
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onExport(BackupOptions(
                                includeTextFiles = includeTextFiles,
                                includeColorPresets = includeColorPresets,
                                includeSettings = includeSettings,
                                includePhotoBackgrounds = includePhotoBackgrounds
                            ))
                        },
                        enabled = includeTextFiles || includeColorPresets || includeSettings || includePhotoBackgrounds
                    ) {
                        Text("Export")
                    }
                }
            }
        }
    }
}

/**
 * Dialog for selecting what to import from a backup
 */
@Composable
fun ImportOptionsDialog(
    summary: BackupSummary,
    onDismiss: () -> Unit,
    onImport: (BackupOptions) -> Unit
) {
    var includeTextFiles by remember { mutableStateOf(true) }
    var includeColorPresets by remember { mutableStateOf(true) }
    var includeSettings by remember { mutableStateOf(true) }
    var includePhotoBackgrounds by remember { mutableStateOf(true) }
    var mergeMode by remember { mutableStateOf(true) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Import Backup",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Show backup info
                if (summary.backupDate.isNotEmpty()) {
                    Text(
                        text = "Backup from: ${summary.backupDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Text(
                    text = "Select what to restore from this backup:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Text Files option
                BackupOptionRow(
                    title = "Text Files",
                    subtitle = if (summary.textFileCount > 0) "${summary.textFileCount} file(s)" else "None in backup",
                    checked = includeTextFiles && summary.textFileCount > 0,
                    onCheckedChange = { includeTextFiles = it },
                    enabled = summary.textFileCount > 0
                )
                
                // Color Presets option
                BackupOptionRow(
                    title = "Color Presets",
                    subtitle = if (summary.colorPresetCount > 0) "${summary.colorPresetCount} preset(s)" else "None in backup",
                    checked = includeColorPresets && summary.colorPresetCount > 0,
                    onCheckedChange = { includeColorPresets = it },
                    enabled = summary.colorPresetCount > 0
                )
                
                // Settings option
                BackupOptionRow(
                    title = "App Settings",
                    subtitle = if (summary.hasSettings) "Layout, behavior, display settings" else "None in backup",
                    checked = includeSettings && summary.hasSettings,
                    onCheckedChange = { includeSettings = it },
                    enabled = summary.hasSettings
                )
                
                // Photo Backgrounds option
                BackupOptionRow(
                    title = "Photo Backgrounds",
                    subtitle = if (summary.photoCount > 0) "${summary.photoCount} photo(s)" else "None in backup",
                    checked = includePhotoBackgrounds && summary.photoCount > 0,
                    onCheckedChange = { includePhotoBackgrounds = it },
                    enabled = summary.photoCount > 0
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Merge/Replace Mode Toggle
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Import Mode",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (mergeMode) "Merge with existing" else "Replace existing",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (mergeMode) 
                                        "Keep files/presets created after backup" 
                                    else 
                                        "Remove all existing data in selected categories",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = mergeMode,
                                onCheckedChange = { mergeMode = it }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Warning about overwriting
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (mergeMode) 
                            "⚠️ Backup data will overwrite conflicts. New items will be preserved."
                        else
                            "⚠️ All existing data in selected categories will be replaced.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onImport(BackupOptions(
                                includeTextFiles = includeTextFiles && summary.textFileCount > 0,
                                includeColorPresets = includeColorPresets && summary.colorPresetCount > 0,
                                includeSettings = includeSettings && summary.hasSettings,
                                includePhotoBackgrounds = includePhotoBackgrounds && summary.photoCount > 0,
                                mergeMode = mergeMode
                            ))
                        },
                        enabled = (includeTextFiles && summary.textFileCount > 0) || 
                                  (includeColorPresets && summary.colorPresetCount > 0) || 
                                  (includeSettings && summary.hasSettings) || 
                                  (includePhotoBackgrounds && summary.photoCount > 0)
                    ) {
                        Text("Import")
                    }
                }
            }
        }
    }
}

@Composable
private fun BackupOptionRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked && enabled,
            onCheckedChange = { if (enabled) onCheckedChange(it) },
            enabled = enabled
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface 
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant 
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
