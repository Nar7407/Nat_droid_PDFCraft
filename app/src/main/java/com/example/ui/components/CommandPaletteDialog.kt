package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.PdfCraftViewModel
import com.example.viewmodel.WorkbenchNavDestination

data class PaletteAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val category: String,
    val onClick: () -> Unit
)

@Composable
fun CommandPaletteDialog(
    viewModel: PdfCraftViewModel,
    onDismiss: () -> Unit,
    onOpenPdfClick: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val allActions = listOf(
        PaletteAction(
            title = "Open Document (SAF)",
            subtitle = "Select PDF from device storage via ActivityResultContracts.OpenDocument",
            icon = Icons.Default.FolderOpen,
            category = "File",
            onClick = onOpenPdfClick
        ),
        PaletteAction(
            title = "Run On-Device OCR (ML Kit)",
            subtitle = "Extract text from scanned image-based pages to make them searchable",
            icon = Icons.Default.DocumentScanner,
            category = "OCR",
            onClick = {
                viewModel.runOcrOnDocument(context)
                onDismiss()
            }
        ),
        PaletteAction(
            title = "Page Organizer",
            subtitle = "Reorder, rotate, duplicate, or delete pages",
            icon = Icons.Default.Build,
            category = "Workbench",
            onClick = {
                viewModel.navigateTo(WorkbenchNavDestination.PAGE_ORGANIZER)
                onDismiss()
            }
        ),
        PaletteAction(
            title = "Merge & Split PDFs",
            subtitle = "Combine multiple PDF files or extract page intervals",
            icon = Icons.Default.CallSplit,
            category = "Workbench",
            onClick = {
                viewModel.navigateTo(WorkbenchNavDestination.MERGE_SPLIT)
                onDismiss()
            }
        ),
        PaletteAction(
            title = "Security & Encryption",
            subtitle = "Set password, permissions, and apply watermarks",
            icon = Icons.Default.Security,
            category = "Workbench",
            onClick = {
                viewModel.navigateTo(WorkbenchNavDestination.SECURITY)
                onDismiss()
            }
        ),
        PaletteAction(
            title = "Annotations & Freehand Draw",
            subtitle = "Highlighter, pens, sticky notes, and signature stamp",
            icon = Icons.Default.Draw,
            category = "Workbench",
            onClick = {
                viewModel.navigateTo(WorkbenchNavDestination.ANNOTATIONS)
                onDismiss()
            }
        ),
        PaletteAction(
            title = "Interactive Forms Filler",
            subtitle = "Fill form fields, checkboxes, and sign documents",
            icon = Icons.Default.EditNote,
            category = "Workbench",
            onClick = {
                viewModel.navigateTo(WorkbenchNavDestination.FORMS)
                onDismiss()
            }
        ),
        PaletteAction(
            title = "Document Properties Inspector",
            subtitle = "View metadata, title, author, and security status",
            icon = Icons.Default.Info,
            category = "Inspect",
            onClick = {
                viewModel.setShowPropertiesDialog(true)
                onDismiss()
            }
        ),
        PaletteAction(
            title = "Deep Undo/Redo Audit Trail",
            subtitle = "Inspect modification history and roll back actions",
            icon = Icons.Default.History,
            category = "Audit",
            onClick = {
                viewModel.setShowAuditTrailDialog(true)
                onDismiss()
            }
        ),
        PaletteAction(
            title = "Continuous Two-Up View",
            subtitle = "Toggle single page vs two-page side-by-side mode",
            icon = Icons.Default.PictureAsPdf,
            category = "View",
            onClick = {
                viewModel.toggleTwoUpView()
                viewModel.navigateTo(WorkbenchNavDestination.VIEWER)
                onDismiss()
            }
        )
    )

    val filteredActions = remember(searchQuery) {
        if (searchQuery.isBlank()) allActions
        else allActions.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.subtitle.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Command Palette",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search tools, commands, or files...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("command_palette_search_field")
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    items(filteredActions) { action ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { action.onClick() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = action.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = action.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = action.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
