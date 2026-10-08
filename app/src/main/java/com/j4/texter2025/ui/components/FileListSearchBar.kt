package com.j4.texter2025.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.j4.texter2025.data.SearchMethod
import com.j4.texter2025.data.SortMethod

@Composable
fun FileListSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    currentSortMethod: SortMethod,
    onSortMethodChange: (SortMethod) -> Unit,
    searchMethod: SearchMethod,
    onSearchMethodChange: (SearchMethod) -> Unit,
    isCaseSensitive: Boolean,
    onCaseSensitiveChange: (Boolean) -> Unit,
    showFileExtensions: Boolean,
    onShowFileExtensionsChange: (Boolean) -> Unit,
    onImportRequest: () -> Unit,
    onExportRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    exportEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val glassShape = RoundedCornerShape(24.dp)
    val glassBorder = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f))
    val glassColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.42f)

    Surface(
        modifier = modifier,
        shape = glassShape,
        color = glassColor,
        border = glassBorder,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp
    ) {
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            singleLine = true,
            placeholder = {
                Text(
                    text = "Search files...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            },
            leadingIcon = {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Import text file") },
                            onClick = {
                                showMenu = false
                                onImportRequest()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export selected file") },
                            enabled = exportEnabled,
                            onClick = {
                                showMenu = false
                                onExportRequest()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Open settings") },
                            onClick = {
                                showMenu = false
                                onOpenSettings()
                            }
                        )

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        DropdownMenuItem(
                            text = { Text("Show file extensions") },
                            onClick = { onShowFileExtensionsChange(!showFileExtensions) },
                            trailingIcon = {
                                Switch(
                                    checked = showFileExtensions,
                                    onCheckedChange = onShowFileExtensionsChange
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Case sensitive search") },
                            onClick = { onCaseSensitiveChange(!isCaseSensitive) },
                            trailingIcon = {
                                Switch(
                                    checked = isCaseSensitive,
                                    onCheckedChange = onCaseSensitiveChange
                                )
                            }
                        )

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Text(
                            text = "Sort by",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                        )
                        SortMethod.values().forEach { method ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = currentSortMethod == method,
                                            onClick = {
                                                onSortMethodChange(method)
                                                showMenu = false
                                            }
                                        )
                                        Text(method.displayName)
                                    }
                                },
                                onClick = {
                                    onSortMethodChange(method)
                                    showMenu = false
                                }
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Text(
                            text = "Search method",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                        )
                        SearchMethod.values().forEach { method ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = searchMethod == method,
                                            onClick = {
                                                onSearchMethodChange(method)
                                                showMenu = false
                                            }
                                        )
                                        Text(method.name.replace("_", " "))
                                    }
                                },
                                onClick = {
                                    onSearchMethodChange(method)
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .width(18.dp)
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(0.dp, Color.Transparent), glassShape)
        )
    }
}
