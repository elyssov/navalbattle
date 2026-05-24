package com.elyssov.navalbattle.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.elyssov.navalbattle.game.Orientation
import com.elyssov.navalbattle.game.Ship
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.unit.Density
import com.elyssov.navalbattle.game.Coord
import kotlin.math.max
import kotlin.math.min

data class GridCamera(
    var scale: Float = 1f,
    var offset: Offset = Offset.Zero,
    var fieldSize: Int = 50,
    var canvasSize: Size = Size.Zero
) {
    val cellPx: Float get() = if (fieldSize == 0) 0f else min(canvasSize.width, canvasSize.height) / fieldSize * scale

    fun fitToCanvas() {
        scale = 1f
        offset = Offset.Zero
    }

    fun toCell(screen: Offset): Coord {
        val cs = cellPx
        if (cs <= 0f) return Coord(-1, -1)
        val gx = ((screen.x - offset.x) / cs).toInt()
        val gy = ((screen.y - offset.y) / cs).toInt()
        return Coord(gx, gy)
    }

    fun cellRect(c: Coord): Pair<Offset, Size> {
        val cs = cellPx
        return Offset(offset.x + c.x * cs, offset.y + c.y * cs) to Size(cs, cs)
    }
}

@Composable
fun rememberGridCamera(fieldSize: Int): MutableState<GridCamera> =
    remember(fieldSize) { mutableStateOf(GridCamera(fieldSize = fieldSize)) }

/**
 * Reusable battlefield canvas with pinch-zoom, pan, double-tap-fit.
 *
 * minScale: 1f (fit-to-canvas), maxScale: 8f
 * Tap: onCellTap(c)
 * Long-press: onCellLong(c)
 */
@Composable
fun FleetGrid(
    camera: MutableState<GridCamera>,
    background: Color,
    modifier: Modifier = Modifier,
    onCellTap: (Coord) -> Unit = {},
    onCellLong: (Coord) -> Unit = {},
    draw: DrawScope.(GridCamera) -> Unit
) {
    var cam by camera

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .pointerInput(cam.fieldSize) {
                awaitEachGesture {
                    val first = awaitFirstDown(requireUnconsumed = false)
                    var pointerCount = 1
                    var didMove = false
                    val downTime = first.uptimeMillis
                    val downPos = first.position

                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        pointerCount = event.changes.count { it.pressed }
                        if (pointerCount == 0) break

                        val zoom = event.calculateZoom()
                        val pan = event.calculatePan()
                        val centroid = event.calculateCentroid()

                        if (pointerCount >= 2 && (zoom != 1f || pan != Offset.Zero)) {
                            didMove = true
                            val newScale = (cam.scale * zoom).coerceIn(1f, 8f)
                            val scaleFactor = newScale / cam.scale
                            val newOffset = (cam.offset - centroid) * scaleFactor + centroid + pan
                            cam = cam.copy(scale = newScale, offset = clampOffset(newOffset, newScale, cam.canvasSize, cam.fieldSize))
                            event.changes.forEach { it.consume() }
                        } else if (pointerCount == 1) {
                            val change = event.changes.first()
                            if (change.positionChanged()) {
                                val delta = change.position - change.previousPosition
                                if (delta.getDistance() > 4f) didMove = true
                                if (didMove && cam.scale > 1f) {
                                    val newOffset = cam.offset + delta
                                    cam = cam.copy(offset = clampOffset(newOffset, cam.scale, cam.canvasSize, cam.fieldSize))
                                    change.consume()
                                }
                            }
                        }
                    }

                    if (!didMove) {
                        val held = first.uptimeMillis - downTime
                        if (held > 350) onCellLong(cam.toCell(downPos))
                        else onCellTap(cam.toCell(downPos))
                    }
                }
            }
    ) {
        if (cam.canvasSize != size) {
            cam = cam.copy(canvasSize = size)
        }
        draw(cam)
    }
}

private fun clampOffset(o: Offset, scale: Float, size: Size, fieldSize: Int): Offset {
    if (fieldSize == 0 || size == Size.Zero) return o
    val cellPx = min(size.width, size.height) / fieldSize * scale
    val totalW = cellPx * fieldSize
    val totalH = cellPx * fieldSize
    val minX = if (totalW <= size.width) (size.width - totalW) / 2f else size.width - totalW
    val maxX = if (totalW <= size.width) (size.width - totalW) / 2f else 0f
    val minY = if (totalH <= size.height) (size.height - totalH) / 2f else size.height - totalH
    val maxY = if (totalH <= size.height) (size.height - totalH) / 2f else 0f
    return Offset(o.x.coerceIn(minX, maxX), o.y.coerceIn(minY, maxY))
}

/**
 * Draw the grid lines.
 */
fun DrawScope.drawGridLines(camera: GridCamera, color: Color, strokeWidth: Float = 1f) {
    val cs = camera.cellPx
    val n = camera.fieldSize
    val left = camera.offset.x
    val top = camera.offset.y
    for (i in 0..n) {
        drawLine(
            color = color,
            start = Offset(left, top + i * cs),
            end = Offset(left + n * cs, top + i * cs),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(left + i * cs, top),
            end = Offset(left + i * cs, top + n * cs),
            strokeWidth = strokeWidth
        )
    }
}

fun DrawScope.fillCell(camera: GridCamera, c: Coord, color: Color) {
    val (pos, sz) = camera.cellRect(c)
    drawRect(color = color, topLeft = pos, size = sz)
}

fun DrawScope.strokeCell(camera: GridCamera, c: Coord, color: Color, strokeWidth: Float = 2f) {
    val (pos, sz) = camera.cellRect(c)
    drawRect(color = color, topLeft = pos, size = sz, style = Stroke(width = strokeWidth))
}

fun DrawScope.drawShipSprite(camera: GridCamera, ship: Ship, sprite: ImageBitmap) {
    if (ship.cells.isEmpty()) return
    val cs = camera.cellPx
    val anchor = ship.cells.first()
    val (topLeft, _) = camera.cellRect(anchor)

    if (ship.orientation == Orientation.Horizontal) {
        drawImage(
            image = sprite,
            srcOffset = IntOffset(0, 0),
            srcSize = IntSize(sprite.width, sprite.height),
            dstOffset = IntOffset(topLeft.x.toInt(), topLeft.y.toInt()),
            dstSize = IntSize((cs * ship.size).toInt(), cs.toInt())
        )
    } else {
        withTransform({
            translate(left = topLeft.x + cs, top = topLeft.y)
            rotate(degrees = 90f, pivot = Offset.Zero)
        }) {
            drawImage(
                image = sprite,
                srcOffset = IntOffset(0, 0),
                srcSize = IntSize(sprite.width, sprite.height),
                dstOffset = IntOffset(0, 0),
                dstSize = IntSize((cs * ship.size).toInt(), cs.toInt())
            )
        }
    }
}
