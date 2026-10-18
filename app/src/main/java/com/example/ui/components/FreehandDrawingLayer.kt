package com.example.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.data.model.AnnotationType
import com.example.data.model.PdfAnnotation
import com.example.data.model.PdfPage

@Composable
fun FreehandDrawingLayer(
    page: PdfPage,
    isDrawMode: Boolean,
    selectedColor: Color,
    strokeWidth: Float,
    onStrokeFinished: (points: List<Pair<Float, Float>>, colorHex: String, strokeWidth: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val livePoints = remember { mutableStateListOf<Pair<Float, Float>>() }

    val colorHex = remember(selectedColor) {
        val r = (selectedColor.red * 255).toInt()
        val g = (selectedColor.green * 255).toInt()
        val b = (selectedColor.blue * 255).toInt()
        String.format("#%02X%02X%02X", r, g, b)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("freehand_drawing_layer_${page.pageIndex}")
    ) {
        val inputModifier = if (isDrawMode) {
            Modifier
                .fillMaxSize()
                .pointerInput(page.pageIndex, isDrawMode, selectedColor, strokeWidth) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            livePoints.clear()
                            livePoints.add(Pair(offset.x, offset.y))
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            livePoints.add(Pair(change.position.x, change.position.y))
                        },
                        onDragEnd = {
                            if (livePoints.isNotEmpty()) {
                                onStrokeFinished(livePoints.toList(), colorHex, strokeWidth)
                                livePoints.clear()
                            }
                        },
                        onDragCancel = {
                            livePoints.clear()
                        }
                    )
                }
        } else {
            Modifier.fillMaxSize()
        }

        Canvas(
            modifier = inputModifier.testTag("drawing_canvas_${page.pageIndex}")
        ) {
            // 1. Draw existing highlight annotations
            page.annotations.filter { it.type == AnnotationType.HIGHLIGHT }.forEach { ann ->
                val highlightColor = parseHexColor(ann.colorHex).copy(alpha = 0.35f)
                val topY = if (ann.y > 0f) ann.y else size.height * 0.2f
                val h = if (ann.height > 0f) ann.height else 28f
                drawRect(
                    color = highlightColor,
                    topLeft = Offset(ann.x.coerceAtLeast(20f), topY),
                    size = androidx.compose.ui.geometry.Size(
                        (ann.width).coerceIn(100f, size.width - 40f),
                        h
                    )
                )
            }

            // 2. Draw existing freehand drawing strokes
            page.annotations.filter { it.type == AnnotationType.DRAWING }.forEach { ann ->
                val strokeColor = parseHexColor(ann.colorHex)
                if (ann.strokePoints.size > 1) {
                    val path = Path()
                    val first = ann.strokePoints.first()
                    path.moveTo(first.first, first.second)
                    for (i in 1 until ann.strokePoints.size) {
                        val pt = ann.strokePoints[i]
                        path.lineTo(pt.first, pt.second)
                    }
                    drawPath(
                        path = path,
                        color = strokeColor,
                        style = Stroke(
                            width = ann.strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                } else if (ann.strokePoints.size == 1) {
                    val single = ann.strokePoints.first()
                    drawCircle(
                        color = strokeColor,
                        radius = ann.strokeWidth / 2f,
                        center = Offset(single.first, single.second)
                    )
                }
            }

            // 3. Draw live active stroke currently being drawn by user
            if (isDrawMode && livePoints.isNotEmpty()) {
                if (livePoints.size > 1) {
                    val livePath = Path()
                    val first = livePoints.first()
                    livePath.moveTo(first.first, first.second)
                    for (i in 1 until livePoints.size) {
                        val pt = livePoints[i]
                        livePath.lineTo(pt.first, pt.second)
                    }
                    drawPath(
                        path = livePath,
                        color = selectedColor,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                } else if (livePoints.size == 1) {
                    val single = livePoints.first()
                    drawCircle(
                        color = selectedColor,
                        radius = strokeWidth / 2f,
                        center = Offset(single.first, single.second)
                    )
                }
            }
        }
    }
}

private fun parseHexColor(hex: String): Color {
    return try {
        Color(AndroidColor.parseColor(hex))
    } catch (_: Exception) {
        Color(0xFFFFEB3B)
    }
}
