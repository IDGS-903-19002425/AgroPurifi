package com.android.agropurrifi

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        Text("Estado de la conexión: $estatusConexion", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.padding(16.dp))

        val phDouble = ph.toDoubleOrNull() ?: 7.0
        Text("pH actual del agua: ${String.format("%.2f", phDouble)}", fontSize = 24.sp)

        Text(estadoAgua, fontSize = 18.sp, color = when{
            phDouble < 6.0 || phDouble > 8.5 -> androidx.compose.ui.graphics.Color.Red
            phDouble in 6.0..6.5 || phDouble in 7.5..8.5 -> androidx.compose.ui.graphics.Color(0xFFFFA500) // Orange
            phDouble in 6.5..7.5 -> androidx.compose.ui.graphics.Color.Green
            else -> androidx.compose.ui.graphics.Color.Gray
        })
    }
}