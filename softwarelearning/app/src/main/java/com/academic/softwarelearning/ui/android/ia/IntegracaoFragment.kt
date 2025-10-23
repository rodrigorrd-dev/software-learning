package com.academic.softwarelearning.ui.ia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.academic.softwarelearning.R
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class IntegracaoFragment : Fragment() {

    private val client = OkHttpClient()
    private val apiUrl = ""

    private lateinit var inputPergunta: EditText
    private lateinit var btnEnviar: Button
    private lateinit var txtResposta: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.integracao_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inputPergunta = view.findViewById(R.id.editPergunta)
        btnEnviar = view.findViewById(R.id.btnEnviar)
        txtResposta = view.findViewById(R.id.txtResposta)

        btnEnviar.setOnClickListener {
            val pergunta = inputPergunta.text.toString()
            enviarParaOllama(pergunta) { resposta ->
                activity?.runOnUiThread {
                    txtResposta.text = resposta
                }
            }
        }
    }

    private fun enviarParaOllama(texto: String, callback: (String) -> Unit) {
        val json = JSONObject().apply {
            put("model", "mistral")
            put("prompt", "Responda em português, qual a resposta: $texto")
            put("stream", true)
        }

        val mediaType = "com/academic/softwarelearning/application/json; charset=utf-8".toMediaType()
        val requestBody = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(apiUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    txtResposta.text = "Erro: ${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body ?: return
                val inputStream = body.byteStream()
                val reader = inputStream.bufferedReader()

                val builder = StringBuilder()

                try {
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        try {
                            val jsonLine = JSONObject(line!!)
                            val piece = jsonLine.optString("response", "")
                            builder.append(piece)

                            activity?.runOnUiThread {
                                txtResposta.text = builder.toString()
                            }

                        } catch (e: Exception) {
                            // linha malformada — ignora
                        }
                    }
                } catch (e: IOException) {
                    activity?.runOnUiThread {
                        txtResposta.text = "Erro durante leitura: ${e.message}"
                    }
                } finally {
                    reader.close()
                }
            }
        })
    }
}
