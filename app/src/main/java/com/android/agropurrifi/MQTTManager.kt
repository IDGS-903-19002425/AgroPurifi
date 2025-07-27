package com.android.agropurrifi

import android.content.ContentUris
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.text.Charsets.UTF_8

object MQTTManager {
    private val host = "29fd67fe7f304aff907332a3983b32f5.s1.eu.hivemq.cloud"
    private val username = "agroPurifi"
    private val password = "12345678Aa"
    private val topicTurbidez = "esp32/turbidez"
    private val topicPH = "esp32/PH"
    private var mqttClient : Mqtt5BlockingClient? = null

    // Callbacks para diferentes tipos de mensajes
    private var onTurbidezMessage: ((String) -> Unit)? = null
    private var onPHMessage: ((String) -> Unit)? = null
    private var onStatusChange: ((String) -> Unit)? = null

    fun conectarTurbidez(onMessage: (String) -> Unit, onStatus: (String) -> Unit){
        onTurbidezMessage = onMessage
        onStatusChange = onStatus
        conectar()
    }

    fun conectarPH(onMessage: (String) -> Unit, onStatus: (String) -> Unit){
        onPHMessage = onMessage
        onStatusChange = onStatus
        conectar()
    }

    private fun conectar(){
        CoroutineScope(Dispatchers.IO).launch {
            try{
                if (mqttClient?.state?.isConnected == true) {
                    withContext(Dispatchers.Main){
                        onStatusChange?.invoke("Conectado")
                    }
                    return@launch
                }

                mqttClient = MqttClient.builder()
                    .useMqttVersion5()
                    .serverHost(host)
                    .serverPort(8883)
                    .sslWithDefaultConfig()
                    .buildBlocking()

                mqttClient!!.connectWith()
                    .simpleAuth()
                    .username(username)
                    .password(UTF_8.encode(password))
                    .applySimpleAuth()
                    .send()

                // Suscribirse a ambos tópicos
                mqttClient!!.subscribeWith()
                    .topicFilter(topicTurbidez)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .send()

                mqttClient!!.subscribeWith()
                    .topicFilter(topicPH)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .send()

                withContext(Dispatchers.Main){
                    onStatusChange?.invoke("Conectado")
                }

                val publishes = mqttClient!!.publishes(com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL, true)
                while(true){
                    val message = publishes.receive()
                    val payload = String(message.payloadAsBytes, UTF_8)
                    val topic = message.topic.toString()

                    when(topic) {
                        topicTurbidez -> {
                            val turbidez = payload.toFloatOrNull()
                            if (turbidez != null){
                                withContext(Dispatchers.Main){
                                    onTurbidezMessage?.invoke(turbidez.toString())
                                }
                            }
                        }
                        topicPH -> {
                            val ph = payload.toFloatOrNull()
                            if (ph != null){
                                withContext(Dispatchers.Main){
                                    onPHMessage?.invoke(ph.toString())
                                }
                            }
                        }
                    }
                }

            }catch (e: Exception){
                withContext(Dispatchers.Main){
                    onStatusChange?.invoke("Error: ${e.message}")
                }
            }
        }
    }

    fun desconectar(){
        mqttClient?.disconnect()
        onTurbidezMessage = null
        onPHMessage = null
        onStatusChange = null
    }
}