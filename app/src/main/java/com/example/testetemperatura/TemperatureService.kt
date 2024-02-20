package com.example.testetemperatura

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.IBinder
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class TemperatureService : Service() {

    private val TAG = "TemperatureService"
    private lateinit var batteryReceiver: BroadcastReceiver
    private val handler = android.os.Handler()
    private var estaRodando: Boolean = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    //private fun updateTemperature(temperature: Int) {
        //val intent = Intent("com.example.testetemperatura.UPDATE_TEMPERATURE")
        //intent.putExtra("temperature", temperature)
        //sendBroadcast(intent)
        //Log.d(TAG, "Temperatura: $temperature")
    //}

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Medidor de temperatura criado")
        estaRodando = true

        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val temperature =
                    intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
                Log.d(TAG, "Temperatura: $temperature")
            }
        }



        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        //handler.postDelayed(updateTemperatureRunnable, 5000)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Registra o receiver quando o serviço está em execução
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        // Inicia a atualização da temperatura
        handler.postDelayed(updateTemperatureRunnable, 5000)

        return START_STICKY
    }



    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "TemperatureService destroyed")

        // Remove o callback para interromper a atualização da temperatura
        handler.removeCallbacks(updateTemperatureRunnable)

        try {
            // Certifique-se de que o receiver está registrado antes de tentar desregistrá-lo
            if (estaRodando) {
                unregisterReceiver(batteryReceiver)
            }
        } catch (e: IllegalArgumentException) {
            // Ignora a exceção se o receiver não estiver registrado
            Log.e(TAG, "Receiver not registered.", e)
        }

    }

    private val updateTemperatureRunnable = object : Runnable {
        override fun run() {
            //handler.postDelayed(this, 600000)

            handler.postDelayed(this, 60000) // ---> o log se cria de 1 em 1 minuto, tomar cuidado com isso pra não ficar arquivos demais (cada arquivo de log fica com 55b),
            // implementar futura atualização para apagar logs antigos

            // Executar lógica para criar log no serviço
            criarLog()
        }
    }


    private fun criarLog() {
        try {
            Log.d(TAG, "Tentativa de criar log no serviço.")

            val dateFormat = SimpleDateFormat("dd-MM-yyyy_HH:mm:ss") // Define o formato da data e hora pro arquivo de log
            val dataHoraAtual = Date() // Obtém a data e hora atual

            // Lógica para criar o log no serviço
            if (estaRodando) {
                Log.d(TAG, "EstaRodando é verdadeiro.")
                val arquivo = File("${this.filesDir}/logTemperatura${dateFormat.format(dataHoraAtual)}.txt")
                //val arquivo = File("/storage/emulated/0/logTemperatura${dateFormat.format(dataHoraAtual)}.txt")
                arquivo.writeText("A temperatura no horário $dataHoraAtual foi de: ${MainActivity.VariaveisGlobais.temperatureTextView.text}")

                Log.d(TAG, "Log criado com sucesso. Caminho do arquivo: ${arquivo.absolutePath}")
            } else {
                Log.e(TAG, "EstaRodando é falso.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar log: ${e.message}")
        }
    }

}