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
    private val topic = "esp32/turbidez"
    private var mqttClient : Mqtt5BlockingClient? = null


fun conectar(onMesagge: (String) -> Unit, onStatus: (String) -> Unit){
    CoroutineScope(Dispatchers.IO).launch {
        try{
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
            mqttClient!!.subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .send()
            withContext(Dispatchers.Main){
                onStatus("Conectado")
            }
            val publishes = mqttClient!!.publishes(com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL, true)
            while(true){
                val message = publishes.receive()
                val payload = String(message.payloadAsBytes, UTF_8)
                val turbidez = payload.toFloatOrNull()
                if (turbidez != null){
                    withContext(Dispatchers.Main){
                        onMesagge(turbidez.toString())
                    }
                }
            }

        }catch (e: Exception){
            withContext(Dispatchers.Main){
                onStatus("Error: ${e.message}")
            }
        }

    }

}
fun desconectar(){
    mqttClient?.disconnect()
}
}
