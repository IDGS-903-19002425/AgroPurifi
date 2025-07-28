package com.android.agropurrifi

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PHScreen() {
    var estatusConexion by remember { mutableStateOf("Desconectado") }
    var ph by remember { mutableStateOf("7.0") }
    var estadoAgua by remember { mutableStateOf("Desconocido") }

    LaunchedEffect(Unit){
        MQTTManager.conectarPH(
            onMessage = { nuevoValor ->
                ph = nuevoValor
                val phDouble = ph.toDoubleOrNull() ?: 7.0
                estadoAgua = when{
                    phDouble < 6.0 -> "Agua muy ácida - No recomendada"
                    phDouble in 6.0..6.5 -> "Agua ligeramente ácida - Aceptable"
                    phDouble in 6.5..7.5 -> "Agua ideal para riego"
                    phDouble in 7.5..8.5 -> "Agua ligeramente alcalina - Aceptable"
                    else -> "Agua muy alcalina - No recomendada"
                }
            },
            onStatus = { status -> estatusConexion = status}
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E3A8A),
                        Color(0xFF3B82F6),
                        Color(0xFF60A5FA)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Monitor de pH",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        Text(
                            text = "Acidez del Agua",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }

            // pH Gauge
            val phDouble = ph.toDoubleOrNull() ?: 7.0
            PHGauge(phValue = phDouble)

            // pH Value Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Valor Actual",
                        fontSize = 16.sp,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format("%.2f", phDouble),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = getPhColor(phDouble)
                    )
                    Text(
                        text = "pH",
                        fontSize = 18.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        phDouble < 6.0 || phDouble > 8.5 -> Color(0xFFEF4444).copy(alpha = 0.1f)
                        phDouble in 6.0..6.5 || phDouble in 7.5..8.5 -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                        else -> Color(0xFF10B981).copy(alpha = 0.1f)
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Estado del Agua",
                        fontSize = 16.sp,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = estadoAgua,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = getPhColor(phDouble),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Connection Status
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val connectionColor = when {
                        estatusConexion.contains("Conectado") -> Color(0xFF10B981)
                        estatusConexion.contains("Error") -> Color(0xFFEF4444)
                        else -> Color(0xFFF59E0B)
                    }

                    Canvas(
                        modifier = Modifier.size(12.dp)
                    ) {
                        drawCircle(
                            color = connectionColor,
                            radius = size.minDimension / 2
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Estado de Conexión",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )
                        Text(
                            text = estatusConexion,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = connectionColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PHGauge(phValue: Double) {
    Card(
        modifier = Modifier.size(250.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.size(200.dp)
            ) {
                drawPHGauge(phValue)
            }
        }
    }
}

fun DrawScope.drawPHGauge(phValue: Double) {
    val center = Offset(size.width / 2, size.height / 2)
    val radius = size.minDimension / 2 - 20.dp.toPx()

    // Dibujar el arco de fondo
    drawArc(
        color = Color(0xFFE5E7EB),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        style = Stroke(width = 20.dp.toPx()),
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2)
    )

    // Calcular el ángulo para el valor de pH (0-14 mapeado a 0-180 grados)
    val normalizedPH = (phValue.coerceIn(0.0, 14.0) / 14.0 * 180.0).toFloat()

    // Color del arco basado en el pH
    val arcColor = when {
        phValue < 6.0 -> Color(0xFFEF4444)
        phValue in 6.0..6.5 -> Color(0xFFF59E0B)
        phValue in 6.5..7.5 -> Color(0xFF10B981)
        phValue in 7.5..8.5 -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    // Dibujar el arco del valor actual
    drawArc(
        color = arcColor,
        startAngle = 180f,
        sweepAngle = normalizedPH,
        useCenter = false,
        style = Stroke(width = 20.dp.toPx()),
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2)
    )

    // Dibujar marcas de escala
    for (i in 0..14 step 2) {
        val angle = (i / 14.0 * PI).toFloat()
        val startRadius = radius - 10.dp.toPx()
        val endRadius = radius + 10.dp.toPx()

        val startX = center.x + startRadius * cos(angle + PI.toFloat())
        val startY = center.y + startRadius * sin(angle + PI.toFloat())
        val endX = center.x + endRadius * cos(angle + PI.toFloat())
        val endY = center.y + endRadius * sin(angle + PI.toFloat())

        drawLine(
            color = Color(0xFF6B7280),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 2.dp.toPx()
        )
    }
}