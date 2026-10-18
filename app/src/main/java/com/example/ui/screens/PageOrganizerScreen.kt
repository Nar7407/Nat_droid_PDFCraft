package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PdfPage
import com.example.ui.components.DragDropPageOrganizer
import com.example.viewmodel.PdfCraftViewModel
import com.example.viewmodel.WorkbenchNavDestination

@Composable
fun PageOrganizerScreen(
    viewModel: PdfCraftViewModel
) {
    val activeDoc by viewModel.activeDocument.collectAsState()
    var selectedPageIndex by remember { mutableStateOf(0) }

    if (activeDoc == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Select or open a PDF to organize pages.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val doc = activeDoc!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("page_organizer_screen")
    ) {
        // Toolbar with bulk actions
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Page Organizer (${doc.pages.size} pages)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Selected: Page ${selectedPageIndex + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { viewModel.rotatePage(selectedPageIndex, -90) },
                        modifier = Modifier.testTag("organizer_rotate_left_button")
                    ) {
                        Icon(Icons.Default.RotateLeft, contentDescription = "Rotate -90°")
                    }
                    IconButton(
                        onClick = { viewModel.rotatePage(selectedPageIndex, 90) },
                        modifier = Modifier.testTag("organizer_rotate_right_button")
                    ) {
                        Icon(Icons.Default.RotateRight, contentDescription = "Rotate +90°")
                    }
                    IconButton(
                        onClick = { viewModel.duplicatePage(selectedPageIndex) },
                        modifier = Modifier.testTag("organizer_duplicate_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate page")
                    }
                    IconButton(
                        onClick = {
                            viewModel.deletePage(selectedPageIndex)
                            if (selectedPageIndex >= doc.pages.size - 1) {
                                selectedPageIndex = maxOf(0, doc.pages.size - 2)
                            }
                        },
                        modifier = Modifier.testTag("organizer_delete_button")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete page",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Drag and Drop Page Organizer Interactive Canvas
        DragDropPageOrganizer(
            pages = doc.pages,
            selectedPageIndex = selectedPageIndex,
            onSelectPage = { selectedPageIndex = it },
            onReorderPage = { fromIndex, toIndex ->
                viewModel.movePage(fromIndex, toIndex)
                selectedPageIndex = toIndex
            },
            onDuplicatePage = { pageIdx ->
                viewModel.duplicatePage(pageIdx)
                selectedPageIndex = pageIdx + 1
            },
            onDeletePage = { pageIdx ->
                viewModel.deletePage(pageIdx)
                if (selectedPageIndex >= doc.pages.size - 1) {
                    selectedPageIndex = maxOf(0, doc.pages.size - 2)
                }
            },
            onRotatePage = { pageIdx, degrees ->
                viewModel.rotatePage(pageIdx, degrees)
            },
            modifier = Modifier.weight(1f)
        )

        // Bottom action bar
        Surface(
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.setPageIndex(selectedPageIndex)
                        viewModel.navigateTo(WorkbenchNavDestination.VIEWER)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("organizer_preview_page_button")
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Preview Page")
                }

                Button(
                    onClick = {
                        viewModel.setPageIndex(selectedPageIndex)
                        viewModel.navigateTo(WorkbenchNavDestination.VIEWER)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("organizer_apply_changes_button")
                ) {
                    Text("Done")
                }
            }
        }
    }
}
