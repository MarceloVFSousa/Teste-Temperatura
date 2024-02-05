package com.example.testetemperatura

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.widget.TableLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.testetemperatura.MainActivity.VariaveisGlobais.data
import com.example.testetemperatura.MainActivity.VariaveisGlobais.row1
import com.example.testetemperatura.MainActivity.VariaveisGlobais.row2
import com.example.testetemperatura.MainActivity.VariaveisGlobais.tabela_temperatura
//import com.example.testetemperatura.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var temperatureTextView: TextView
    //private lateinit var binding: ActivityMainBinding
    private val handler = Handler()

    //VARIÁVEL BOOLEANA PRA CHECAR QUANDO O APP TÁ RODANDO PRA PODER REGISTRAR O LOG
    private var estaRodando: Boolean = false



    object VariaveisGlobais {
        lateinit var temperatureTextView: TextView
        lateinit var tabela_temperatura: TableLayout
        var row1: Array<SimpleDateFormat?> = arrayOfNulls(1)
        var row2: Array<String?> = arrayOfNulls(1)
        var data = SimpleDateFormat("dd-MM-yyyy - HH:mm:ss")


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        VariaveisGlobais.temperatureTextView = findViewById(R.id.temperatureTextView)
        estaRodando = true

        ////////////////////////////////
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val temperature = intent?.getIntExtra("temperature", 0) ?: 0
                updateTemperature(temperature)
            }
        }

        registerReceiver(receiver, IntentFilter("com.example.testetemperatura.UPDATE_TEMPERATURE"))

        ///////////////////////////////

        val serviceIntent = Intent(this, TemperatureService::class.java)
        startService(serviceIntent)

        //registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        handler.postDelayed(updateTemperatureRunnable, 5000)
    }

    override fun onResume() {
        super.onResume()

        // Registra o receiver quando a atividade está visível
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        // Inicia a atualização da temperatura
        handler.postDelayed(updateTemperatureRunnable, 10000)
    }

    /////////// ALTERAR ISSO AQUI PARA EXECUÇÃO EM SEGUNDO PLANO

    override fun onPause() {
        super.onPause()

        // Desregistra o receiver quando a atividade não está visível
        unregisterReceiver(batteryReceiver)

        // Remove o callback para interromper a atualização da temperatura
        handler.removeCallbacks(updateTemperatureRunnable)
    }
    ////////////////////////////////////////////////////////////

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val temperature = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            updateTemperature(temperature)
        }
    }

    private fun updateTemperature(temperature: Int) {
        val formattedTemperature = temperature / 10.0
        VariaveisGlobais.temperatureTextView.text = "Temperatura: $formattedTemperature °C"
    }

    private val updateTemperatureRunnable = object : Runnable {
        override fun run() {
            registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            handler.postDelayed(this, 10000)

            //LINHA DATA
            row1[0] = data
            var linhaHorario = findViewById<TextView>(R.id.linha1)
            val dataFormatadaLinha = row1[0]?.format(Date())
            linhaHorario.text = dataFormatadaLinha

            //LINHA TEMPERATURA

            row2[0] = VariaveisGlobais.temperatureTextView.toString()
            var linhaTemperatura = findViewById<TextView>(R.id.linha2)
            val linhaTemperaturaFormatada = row2[0].toString()
            linhaTemperatura.setText(linhaTemperaturaFormatada)

            //row1[1] = "Temperatura: "


            enviarAtualizacaoLogBroadcast()
        }
    }

    private fun enviarAtualizacaoLogBroadcast() {
        val intent = Intent("com.example.testetemperatura.UPDATE_LOG")
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        val serviceIntent = Intent(this, TemperatureService::class.java)
        stopService(serviceIntent)
        estaRodando = false
        super.onDestroy()
    }



    // Restante do seu código...
}