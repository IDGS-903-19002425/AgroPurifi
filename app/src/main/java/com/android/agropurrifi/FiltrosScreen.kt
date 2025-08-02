package com.android.agropurrifi

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltrosScreen(
    onNavigateBack: () -> Unit = {}
) {
    var estatusConexion by remember { mutableStateOf("Desconectado") }
    var irrigationFilterOpen by remember { mutableStateOf(false) }
    var cleaningFilterOpen by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf("") }
    var actionMessage by remember { mutableStateOf("") }
    var lastCommand by remember { mutableStateOf("Ninguno") }

    LaunchedEffect(Unit) {
        MQTTManager.conectar()
    }

    fun executeFilterAction(action: String, message: String) {
        pendingAction = action
        actionMessage = message
        showConfirmationDialog = true
    }

    Scaffold(
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
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .systemBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 20.dp,
                bottom = 100.dp
            )
        ) {
            item {
                // Header Card con botón de regreso
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
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF059669).copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Control de Filtros",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                            Text(
                                text = "Sistema de Purificación",
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }

            item {
                // Estado actual del sistema
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
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Estado Actual del Sistema",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F2937)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            SystemStatusIndicator(
                                label = "Filtro Riego",
                                isActive = irrigationFilterOpen,
                                icon = Icons.Default.Build,
                                activeColor = Color(0xFF10B981)
                            )
                            SystemStatusIndicator(
                                label = "Filtro Limpieza",
                                isActive = cleaningFilterOpen,
                                icon = Icons.Default.Notifications,
                                activeColor = Color(0xFF06B6D4)
                            )
                        }
                    }
                }
            }

            item {
                // Filtro de Riego
                FilterControlCard(
                    title = "Filtro de Riego",
                    description = "Control del sistema de filtrado para riego",
                    icon = Icons.Default.Check,
                    iconColor = Color(0xFF10B981),
                    isOpen = irrigationFilterOpen,
                    onOpenClick = {
                        executeFilterAction(
                            "open_irrigation",
                            "¿Confirmas abrir el filtro de riego?\n\nEsto permitirá el paso de agua filtrada hacia el sistema de riego."
                        )
                    },
                    onCloseClick = {
                        executeFilterAction(
                            "close_irrigation",
                            "¿Confirmas cerrar el filtro de riego?\n\nEsto detendrá el flujo de agua hacia el sistema de riego."
                        )
                    }
                )
            }

            item {
                // Filtro de Limpieza
                FilterControlCard(
                    title = "Filtro de Limpieza",
                    description = "Control del sistema de filtrado para limpieza",
                    icon = Icons.Default.Edit,
                    iconColor = Color(0xFF06B6D4),
                    isOpen = cleaningFilterOpen,
                    onOpenClick = {
                        executeFilterAction(
                            "open_cleaning",
                            "¿Confirmas abrir el filtro de limpieza?\n\nEsto iniciará el proceso de purificación del agua."
                        )
                    },
                    onCloseClick = {
                        executeFilterAction(
                            "close_cleaning",
                            "¿Confirmas cerrar el filtro de limpieza?\n\nEsto detendrá el proceso de purificación."
                        )
                    }
                )
            }

            item {
                // Estado MQTT (ya no dentro de emergencia)
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

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Estado MQTT",
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280)
                            )
                            Text(
                                text = estatusConexion,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = connectionColor
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Último Comando",
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                            Text(
                                text = lastCommand,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1F2937)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Diálogo de confirmación simplificado
        if (showConfirmationDialog) {
            AlertDialog(
                onDismissRequest = {
                    showConfirmationDialog = false
                    pendingAction = ""
                },
                title = {
                    Text(
                        text = "Confirmar Acción",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                },
                text = {
                    Text(
                        text = actionMessage,
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = {
                                showConfirmationDialog = false
                                pendingAction = ""
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF6B7280)
                            )
                        ) {
                            Text("Cancelar")
                        }
                        TextButton(
                            onClick = {
                                when (pendingAction) {
                                    "open_irrigation" -> {
                                        MQTTManager.abrirFiltroRiego()
                                        irrigationFilterOpen = true
                                        lastCommand = "Abrir Riego"
                                    }
                                    "close_irrigation" -> {
                                        MQTTManager.cerrarFiltroRiego()
                                        irrigationFilterOpen = false
                                        lastCommand = "Cerrar Riego"
                                    }
                                    "open_cleaning" -> {
                                        MQTTManager.abrirFiltroLimpieza()
                                        cleaningFilterOpen = true
                                        lastCommand = "Abrir Limpieza"
                                    }
                                    "close_cleaning" -> {
                                        MQTTManager.cerrarFiltroLimpieza()
                                        cleaningFilterOpen = false
                                        lastCommand = "Cerrar Limpieza"
                                    }
                                }
                                showConfirmationDialog = false
                                pendingAction = ""
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF3B82F6)
                            )
                        ) {
                            Text(
                                text = "Confirmar",
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                },
                dismissButton = null
            )
        }
    }
}

@Composable
fun SystemStatusIndicator(
    label: String,
    isActive: Boolean,
    icon: ImageVector,
    activeColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    if (isActive) activeColor else Color(0xFFEF4444),
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center
        )
        Text(
            text = if (isActive) "ABIERTO" else "CERRADO",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) activeColor else Color(0xFFDC2626),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FilterControlCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    isOpen: Boolean,
    onOpenClick: () -> Unit,
    onCloseClick: () -> Unit
) {
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
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isOpen) "Estado: ABIERTO" else "Estado: CERRADO",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isOpen) iconColor else Color(0xFFDC2626)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onOpenClick,
                    enabled = !isOpen,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = iconColor,
                        disabledContainerColor = Color(0xFFE5E7EB)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ABRIR",
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onCloseClick,
                    enabled = isOpen,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444),
                        disabledContainerColor = Color(0xFFE5E7EB)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "CERRAR",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}