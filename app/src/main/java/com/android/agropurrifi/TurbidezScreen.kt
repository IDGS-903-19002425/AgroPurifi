package com.android.agropurrifi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun TurbidezScreen() {
    var estatusConexion by remember { mutableStateOf("Desconectado") }
    var turbidez by remember { mutableStateOf("0.0") }
    var estadoAgua by remember { mutableStateOf("Desconocido") }

    LaunchedEffect(Unit){
        MQTTManager.conectar(
            onMesagge = { nuevoValor ->
                turbidez = nuevoValor
                val turbidezDouble = turbidez.toDoubleOrNull() ?: 0.0
                estadoAgua = when{
                    turbidezDouble <= 5 -> "Agua excelente para riego"
                    turbidezDouble in 5.01..19.0 -> "Agua buena para riego"
                    else -> "Agua no recomendada para riego"
                }
            },
            onStatus = { status -> estatusConexion = status}
    )
}
Column(
    modifier = Modifier
    .fillMaxSize()
    .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
){
    Text("Estado de la conexion: $estatusConexion", style = MaterialTheme.typography.bodyMedium)

    Spacer(modifier = Modifier.padding(16.dp))
    val turbidezDouble = turbidez.toDoubleOrNull() ?: 0.0
    Text("Turbidez actual del agua: ${String.format("%.2f", turbidezDouble)} NTU", fontSize = 24.sp)
    Text(estadoAgua, fontSize = 18.sp,color = when{
        turbidezDouble <= 5 -> androidx.compose.ui.graphics.Color.Green
        turbidezDouble in 5.01..19.0 -> androidx.compose.ui.graphics.Color.Yellow
        else -> androidx.compose.ui.graphics.Color.Red
    })
}
}




