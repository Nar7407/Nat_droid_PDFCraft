package com.example.ui.screens

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnnotationType
import com.example.data.model.PdfAnnotation
import com.example.ui.components.FreehandDrawingLayer
import com.example.viewmodel.PdfCraftViewModel
import java.util.UUID

@Composable
fun AnnotationsScreen(
    viewModel: PdfCraftViewModel
) {
    val activeDoc by viewModel.activeDocument.collectAsState()
    val currentPageIndex by viewModel.currentPageIndex.collectAsState()

    var selectedToolIndex by remember { mutableIntStateOf(0) } // 0: Pen Drawing, 1: Highlight, 2: Sticky Note, 3: Signature

    // Drawing Tool State
    var penColorHex by remember { mutableStateOf("#E11D48") } // Vivid Crimson
    var penStrokeWidth by remember { mutableFloatStateOf(6f) }

    // Highlight Tool State
    var highlightColorHex by remember { mutableStateOf("#FACC15") } // Yellow

    var noteContent by remember { mutableStateOf("") }
    var signatureName by remember { mutableStateOf("Elena Rostova, CFA") }

    val drawingColors = listOf(
        "#E11D48", // Vivid Crimson
        "#2563EB", // Royal Blue
        "#16A34A", // Forest Green
        "#D97706", // Amber
        "#7C3AED", // Royal Purple
        "#1F2937"  // Onyx Black
    )

    val strokeWidthOptions = listOf(
        Pair("Fine (3px)", 3f),
        Pair("Medium (6px)", 6f),
        Pair("Broad (12px)", 12f),
        Pair("Marker (24px)", 24f)
    )

    val highlightColors = listOf(
        "#FACC15", // Yellow
        "#4ADE80", // Green
        "#60A5FA", // Blue
        "#F472B6", // Pink
        "#FB923C"  // Orange
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("annotations_screen")
    ) {
        Text(
            text = "Annotate & Markup",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Canvas-based freehand drawing with pen color & stroke thickness, highlighters, notes, and signatures.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Tool Mode Tabs
        TabRow(selectedTabIndex = selectedToolIndex) {
            Tab(
                selected = selectedToolIndex == 0,
                onClick = { selectedToolIndex = 0 },
                text = { Text("Draw") },
                icon = { Icon(Icons.Default.Draw, contentDescription = null) },
                modifier = Modifier.testTag("tab_tool_drawing")
            )
            Tab(
                selected = selectedToolIndex == 1,
                onClick = { selectedToolIndex = 1 },
                text = { Text("Highlight") },
                icon = { Icon(Icons.Default.FormatColorFill, contentDescription = null) },
                modifier = Modifier.testTag("tab_tool_highlight")
            )
            Tab(
                selected = selectedToolIndex == 2,
                onClick = { selectedToolIndex = 2 },
                text = { Text("Note") },
                icon = { Icon(Icons.Default.StickyNote2, contentDescription = null) },
                modifier = Modifier.testTag("tab_tool_note")
            )
            Tab(
                selected = selectedToolIndex == 3,
                onClick = { selectedToolIndex = 3 },
                text = { Text("Signature") },
                icon = { Icon(Icons.Default.Gesture, contentDescription = null) },
                modifier = Modifier.testTag("tab_tool_signature")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tool Config Box
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                when (selectedToolIndex) {
                    0 -> {
                        // Freehand Draw Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pen Color & Thickness",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row {
                                IconButton(
                                    onClick = { viewModel.removeLastAnnotation(currentPageIndex) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("drawing_undo_stroke_button")
                                ) {
                                    Icon(Icons.Default.Undo, contentDescription = "Undo stroke", modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { viewModel.clearDrawingsOnPage(currentPageIndex) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("drawing_clear_strokes_button")
                                ) {
                                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear drawings", modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Pen Color Chips
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            drawingColors.forEach { hex ->
                                val color = Color(AndroidColor.parseColor(hex))
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (penColorHex == hex) 3.dp else 1.dp,
                                            color = if (penColorHex == hex) MaterialTheme.colorScheme.primary else Color.LightGray,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            penColorHex = hex
                                            viewModel.setDrawPenColor(color)
                                        }
                                        .testTag("pen_color_chip_$hex"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (penColorHex == hex) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stroke Thickness Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            strokeWidthOptions.forEach { (label, width) ->
                                val isSelected = penStrokeWidth == width
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        penStrokeWidth = width
                                        viewModel.setDrawStrokeWidth(width)
                                    },
                                    label = { Text(label, fontSize = 11.sp) },
                                    modifier = Modifier.testTag("stroke_width_chip_${width.toInt()}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Touch and drag directly on the canvas page below to draw freehand.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    1 -> {
                        // Highlight
                        Text(
                            text = "Highlight Palette",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            highlightColors.forEach { hex ->
                                val color = Color(AndroidColor.parseColor(hex))
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (highlightColorHex == hex) 3.dp else 1.dp,
                                            color = if (highlightColorHex == hex) MaterialTheme.colorScheme.primary else Color.LightGray,
                                            shape = CircleShape
                                        )
                                        .clickable { highlightColorHex = hex },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (highlightColorHex == hex) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.addAnnotation(
                                    PdfAnnotation(
                                        id = "ann_${UUID.randomUUID()}",
                                        type = AnnotationType.HIGHLIGHT,
                                        pageIndex = currentPageIndex,
                                        colorHex = highlightColorHex,
                                        content = "Highlighted section"
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("apply_highlight_button")
                        ) {
                            Text("Apply Highlight on Page ${currentPageIndex + 1}")
                        }
                    }

                    2 -> {
                        // Sticky Note
                        OutlinedTextField(
                            value = noteContent,
                            onValueChange = { noteContent = it },
                            label = { Text("Note Comment") },
                            placeholder = { Text("E.g. Confirm tax liability with accountant...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("note_comment_input")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (noteContent.isNotBlank()) {
                                    viewModel.addAnnotation(
                                        PdfAnnotation(
                                            id = "ann_${UUID.randomUUID()}",
                                            type = AnnotationType.NOTE,
                                            pageIndex = currentPageIndex,
                                            content = noteContent
                                        )
                                    )
                                    noteContent = ""
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_note_button")
                        ) {
                            Text("Attach Sticky Note to Page ${currentPageIndex + 1}")
                        }
                    }

                    3 -> {
                        // Signature
                        OutlinedTextField(
                            value = signatureName,
                            onValueChange = { signatureName = it },
                            label = { Text("Signatory Legal Name") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signatory_name_input")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                viewModel.addAnnotation(
                                    PdfAnnotation(
                                        id = "ann_${UUID.randomUUID()}",
                                        type = AnnotationType.SIGNATURE,
                                        pageIndex = currentPageIndex,
                                        content = signatureName
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("apply_signature_button")
                        ) {
                            Text("Stamp Digital Signature on Page ${currentPageIndex + 1}")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Document Page Preview with Canvas-Based Freehand Annotation Layer
        val currentPage = activeDoc?.pages?.getOrNull(currentPageIndex)
        if (currentPage != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Page ${currentPageIndex + 1} Interactive Canvas (${currentPage.annotations.size} annotations)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (currentPage.annotations.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { viewModel.clearAnnotationsOnPage(currentPageIndex) },
                        modifier = Modifier.testTag("clear_all_page_annotations_button")
                    ) {
                        Text("Clear All", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Page Content Representation
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = currentPage.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (currentPage.isOcrProcessed && !currentPage.ocrText.isNullOrBlank()) {
                                currentPage.ocrText
                            } else {
                                currentPage.textSnippet
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Text tags of non-drawing annotations
                        currentPage.annotations.filter { it.type != AnnotationType.DRAWING }.forEach { ann ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (ann.type) {
                                    AnnotationType.HIGHLIGHT -> Color(AndroidColor.parseColor(ann.colorHex)).copy(alpha = 0.30f)
                                    AnnotationType.NOTE -> Color(0xFFFEF3C7)
                                    AnnotationType.SIGNATURE -> Color(0xFFEFF6FF)
                                    else -> Color(0xFFF3F4F6)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "[${ann.type.name}] ",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = ann.content,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }

                    // Canvas-Based Freehand Annotation Drawing Layer Overlaid
                    FreehandDrawingLayer(
                        page = currentPage,
                        isDrawMode = selectedToolIndex == 0,
                        selectedColor = Color(AndroidColor.parseColor(penColorHex)),
                        strokeWidth = penStrokeWidth,
                        onStrokeFinished = { points, colorHex, strokeWidth ->
                            viewModel.addDrawingStroke(currentPage.pageIndex, points, colorHex, strokeWidth)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
