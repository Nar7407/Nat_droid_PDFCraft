package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.ui.draw.clip
import com.example.ui.components.FreehandDrawingLayer
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PdfDocument
import com.example.data.model.PdfPage
import com.example.viewmodel.PdfCraftViewModel

@Composable
fun PdfViewerScreen(
    viewModel: PdfCraftViewModel
) {
    val activeDoc by viewModel.activeDocument.collectAsState()
    val currentPageIndex by viewModel.currentPageIndex.collectAsState()
    val isTwoUpView by viewModel.isTwoUpView.collectAsState()
    val zoomScale by viewModel.zoomScale.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val currentMatchIndex by viewModel.currentMatchIndex.collectAsState()
    val isOcrProcessing by viewModel.isOcrProcessing.collectAsState()
    val ocrProgress by viewModel.ocrProgress.collectAsState()
    val ocrSearchFilterActive by viewModel.ocrSearchFilterActive.collectAsState()
    val isDrawMode by viewModel.isDrawMode.collectAsState()
    val drawPenColor by viewModel.drawPenColor.collectAsState()
    val drawStrokeWidth by viewModel.drawStrokeWidth.collectAsState()
    val context = LocalContext.current

    var showSearchBar by remember { mutableStateOf(false) }

    if (activeDoc == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("pdf_viewer_empty_state"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No document selected. Return to Dashboard or open a PDF from device storage.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val doc = activeDoc!!
    val totalPages = doc.pages.size

    if (doc.isLocked) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("pdf_viewer_locked_state"),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Document Locked",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "'${doc.name}' is password-protected with the Android Keystore system. Please unlock to view pages.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.setPasswordUnlockDialogVisible(true) },
                        modifier = Modifier.testTag("unlock_locked_doc_viewer_button")
                    ) {
                        Text("Enter Password to Unlock")
                    }
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pdf_viewer_screen")
    ) {
        // Viewer Controls Header Bar
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Page Navigation
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.previousPage() },
                            enabled = currentPageIndex > 0,
                            modifier = Modifier.testTag("viewer_prev_page_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous page")
                        }
                        Text(
                            text = "${currentPageIndex + 1} / $totalPages",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        IconButton(
                            onClick = { viewModel.nextPage() },
                            enabled = currentPageIndex < totalPages - 1,
                            modifier = Modifier.testTag("viewer_next_page_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next page")
                        }
                    }

                    // Zoom and Two-Up Controls
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.zoomOut() },
                            enabled = zoomScale > 0.5f,
                            modifier = Modifier.testTag("viewer_zoom_out_button")
                        ) {
                            Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out")
                        }
                        Text(
                            text = "${(zoomScale * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { viewModel.resetZoom() }
                        )
                        IconButton(
                            onClick = { viewModel.zoomIn() },
                            enabled = zoomScale < 3.0f,
                            modifier = Modifier.testTag("viewer_zoom_in_button")
                        ) {
                            Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In")
                        }

                        // Toggle Two-Up View
                        IconButton(
                            onClick = { viewModel.toggleTwoUpView() },
                            modifier = Modifier.testTag("viewer_toggle_two_up_button")
                        ) {
                            Icon(
                                imageVector = if (isTwoUpView) Icons.Default.ViewAgenda else Icons.Default.ViewCarousel,
                                contentDescription = "Toggle Two-Up Layout",
                                tint = if (isTwoUpView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Search Toggle
                        IconButton(
                            onClick = { showSearchBar = !showSearchBar },
                            modifier = Modifier.testTag("viewer_toggle_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search text in document",
                                tint = if (showSearchBar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Freehand Draw / Canvas Annotation Mode Toggle
                        IconButton(
                            onClick = { viewModel.toggleDrawMode() },
                            modifier = Modifier.testTag("viewer_toggle_draw_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Draw,
                                contentDescription = "Toggle Freehand Draw Mode",
                                tint = if (isDrawMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // On-Device OCR Text Recognition Button
                        IconButton(
                            onClick = { viewModel.runOcrOnDocument(context) },
                            enabled = !isOcrProcessing,
                            modifier = Modifier.testTag("viewer_ocr_scan_button")
                        ) {
                            if (isOcrProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.DocumentScanner,
                                    contentDescription = "Run On-Device OCR Text Recognition",
                                    tint = if (doc.ocrProcessedPageCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Bookmarks Drawer
                        IconButton(
                            onClick = { viewModel.setShowBookmarksDialog(true) },
                            modifier = Modifier.testTag("viewer_open_bookmarks_button")
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = "Bookmarks")
                        }

                        // Document Properties
                        IconButton(
                            onClick = { viewModel.setShowPropertiesDialog(true) },
                            modifier = Modifier.testTag("viewer_open_properties_button")
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "Properties")
                        }
                    }
                }

                // OCR Progress Indicator Bar when scanning is running
                if (isOcrProcessing) {
                    LinearProgressIndicator(
                        progress = { ocrProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .testTag("viewer_ocr_progress_indicator")
                    )
                }

                // Search in Document Inline Bar
                if (showSearchBar) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                placeholder = { Text("Find text (embedded or OCR scanned)...") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("viewer_search_input")
                            )
                            Spacer(modifier = Modifier.width(6.dp))

                            // OCR Filter Chip toggle
                            FilterChip(
                                selected = ocrSearchFilterActive,
                                onClick = { viewModel.toggleOcrSearchFilter() },
                                label = { Text("OCR", fontSize = 11.sp) },
                                modifier = Modifier.testTag("viewer_search_ocr_filter_chip")
                            )

                            Spacer(modifier = Modifier.width(4.dp))
                            if (searchResults.isNotEmpty()) {
                                Text(
                                    text = "${currentMatchIndex + 1}/${searchResults.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { viewModel.previousSearchMatch() },
                                    modifier = Modifier.testTag("viewer_search_prev_match")
                                ) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Previous match")
                                }
                                IconButton(
                                    onClick = { viewModel.nextSearchMatch() },
                                    modifier = Modifier.testTag("viewer_search_next_match")
                                ) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next match")
                                }
                            } else if (searchQuery.isNotBlank()) {
                                Text(
                                    text = "No matches",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            IconButton(
                                onClick = {
                                    showSearchBar = false
                                    viewModel.updateSearchQuery("")
                                }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close search")
                            }
                        }

                        // Display active match indicator badge (OCR vs Text layer)
                        val activeMatch = searchResults.getOrNull(currentMatchIndex)
                        if (activeMatch != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (activeMatch.isOcrMatch) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
                                ) {
                                    Text(
                                        text = if (activeMatch.isOcrMatch) "MATCH VIA ON-DEVICE OCR: ${activeMatch.snippet}" else "MATCH: ${activeMatch.snippet}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (activeMatch.isOcrMatch) Color(0xFFE65100) else Color(0xFF2E7D32),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Scanned PDF Prompt Banner (shows if document is scanned image and OCR has not been run yet)
                val isScannedPage = doc.pages.getOrNull(currentPageIndex)?.isScannedImage == true || doc.isScannedDocument
                val hasOcrRun = doc.pages.getOrNull(currentPageIndex)?.isOcrProcessed == true || doc.ocrProcessedPageCount > 0
                if (isScannedPage && !hasOcrRun && !isOcrProcessing) {
                    Surface(
                        color = Color(0xFFFFF8E1),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .testTag("scanned_pdf_ocr_prompt_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.DocumentScanner,
                                    contentDescription = null,
                                    tint = Color(0xFFF57F17),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Scanned PDF image: Run on-device OCR to enable text search & extraction.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF5D4037)
                                )
                            }
                            Button(
                                onClick = { viewModel.runOcrOnDocument(context) },
                                modifier = Modifier.testTag("run_ocr_scanned_banner_button")
                            ) {
                                Text("Scan OCR", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        // PDF Page Rendering Canvas + Floating Drawing Toolbar
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE5E7EB))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .graphicsLayer(
                            scaleX = zoomScale,
                            scaleY = zoomScale
                        )
                ) {
                    if (isTwoUpView && currentPageIndex + 1 < totalPages) {
                        // Two-up side-by-side mode
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val page1 = doc.pages[currentPageIndex]
                            val page2 = doc.pages[currentPageIndex + 1]

                            Box(modifier = Modifier.weight(1f)) {
                                PdfPageCard(
                                    page = page1,
                                    watermark = doc.watermarkText,
                                    searchQuery = searchQuery,
                                    isDrawMode = isDrawMode,
                                    drawPenColor = drawPenColor,
                                    drawStrokeWidth = drawStrokeWidth,
                                    onDrawingStrokeFinished = { points, colorHex, strokeWidth ->
                                        viewModel.addDrawingStroke(page1.pageIndex, points, colorHex, strokeWidth)
                                    },
                                    onRunOcrPage = { viewModel.runOcrOnCurrentPage(context) }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                PdfPageCard(
                                    page = page2,
                                    watermark = doc.watermarkText,
                                    searchQuery = searchQuery,
                                    isDrawMode = isDrawMode,
                                    drawPenColor = drawPenColor,
                                    drawStrokeWidth = drawStrokeWidth,
                                    onDrawingStrokeFinished = { points, colorHex, strokeWidth ->
                                        viewModel.addDrawingStroke(page2.pageIndex, points, colorHex, strokeWidth)
                                    },
                                    onRunOcrPage = { viewModel.runOcrOnCurrentPage(context) }
                                )
                            }
                        }
                    } else {
                        // Single Page Mode
                        val page = doc.pages.getOrNull(currentPageIndex)
                        if (page != null) {
                            PdfPageCard(
                                page = page,
                                watermark = doc.watermarkText,
                                searchQuery = searchQuery,
                                isDrawMode = isDrawMode,
                                drawPenColor = drawPenColor,
                                drawStrokeWidth = drawStrokeWidth,
                                onDrawingStrokeFinished = { points, colorHex, strokeWidth ->
                                    viewModel.addDrawingStroke(page.pageIndex, points, colorHex, strokeWidth)
                                },
                                onRunOcrPage = { viewModel.runOcrOnCurrentPage(context) }
                            )
                        }
                    }
                }
            }

            // Floating Freehand Markup Toolbar
            if (isDrawMode) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                        .testTag("viewer_markup_floating_toolbar")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Draw,
                            contentDescription = null,
                            tint = drawPenColor,
                            modifier = Modifier.size(20.dp)
                        )

                        // Color swatches
                        val quickColors = listOf(
                            Color(0xFFE11D48), // Crimson
                            Color(0xFF2563EB), // Blue
                            Color(0xFF16A34A), // Green
                            Color(0xFF1F2937), // Black
                            Color(0xFFF59E0B)  // Amber
                        )
                        quickColors.forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(
                                        width = if (drawPenColor == c) 2.5.dp else 1.dp,
                                        color = if (drawPenColor == c) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.setDrawPenColor(c) }
                            )
                        }

                        VerticalDivider(modifier = Modifier.height(20.dp))

                        // Stroke Thickness Quick Toggles
                        listOf(3f to "Fine", 6f to "Med", 14f to "Thick").forEach { (width, label) ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (drawStrokeWidth == width) FontWeight.Bold else FontWeight.Normal,
                                color = if (drawStrokeWidth == width) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier
                                    .clickable { viewModel.setDrawStrokeWidth(width) }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        VerticalDivider(modifier = Modifier.height(20.dp))

                        // Undo stroke
                        IconButton(
                            onClick = { viewModel.removeLastAnnotation(currentPageIndex) },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("viewer_draw_undo_button")
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = "Undo stroke", modifier = Modifier.size(18.dp))
                        }

                        // Clear drawings
                        IconButton(
                            onClick = { viewModel.clearDrawingsOnPage(currentPageIndex) },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("viewer_draw_clear_button")
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear drawings", modifier = Modifier.size(18.dp))
                        }

                        // Done
                        IconButton(
                            onClick = { viewModel.setDrawMode(false) },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("viewer_draw_done_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Done", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        // Thumbnails Strip
        Surface(
            tonalElevation = 3.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(doc.pages) { index, page ->
                    val isSelected = index == currentPageIndex
                    Card(
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White
                        ),
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(0.70f)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { viewModel.setPageIndex(index) }
                            .testTag("thumbnail_page_$index")
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "p. ${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray
                                )
                                if (page.rotationDegrees != 0) {
                                    Text(
                                        text = "${page.rotationDegrees}°",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfPageCard(
    page: PdfPage,
    watermark: String?,
    searchQuery: String,
    isDrawMode: Boolean = false,
    drawPenColor: Color = Color(0xFFE11D48),
    drawStrokeWidth: Float = 6f,
    onDrawingStrokeFinished: ((List<Pair<Float, Float>>, String, Float) -> Unit)? = null,
    onRunOcrPage: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.707f) // ISO 216 standard A4 ratio
            .rotate(page.rotationDegrees.toFloat())
            .testTag("pdf_rendered_page_${page.pageIndex}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Document Page Simulation
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    // Scanned & OCR status badges
                    if (page.isScannedImage) {
                        if (page.isOcrProcessed) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFE8F5E9)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DocumentScanner,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "OCR Active",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF1B5E20),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFFF3E0)
                            ) {
                                Text(
                                    text = "Scanned Image",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFE65100),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Page text content or OCR content
                if (page.isOcrProcessed && !page.ocrText.isNullOrBlank()) {
                    Text(
                        text = "On-Device OCR Recognized Text:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = page.ocrText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1F2937),
                        lineHeight = 17.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.testTag("page_ocr_recognized_text_${page.pageIndex}")
                    )
                } else {
                    Text(
                        text = page.textSnippet,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1F2937),
                        lineHeight = 18.sp,
                        fontFamily = FontFamily.Serif
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Diagram / Scanned image visualization box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(if (page.isScannedImage) Color(0xFFFAF7F2) else Color(0xFFF9FAFB), RoundedCornerShape(4.dp))
                        .border(1.dp, if (page.isScannedImage) Color(0xFFE2D9CC) else Color(0xFFE5E7EB), RoundedCornerShape(4.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        if (page.isScannedImage) {
                            Text(
                                text = "Optical Scan Raster Layer [DPI: 300 • Format: 1-bit CCITT Group 4]",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF78716C),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (page.isOcrProcessed) {
                                    "• On-device ML Kit engine indexed optical glyphs.\n• Searchable text vector generated and queryable.\n• Integrity: Verified local optical character recognition."
                                } else {
                                    "• Image-based PDF page: No embedded font glyph stream.\n• Characters cannot be searched without optical text recognition.\n• Tap 'Run OCR' to process text locally on this device."
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF44403C)
                            )
                            if (!page.isOcrProcessed && onRunOcrPage != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onRunOcrPage,
                                    modifier = Modifier.testTag("card_run_ocr_button_${page.pageIndex}")
                                ) {
                                    Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Run Page OCR", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        } else {
                            Text(
                                text = "Vector Rendering Pipeline [Resolution: ${page.width}x${page.height} pt]",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• Embedded typography: TrueType / OpenType font subsets\n• Color Space: DeviceRGB calibrated (ICC Profile 2.4)\n• MediaBox: [0 0 ${page.width} ${page.height}] pt",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.DarkGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (page.isOcrProcessed) "OCR Engine: ML Kit On-Device" else "PdfCraft Engine 2026",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                    Text(
                        text = "Page ${page.pageIndex + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
            }

            // Watermark overlay if configured
            if (!watermark.isNullOrBlank()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = watermark,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Red.copy(alpha = 0.22f),
                        fontSize = 38.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.rotate(-35f)
                    )
                }
            }

            // Canvas-based Freehand Annotation Layer
            FreehandDrawingLayer(
                page = page,
                isDrawMode = isDrawMode,
                selectedColor = drawPenColor,
                strokeWidth = drawStrokeWidth,
                onStrokeFinished = { points, colorHex, strokeWidth ->
                    onDrawingStrokeFinished?.invoke(points, colorHex, strokeWidth)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
