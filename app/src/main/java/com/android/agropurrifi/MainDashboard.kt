    package com.android.agropurrifi

    import android.app.NotificationChannel
    import android.app.NotificationManager
    import android.content.Context
    import android.os.Build
    import androidx.compose.foundation.Canvas
    import androidx.compose.foundation.background
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.shape.CircleShape
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.*
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.runtime.saveable.rememberSaveable
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.geometry.Offset
    import androidx.compose.ui.graphics.Brush
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.graphics.drawscope.DrawScope
    import androidx.compose.ui.graphics.vector.ImageVector
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.style.TextAlign
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.core.app.NotificationCompat
    import androidx.core.app.NotificationManagerCompat
    import kotlin.math.cos
    import kotlin.math.sin
    import android.Manifest
    import android.content.pm.PackageManager
    import androidx.activity.compose.rememberLauncherForActivityResult
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.core.content.ContextCompat

    @Composable
    fun MainDashboard(
        onNavigateToFilters: () -> Unit = {} // Callback para navegación
    ) {
        val context = LocalContext.current
        var phValue by remember { mutableStateOf(7.0) }
        var turbidezValue by remember { mutableStateOf(5.0) }
        var phStatus by remember { mutableStateOf("Conectando...") }
        var turbidezStatus by remember { mutableStateOf("Conectando...") }
        var showWaterQualityAlert by remember { mutableStateOf(false) }
        var alertMessage by remember { mutableStateOf("") }
        var alertShownForCurrentState by remember { mutableStateOf(false) }
        var isAppInForeground by remember { mutableStateOf(true) }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (!isGranted) {
                    // Puedes mostrar un mensaje o alertar que no se podrán mostrar notificaciones
                }
            }
        )

        LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionCheck = ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        // Crear canal de notificaciones
        LaunchedEffect(Unit) {
            createNotificationChannel(context)
        }

        // Función para evaluar calidad del agua
        fun evaluateWaterQuality(ph: Double, turbidez: Double): Pair<Boolean, String> {
            val phGood = ph in 6.0..8.5
            val turbidezGood = turbidez <= 19.0

            return when {
                !phGood && !turbidezGood -> Pair(false, "pH y turbidez fuera de rango")
                !phGood -> Pair(false, "pH fuera del rango recomendado")
                !turbidezGood -> Pair(false, "Turbidez muy alta")
                else -> Pair(true, "Agua apta para riego")
            }
        }

        // Función para manejar alertas sin spam
        fun handleWaterQualityCheck(ph: Double, turbidez: Double) {
            val (isGood, message) = evaluateWaterQuality(ph, turbidez)

            if (!isGood) {
                // Solo mostrar alerta si no se ha mostrado para este estado
                if (!alertShownForCurrentState) {
                    alertMessage = message
                    showWaterQualityAlert = true
                    alertShownForCurrentState = true

                    // Solo enviar notificación si la app no está en primer plano
                    if (!isAppInForeground) {
                        sendNotification(context, "Calidad del agua", message)
                    }
                }
            } else {
                // Si el agua está buena, resetear el flag para futuras alertas
                alertShownForCurrentState = false
            }
        }

        // Detectar cuando la app está en primer plano
        DisposableEffect(Unit) {
            isAppInForeground = true
            onDispose {
                isAppInForeground = false
            }
        }

        // Conectar a MQTT para ambos sensores
        LaunchedEffect(Unit) {
            MQTTManager.conectarPH(
                onMessage = { valor ->
                    phValue = valor.toDoubleOrNull() ?: 7.0
                    handleWaterQualityCheck(phValue, turbidezValue)
                },
                onStatus = { status -> phStatus = status }
            )

            MQTTManager.conectarTurbidez(
                onMessage = { valor ->
                    turbidezValue = valor.toDoubleOrNull() ?: 0.0
                    handleWaterQualityCheck(phValue, turbidezValue)
                },
                onStatus = { status -> turbidezStatus = status }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E3A8A), // Azul oscuro
                            Color(0xFF3B82F6), // Azul medio
                            Color(0xFF60A5FA)  // Azul claro
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                HeaderCard()

                // Status Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatusCard(
                        title = "pH del Agua",
                        value = String.format("%.2f", phValue),
                        status = getPhStatus(phValue),
                        icon = Icons.Default.Build,
                        color = getPhColor(phValue),
                        modifier = Modifier.weight(1f)
                    )

                    StatusCard(
                        title = "Turbidez",
                        value = "${String.format("%.1f", turbidezValue)} NTU",
                        status = getTurbidezStatus(turbidezValue),
                        icon = Icons.Default.Clear,
                        color = getTurbidezColor(turbidezValue),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Water Quality Indicator
                WaterQualityIndicator(phValue, turbidezValue)

                // Connection Status
                ConnectionStatusCard(phStatus, turbidezStatus)
            }
        }

        // Alert Dialog Modificado
        if (showWaterQualityAlert) {
            val safeNavigateToFilters by rememberUpdatedState(newValue = onNavigateToFilters)
            WaterQualityAlertDialog(
                message = alertMessage,
                onDismiss = {
                    showWaterQualityAlert = false
                },
                onNavigateToFilters = {
                    // debug: confirma que se presionó
                    android.util.Log.d("NAVEGACION", "Botón 'Ir a Pantalla de Filtros' presionado en diálogo")
                    showWaterQualityAlert = false
                    alertShownForCurrentState = false
                    safeNavigateToFilters()
                },
                onCancel = {
                    // Solo cerrar el diálogo sin enviar nada
                    showWaterQualityAlert = false
                }
            )
        }
    }

    @Composable
    fun HeaderCard() {
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
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "AgroPurifi",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = "Monitor de Calidad del Agua",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }
    }

    @Composable
    fun StatusCard(
        title: String,
        value: String,
        status: String,
        icon: ImageVector,
        color: Color,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = status,
                    fontSize = 10.sp,
                    color = color,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    @Composable
    fun WaterQualityIndicator(ph: Double, turbidez: Double) {
        val (isGood, message) = remember(ph, turbidez) {
            val phGood = ph in 6.0..8.5
            val turbidezGood = turbidez <= 19.0

            when {
                phGood && turbidezGood -> Pair(true, "Agua Apta para Riego")
                !phGood && !turbidezGood -> Pair(false, "Agua No Recomendada")
                !phGood -> Pair(false, "pH Fuera de Rango")
                else -> Pair(false, "Turbidez Elevada")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isGood) Color(0xFF10B981).copy(alpha = 0.1f)
                else Color(0xFFEF4444).copy(alpha = 0.1f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            if (isGood) Color(0xFF10B981) else Color(0xFFEF4444),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isGood) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Estado del Agua",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = message,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGood) Color(0xFF059669) else Color(0xFFDC2626)
                    )
                }
            }
        }
    }

    @Composable
    fun ConnectionStatusCard(phStatus: String, turbidezStatus: String) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Estado de Conexión",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ConnectionStatus("Sensor pH", phStatus)
                    ConnectionStatus("Sensor Turbidez", turbidezStatus)
                }
            }
        }
    }

    @Composable
    fun ConnectionStatus(sensor: String, status: String) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val color = when {
                status.contains("Conectado") -> Color(0xFF10B981)
                status.contains("Error") -> Color(0xFFEF4444)
                else -> Color(0xFFF59E0B)
            }

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = sensor,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
                Text(
                    text = status,
                    fontSize = 10.sp,
                    color = color
                )
            }
        }
    }

    // Diálogo de Alerta Modificado
    @Composable
    fun WaterQualityAlertDialog(
        message: String,
        onDismiss: () -> Unit,
        onNavigateToFilters: () -> Unit,
        onCancel: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Alerta de Calidad del Agua",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "$message\n\nAccede a la pantalla de filtros para gestionar el agua almacenada.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF6B7280)
                        )
                    ) {
                        Text("Cancelar")
                    }
                    TextButton(
                        onClick = onNavigateToFilters,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF3B82F6)
                        )
                    ) {
                        Text("Ir a Pantalla de Filtros")
                    }
                }
            },
            dismissButton = null
        )
    }

    // Funciones auxiliares
    fun getPhStatus(ph: Double): String = when {
        ph < 6.0 -> "Muy Ácido"
        ph in 6.0..6.5 -> "Ligeramente Ácido"
        ph in 6.5..7.5 -> "Óptimo"
        ph in 7.5..8.5 -> "Ligeramente Alcalino"
        else -> "Muy Alcalino"
    }

    fun getPhColor(ph: Double): Color = when {
        ph < 6.0 || ph > 8.5 -> Color(0xFFEF4444)
        ph in 6.0..6.5 || ph in 7.5..8.5 -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    fun getTurbidezStatus(turbidez: Double): String = when {
        turbidez <= 5 -> "Excelente"
        turbidez <= 19 -> "Buena"
        else -> "No Recomendada"
    }

    fun getTurbidezColor(turbidez: Double): Color = when {
        turbidez <= 5 -> Color(0xFF10B981)
        turbidez <= 19 -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "water_quality_alerts",
                "Alertas de Calidad del Agua",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones sobre la calidad del agua para riego"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendNotification(context: Context, title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val notification = NotificationCompat.Builder(context, "water_quality_alerts")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(1, notification)
        }
    }