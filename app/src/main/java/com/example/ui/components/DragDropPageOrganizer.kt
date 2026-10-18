package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.model.PdfPage
import kotlin.math.roundToInt

@Composable
fun DragDropPageOrganizer(
    pages: List<PdfPage>,
    selectedPageIndex: Int,
    onSelectPage: (Int) -> Unit,
    onReorderPage: (fromIndex: Int, toIndex: Int) -> Unit,
    onDuplicatePage: (Int) -> Unit,
    onDeletePage: (Int) -> Unit,
    onRotatePage: (pageIndex: Int, degrees: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var hoverTargetIndex by remember { mutableStateOf<Int?>(null) }
    var isOverTrashZone by remember { mutableStateOf(false) }

    // Map of card bounds in root coordinates for drop collision detection
    val itemPositions = remember { mutableMapOf<Int, Pair<Offset, Pair<Float, Float>>>() }
    var trashZoneTopY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("drag_drop_page_organizer")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Drag Drop Instructions Banner
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DragIndicator,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Long-press & drag to reorder • Tap buttons to duplicate or delete",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Text(
                        text = "${pages.size} pages",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Grid of Pages
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("drag_drop_grid")
            ) {
                itemsIndexed(pages) { index, page ->
                    val isCurrentDragged = draggingIndex == index
                    val isHoveredTarget = hoverTargetIndex == index && !isCurrentDragged
                    val isSelected = selectedPageIndex == index

                    val cardScale by animateFloatAsState(
                        targetValue = if (isCurrentDragged) 1.08f else if (isHoveredTarget) 1.04f else 1.0f,
                        label = "cardScale"
                    )
                    val cardElevation by animateDpAsState(
                        targetValue = if (isCurrentDragged) 14.dp else if (isSelected) 4.dp else 1.dp,
                        label = "cardElevation"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(if (isCurrentDragged) 10f else 1f)
                            .onGloballyPositioned { coords ->
                                val pos = coords.positionInRoot()
                                itemPositions[index] = Pair(pos, Pair(coords.size.width.toFloat(), coords.size.height.toFloat()))
                            }
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isHoveredTarget -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                    else -> Color.White
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.70f)
                                .graphicsLayer {
                                    scaleX = cardScale
                                    scaleY = cardScale
                                    if (isCurrentDragged) {
                                        translationX = dragOffsetX
                                        translationY = dragOffsetY
                                    }
                                }
                                .border(
                                    width = when {
                                        isHoveredTarget -> 2.5.dp
                                        isSelected -> 2.dp
                                        else -> 1.dp
                                    },
                                    color = when {
                                        isHoveredTarget -> MaterialTheme.colorScheme.primary
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        else -> Color(0xFFD1D5DB)
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectPage(index) }
                                .pointerInput(index) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggingIndex = index
                                            dragOffsetX = 0f
                                            dragOffsetY = 0f
                                            onSelectPage(index)
                                        },
                                        onDragEnd = {
                                            val from = draggingIndex
                                            val to = hoverTargetIndex
                                            val overTrash = isOverTrashZone

                                            if (from != null && overTrash) {
                                                onDeletePage(from)
                                            } else if (from != null && to != null && from != to) {
                                                onReorderPage(from, to)
                                            }
                                            draggingIndex = null
                                            hoverTargetIndex = null
                                            dragOffsetX = 0f
                                            dragOffsetY = 0f
                                            isOverTrashZone = false
                                        },
                                        onDragCancel = {
                                            draggingIndex = null
                                            hoverTargetIndex = null
                                            dragOffsetX = 0f
                                            dragOffsetY = 0f
                                            isOverTrashZone = false
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffsetX += dragAmount.x
                                            dragOffsetY += dragAmount.y

                                            // Determine current pointer position in root
                                            val currentCardPos = itemPositions[index]?.first ?: Offset.Zero
                                            val pointerX = currentCardPos.x + dragOffsetX
                                            val pointerY = currentCardPos.y + dragOffsetY

                                            // Check trash zone collision
                                            isOverTrashZone = trashZoneTopY > 0f && pointerY >= trashZoneTopY - 80f

                                            // Calculate collision with other items
                                            var foundTarget: Int? = null
                                            itemPositions.forEach { (targetIdx, posSize) ->
                                                val (targetPos, size) = posSize
                                                val (w, h) = size
                                                if (pointerX in targetPos.x..(targetPos.x + w) &&
                                                    pointerY in targetPos.y..(targetPos.y + h)
                                                ) {
                                                    foundTarget = targetIdx
                                                }
                                            }
                                            hoverTargetIndex = foundTarget
                                        }
                                    )
                                }
                                .testTag("organizer_page_tile_$index")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Header: Page Number Badge & Drag Grip Handle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF374151)
                                    ) {
                                        Text(
                                            text = "p. ${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (page.rotationDegrees != 0) {
                                            Text(
                                                text = "${page.rotationDegrees}°",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(end = 4.dp)
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.DragIndicator,
                                            contentDescription = "Drag Handle",
                                            tint = if (isCurrentDragged) MaterialTheme.colorScheme.primary else Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                // Miniature Page Simulation Thumbnail Canvas
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(vertical = 4.dp)
                                        .rotate(page.rotationDegrees.toFloat())
                                        .background(
                                            if (page.isScannedImage) Color(0xFFFAF7F2) else Color(0xFFF9FAFB),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (page.isScannedImage) Color(0xFFE2D9CC) else Color(0xFFE5E7EB),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = page.title,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = Color.Black,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (page.isScannedImage) "[Scanned Image]" else "${page.width}x${page.height}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 8.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                // Quick Action Controls: Duplicate, Rotate, Delete, and Left/Right Shift
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Row of Actions: Rotate, Duplicate, Delete
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Rotate 90
                                        IconButton(
                                            onClick = { onRotatePage(index, 90) },
                                            modifier = Modifier
                                                .size(26.dp)
                                                .testTag("page_rotate_button_$index")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.RotateRight,
                                                contentDescription = "Rotate page 90°",
                                                modifier = Modifier.size(16.dp),
                                                tint = Color.DarkGray
                                            )
                                        }

                                        // Duplicate
                                        IconButton(
                                            onClick = { onDuplicatePage(index) },
                                            modifier = Modifier
                                                .size(26.dp)
                                                .testTag("page_duplicate_button_$index")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Duplicate page",
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        // Delete
                                        IconButton(
                                            onClick = { onDeletePage(index) },
                                            enabled = pages.size > 1,
                                            modifier = Modifier
                                                .size(26.dp)
                                                .testTag("page_delete_button_$index")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete page",
                                                modifier = Modifier.size(16.dp),
                                                tint = if (pages.size > 1) MaterialTheme.colorScheme.error else Color.LightGray
                                            )
                                        }
                                    }

                                    // Position Shift Arrows (Left / Right)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (index > 0) onReorderPage(index, index - 1)
                                            },
                                            enabled = index > 0,
                                            modifier = Modifier
                                                .size(22.dp)
                                                .testTag("page_move_left_$index")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Move Left",
                                                modifier = Modifier.size(14.dp),
                                                tint = if (index > 0) Color.DarkGray else Color.LightGray
                                            )
                                        }

                                        Text(
                                            text = "Reorder",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 8.sp,
                                            color = Color.Gray
                                        )

                                        IconButton(
                                            onClick = {
                                                if (index < pages.size - 1) onReorderPage(index, index + 1)
                                            },
                                            enabled = index < pages.size - 1,
                                            modifier = Modifier
                                                .size(22.dp)
                                                .testTag("page_move_right_$index")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "Move Right",
                                                modifier = Modifier.size(14.dp),
                                                tint = if (index < pages.size - 1) Color.DarkGray else Color.LightGray
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Target insertion halo indicator when another item is hovering
                        if (isHoveredTarget) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.20f))
                                    .border(2.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "Drop Here",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Trash Drop Target Bar (Reveals smoothly during active dragging)
        AnimatedVisibility(
            visible = draggingIndex != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .onGloballyPositioned { coords ->
                    trashZoneTopY = coords.positionInRoot().y
                }
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isOverTrashZone) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.errorContainer,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .border(
                        width = if (isOverTrashZone) 3.dp else 1.5.dp,
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .testTag("drag_trash_drop_zone")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Trash Drop Target",
                        tint = if (isOverTrashZone) Color.White else MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isOverTrashZone) "Release to Delete Page ${(draggingIndex ?: 0) + 1}" else "Drop here to Delete Page",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverTrashZone) Color.White else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
