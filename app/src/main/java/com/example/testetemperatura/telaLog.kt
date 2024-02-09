package com.example.testetemperatura

import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.testetemperatura.MainActivity.VariaveisGlobais.data
import com.example.testetemperatura.MainActivity.VariaveisGlobais.row1
import com.example.testetemperatura.MainActivity.VariaveisGlobais.row2
//import com.example.testetemperatura.MainActivity.VariaveisGlobais.temperature
//import com.example.testetemperatura.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import com.example.testetemperatura.databinding.ActivityMainBinding
import com.example.testetemperatura.databinding.TelaLogBinding

class telaLog : AppCompatActivity() {

    private lateinit var temperatureTextView: TextView
    //private lateinit var binding: ActivityMainBinding
    private val handler = Handler()

    //VARIÁVEL BOOLEANA PRA CHECAR QUANDO O APP TÁ RODANDO PRA PODER REGISTRAR O LOG
    private var estaRodando: Boolean = false

    private lateinit var binding: ActivityMainBinding

    object VariaveisGlobais {
        lateinit var temperatureTextView: TextView
        //lateinit var tabela_temperatura: TableLayout
        var row1: Array<SimpleDateFormat?> = arrayOfNulls(1)
        //var row2: Array<String?> = arrayOfNulls(1)
        var row2: Array<String?> = arrayOfNulls(1)      // ESSA VARIÁVEL PRECISOU SER DECLARADA COMO GLOBAL PARA PODER ASSUMIR O VALOR DA TEMPERATURA DE DENTRO DE OUTRA FUNÇÃO
        var data = SimpleDateFormat("dd-MM-yyyy - HH:mm:ss")

    }

    fun atualizarData(){

        data = SimpleDateFormat("dd-MM-yyyy - HH:mm:ss")

    }


    // FUNÇÃO PARA PASSAR PARA A NOVA TELA QUANDO CLICAR NO BOTÃO


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_log)

        val binding = TelaLogBinding.inflate(layoutInflater)
        //setContentView(binding.root)

        //VariaveisGlobais.temperatureTextView = findViewById(R.id.temperatureTextView)
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
        //registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

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
        row2[0] = formattedTemperature.toString()   // PASSANDO O VALOR DA TEMPERATURA PARA DENTRO DA VARIÁVEL

    }


    private val updateTemperatureRunnable = object : Runnable {
        override fun run() {
            registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

            //LIMPA A TABELA ANTES DE EXECUTAR O LOOP


            // limpa a tabela caso ela faça 5 registros

            val tabelaTemperatura = findViewById<TableLayout>(R.id.tabela_temperatura)
            if (tabelaTemperatura.childCount >= 5) {
                tabelaTemperatura.removeAllViews()
            }


            // atualiza a data para cada execução do código
            atualizarData()

            // cria uma nova linha
            val tableRow = TableRow(this@telaLog)

            // cria a célula da tabela com os valores da data
            val textViewData = TextView(this@telaLog)
            textViewData.text = data.format(Date())
            textViewData.gravity = Gravity.CENTER
            val paramsData = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            textViewData.layoutParams = paramsData
            textViewData.setBackgroundResource(R.color.colorWhite)  // Adiciona fundo branco
            textViewData.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)  // Define o tamanho do texto
            textViewData.setTextColor(Color.BLACK)  // Define a cor do texto
            tableRow.addView(textViewData)

            // cria a célula da tabela com os valores de temperatura
            val textViewTemperatura = TextView(this@telaLog)
            textViewTemperatura.text = row2[0].toString()
            textViewTemperatura.gravity = Gravity.CENTER
            val paramsTemp = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            textViewTemperatura.layoutParams = paramsTemp
            textViewTemperatura.setBackgroundResource(R.color.colorWhite)  // Adiciona fundo branco
            textViewTemperatura.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)  // Define o tamanho do texto
            textViewTemperatura.setTextColor(Color.BLACK)  // Define a cor do texto
            tableRow.addView(textViewTemperatura)

            // adiciona a linha à tabela
            findViewById<TableLayout>(R.id.tabela_temperatura).addView(tableRow)

            // agenda a próxima execução após o intervalo de tempo
            handler.postDelayed(this, 10000)

            enviarAtualizacaoLogBroadcast()
        }
    }

    private fun enviarAtualizacaoLogBroadcast() {
        val intent = Intent("com.example.testetemperatura.UPDATE_LOG")
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(batteryReceiver)
        } catch (e: IllegalArgumentException) {
            // Lidar com a exceção caso o receiver não esteja registrado
            Log.e(TAG, "Error unregistering receiver", e)
        }

        val serviceIntent = Intent(this, TemperatureService::class.java)
        stopService(serviceIntent)
        estaRodando = false
        super.onDestroy()
    }





    // Restante do seu código...
}