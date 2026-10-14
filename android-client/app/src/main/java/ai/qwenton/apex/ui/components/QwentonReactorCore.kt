package ai.qwenton.apex.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ai.qwenton.apex.ui.theme.*

enum class ReactorState {
    IDLE,
    LISTENING,
    THINKING,
    EXECUTING,
    APPROVAL_REQUIRED
}

@Composable
fun QwentonReactorCore(
    state: ReactorState,
    modifier: Modifier = Modifier,
    size: Dp = 110.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ReactorCoreTransition")

    // Rotation angle for telemetry orbit rings
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    ReactorState.THINKING -> 2000
                    ReactorState.EXECUTING -> 1400
                    ReactorState.LISTENING -> 3500
                    ReactorState.APPROVAL_REQUIRED -> 1000
                    ReactorState.IDLE -> 8000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbitRotation"
    )

    // Breathing pulse scale for the core
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    ReactorState.THINKING -> 450
                    ReactorState.LISTENING -> 300
                    ReactorState.APPROVAL_REQUIRED -> 250
                    ReactorState.EXECUTING -> 600
                    ReactorState.IDLE -> 1800
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    // Core and accent colors based on state
    val coreColor = when (state) {
        ReactorState.IDLE -> PrimaryViolet
        ReactorState.LISTENING -> StatusCyan
        ReactorState.THINKING -> PrimaryVioletLight
        ReactorState.EXECUTING -> StatusGreen
        ReactorState.APPROVAL_REQUIRED -> StatusAmber
    }

    val orbitColor = when (state) {
        ReactorState.IDLE -> PrimaryVioletSubtle
        ReactorState.LISTENING -> StatusCyan.copy(alpha = 0.4f)
        ReactorState.THINKING -> PrimaryViolet.copy(alpha = 0.6f)
        ReactorState.EXECUTING -> StatusGreen.copy(alpha = 0.5f)
        ReactorState.APPROVAL_REQUIRED -> StatusAmber.copy(alpha = 0.7f)
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 2f

            // 1. Outer Static Alignment Guide Ring
            drawCircle(
                color = CharcoalBorder,
                radius = baseRadius * 0.94f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 2. Rotating Segmented Telemetry Ring
            rotate(degrees = rotationAngle, pivot = center) {
                // Segment 1
                drawArc(
                    color = orbitColor,
                    startAngle = 0f,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = Offset(center.x - baseRadius * 0.82f, center.y - baseRadius * 0.82f),
                    size = androidx.compose.ui.geometry.Size(baseRadius * 1.64f, baseRadius * 1.64f),
                    style = Stroke(width = 2.5.dp.toPx())
                )
                // Segment 2
                drawArc(
                    color = orbitColor,
                    startAngle = 120f,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = Offset(center.x - baseRadius * 0.82f, center.y - baseRadius * 0.82f),
                    size = androidx.compose.ui.geometry.Size(baseRadius * 1.64f, baseRadius * 1.64f),
                    style = Stroke(width = 2.5.dp.toPx())
                )
                // Segment 3
                drawArc(
                    color = orbitColor,
                    startAngle = 240f,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = Offset(center.x - baseRadius * 0.82f, center.y - baseRadius * 0.82f),
                    size = androidx.compose.ui.geometry.Size(baseRadius * 1.64f, baseRadius * 1.64f),
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }

            // 3. Counter-Rotating Inner Gyro Ring
            rotate(degrees = -rotationAngle * 1.4f, pivot = center) {
                drawArc(
                    color = coreColor.copy(alpha = 0.5f),
                    startAngle = 45f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(center.x - baseRadius * 0.62f, center.y - baseRadius * 0.62f),
                    size = androidx.compose.ui.geometry.Size(baseRadius * 1.24f, baseRadius * 1.24f),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawArc(
                    color = coreColor.copy(alpha = 0.5f),
                    startAngle = 225f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(center.x - baseRadius * 0.62f, center.y - baseRadius * 0.62f),
                    size = androidx.compose.ui.geometry.Size(baseRadius * 1.24f, baseRadius * 1.24f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // 4. Solid Central Core (No Gradients - Pure OKLCH Depth)
            val currentCoreRadius = baseRadius * 0.36f * pulseScale
            drawCircle(
                color = coreColor,
                radius = currentCoreRadius,
                center = center
            )

            // 5. Central Telemetry Dot
            drawCircle(
                color = DarkCharcoalBg,
                radius = currentCoreRadius * 0.35f,
                center = center
            )
        }
    }
}
