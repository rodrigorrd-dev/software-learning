package com.academic.softwarelearning.ui.home

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.academic.softwarelearning.R
import com.academic.softwarelearning.ui.android.adapter.ArquivoAdapter
import com.academic.softwarelearning.ui.android.adapter.AtividadeAlunoAdapter
import com.academic.softwarelearning.domain.model.AtividadeAluno
import com.academic.softwarelearning.infrastructure.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.academic.softwarelearning.domain.service.AuthSessionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.LinearLayout
import com.academic.softwarelearning.domain.model.FeedbackParsed
import com.academic.softwarelearning.domain.model.FileManager
import com.academic.softwarelearning.domain.repository.ArquivoRepository
import com.academic.softwarelearning.domain.service.ArquivoStorageManager
import com.academic.softwarelearning.domain.service.VisualizadorArquivo
import com.academic.softwarelearning.infrastructure.notification.NotificacaoManager
import com.academic.softwarelearning.infrastructure.repository.ArquivoRepositoryImpl
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.button.MaterialButton
import org.json.JSONArray

@Suppress("DEPRECATION")
class HomeFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var recyclerAtividades: RecyclerView
    private lateinit var recyclerArquivos: RecyclerView
    private lateinit var atividadeAdapter: AtividadeAlunoAdapter
    private lateinit var arquivoAdapter: ArquivoAdapter
    private lateinit var edtRespostaTexto: EditText
    private lateinit var btnAdicionarArquivo: Button
    private lateinit var btnEnviarResposta: Button
    private lateinit var txtIaResponse: TextView
    private lateinit var atividadeSelecionada: AtividadeAluno
    private lateinit var progressAguarde: ProgressBar
    private lateinit var txtAguarde: TextView
    private var isLoading = false
    private val usuarioId: String = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    private var lastPhotoUri: Uri? = null
    private var photoCount = 1
    private lateinit var btnFechar: MaterialButton
    private val fileManager = FileManager()

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                lastPhotoUri?.let { uri ->
                    // adiciona no FileManager
                    fileManager.addArquivo(uri)

                    // notifica UI via listener
                    arquivoAdapter.notifyItemInserted(fileManager.getArquivos().size - 1)
                    Log.d("HomeFragment", "Foto tirada: ${uri.lastPathSegment}")
                }
            } else {
                Log.d("HomeFragment", "Falha ao tirar a foto")
            }
        }

    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                fileManager.addArquivo(it)
                arquivoAdapter.notifyItemInserted(fileManager.getArquivos().size - 1)
                Log.d("HomeFragment", "Arquivo selecionado: ${getFileNameFromUri(it)}")
            }
        }

    // dentro de HomeFragment.kt
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerAtividades = view.findViewById(R.id.recyclerAtividades)
        recyclerArquivos = view.findViewById(R.id.recyclerArquivos)
        edtRespostaTexto = view.findViewById(R.id.edtRespostaTexto)
        btnAdicionarArquivo = view.findViewById(R.id.btnAdicionarArquivo)
        btnEnviarResposta = view.findViewById(R.id.btnEnviarResposta)
        txtIaResponse = view.findViewById(R.id.txtIaResponse)
        progressAguarde = view.findViewById(R.id.progressAguarde)
        txtAguarde = view.findViewById(R.id.txtAguarde)
        btnFechar = view.findViewById(R.id.btnFechar)
        btnFechar.visibility = View.GONE
        // setup recyclers
        recyclerAtividades.layoutManager = LinearLayoutManager(context)
        recyclerArquivos.layoutManager = LinearLayoutManager(context)
        arquivoAdapter = ArquivoAdapter(fileManager.getArquivos())
        recyclerArquivos.adapter = arquivoAdapter
        // firebase listener
        database = FirebaseDatabase.getInstance().reference
        setupAtividadesListener()

        // restore loading state
        if (isLoading) {
            showAguarde()
            showAlunoInputs(false)
        } else {
            hideAguarde()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!isLoading) {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            }
        )

        btnAdicionarArquivo.setOnClickListener {
            checkAlunoOrWarn(
                isAluno(),
                "Apenas ALUNO pode anexar arquivos."
            ) { mostrarOpcoesDeArquivo() }
        }

        btnEnviarResposta.setOnClickListener {
            checkAlunoOrWarn(isAluno(), "Apenas ALUNO pode enviar resposta.") {
                val texto = edtRespostaTexto.text.toString().trim()
                if (texto.isNotEmpty() && ::atividadeSelecionada.isInitialized) {
                    // start loading
                    isLoading = true
                    showAguarde()
                    hideButonsRespostaAtividade()
                    enviarParaApiEGravar(texto)
                } else {
                    Toast.makeText(
                        context,
                        "Digite uma resposta ou selecione uma atividade.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        btnFechar.setOnClickListener {
            hideButonsRespostaAtividade()
            hideButtonClose()
            atividadeSelecionada = AtividadeAluno()
            atividadeAdapter.clearSelection()
            atividadeAdapter.notifyDataSetChanged()
        }

        return view
    }

    private suspend fun getArquivosAtividadeUris(): List<Uri> = withContext(Dispatchers.IO) {
        val uris = mutableListOf<Uri>()
        try {
            val arquivosAtividade = RetrofitClient.apiService
                .buscarArquivos(
                    atividadeSelecionada.responsavel.toString(),
                    atividadeSelecionada.id ?: ""
                )

            for (arquivo in arquivosAtividade) {
                val decodedBytes = Base64.decode(arquivo.arquivo, Base64.DEFAULT)
                val fileName = arquivo.nome

                // Cria arquivo temporário
                val tempFile =
                    File.createTempFile("atividade_", "_${fileName}", requireContext().cacheDir)
                tempFile.writeBytes(decodedBytes)

                // Cria URI
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    tempFile
                )
                uris.add(uri)
            }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Erro ao obter arquivos da atividade: ${e.message}", e)
        }
        return@withContext uris
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun downloadArquivos(responsavel: String, codigoAtividade: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1. Buscar arquivos da API (Repository)
                val arquivos = arquivoRepository.buscarArquivos(responsavel, codigoAtividade)

                arquivos.forEach { arquivo ->
                    val bytes = Base64.decode(arquivo.arquivo, Base64.DEFAULT)

                    // Usa o MIME type real, ou tenta deduzir da extensão
                    val mimeType = arquivo.tipoarquivo ?: getMimeTypeFromExtension(arquivo.nome)

                    // 2. Salvar arquivo no dispositivo (Storage)
                    val uri = arquivoStorage.salvarArquivo(bytes, arquivo.nome, mimeType)

                    // 3. Atualizar UI (Main Thread)
                    withContext(Dispatchers.Main) {
                        uri?.let {
                            // Mostrar notificação
                            notificacaoManager.mostrar(arquivo.nome)

                            // Abrir no visualizador usando o MIME type correto
                            visualizadorArquivo.abrir(it, mimeType)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Download", "Erro ao baixar arquivos", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Erro ao baixar arquivos: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // Função utilitária para deduzir MIME type da extensão
    private fun getMimeTypeFromExtension(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return android.webkit.MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(extension)
            ?: "application/octet-stream" // fallback seguro
    }

    private fun criarCanalDeNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                "canal_download",
                "Downloads de Arquivos",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações de download de arquivos"
            }

            val notificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(canal)
        }
    }

    @SuppressLint("MissingPermission")
    private fun mostrarNotificacao(nomeArquivo: String) {
        val builder = NotificationCompat.Builder(requireContext(), "canal_download")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Download concluído")
            .setContentText("Arquivo \"$nomeArquivo\" foi baixado com sucesso.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = NotificationManagerCompat.from(requireContext())
        notificationManager.notify(nomeArquivo.hashCode(), builder.build())
    }

    private fun setupAtividadesListener() {
        database.child("atividades")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.Q)
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val lista = snapshot.children.mapNotNull {
                        it.getValue(AtividadeAluno::class.java)
                    }
                    atividadeAdapter = AtividadeAlunoAdapter(
                        lista,
                        onAtividadeClick = { sel ->
                            checkAlunoOrWarn(isAluno(), "Apenas ALUNO pode enviar resposta.") {
                                atividadeSelecionada = sel
                                showAlunoInputs(true)
                            }
                        },
                        onDownloadClick = { responsavel, codigo ->
                            downloadArquivos(responsavel, codigo)
                        }
                    )
                    recyclerAtividades.adapter = atividadeAdapter
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    Log.e("HomeFragment", "Erro ao recuperar atividades", error.toException())
                }
            })
    }

    private fun enviarParaApiEGravar(respostaTexto: String) {
        lifecycleScope.launch {
            try {
                hideButtonClose()
                // chama a API OCR/IA
                val arquivosAtividade = getArquivosAtividadeUris()
                val todosArquivos = fileManager.getArquivos() + arquivosAtividade
                val iaJson = callPythonOcrApi(todosArquivos)

                val deepseekPretty = extractDeepseekPretty(iaJson)
                val deepseekRaw = JSONObject(iaJson).optString("deepseek_response", "")
                withContext(Dispatchers.Main) {
                    // abre o bottom sheet bonito com botão de fechar:
                    showAiFeedbackBottomSheet(deepseekRaw)
                }

                // depois segue com upload e gravação
                val fileUrls = uploadFilesToApi()
                saveRespostaFirebase(respostaTexto, iaJson, fileUrls)

            } catch (e: Exception) {
                hideLoadingUI()
                Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("HomeFragment", "Erro integração IA", e)
            }
        }
    }

    private fun extractDeepseekPretty(fullJson: String): String {
        return try {
            val root = JSONObject(fullJson)
            val raw = root.optString("deepseek_response", "")
            if (raw.isBlank()) return "Sem resposta da IA"

            // remove cercas de código ```json ... ```
            val cleaned = stripCodeFence(raw)

            // tenta parsear o JSON interno para identar
            try {
                val inner = JSONObject(cleaned)
                inner.toString(2)
            } catch (_: Exception) {
                // pode ter vindo como array ou texto simples
                try {
                    val arr = org.json.JSONArray(cleaned)
                    arr.toString(2)
                } catch (_: Exception) {
                    cleaned // mostra como veio
                }
            }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Falha ao ler deepseek_response: ${e.message}", e)
            "Sem resposta da IA"
        }
    }

    private suspend fun callPythonOcrApi(uris: List<Uri>): String = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .callTimeout(5, TimeUnit.MINUTES)
            .retryOnConnectionFailure(true)
            .build()

        val multipart = MultipartBody.Builder().setType(MultipartBody.FORM)

        // 🔹 PERGUNTA e RESPOSTA
        val pergunta = atividadeSelecionada.titulo ?: ""   // ou descricao / enunciado
        val respostaDigitada = edtRespostaTexto.text.toString()

        multipart.addFormDataPart("question", pergunta)
        multipart.addFormDataPart("student_text", respostaDigitada)

        // 🔹 (Opcional) gabarito e linguagem
        // multipart.addFormDataPart("answer_key", """{"Q1":"A","Q2":"B"}""")
        multipart.addFormDataPart("language", "por")

        // 🔹 arquivos: escolha 1 (file) OU vários (files)
        // A) apenas 1 arquivo (compatível com `file` no servidor):
        /*
        uris.firstOrNull()?.let { uri ->
            val name = getFileNameFromUri(uri)
            val mime = requireContext().contentResolver.getType(uri) ?: "application/octet-stream"
            val bytes = requireContext().contentResolver.openInputStream(uri)!!.readBytes()
            multipart.addFormDataPart("file", name, bytes.toRequestBody(mime.toMediaType()))
        }
        */

        // B) vários arquivos (compatível com `files: List<UploadFile>` no servidor):
        uris.forEach { uri ->
            val name = getFileNameFromUri(uri)
            val mime = requireContext().contentResolver.getType(uri) ?: "com/academic/softwarelearning/application/octet-stream"
            val bytes = requireContext().contentResolver.openInputStream(uri)!!.readBytes()
            multipart.addFormDataPart("files", name, bytes.toRequestBody(mime.toMediaType()))
        }

        val request = Request.Builder()
            .url("")
            .post(multipart.build())
            .build()

        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) throw IOException("Código ${resp.code}")
            resp.body!!.string()
        }
    }

    private suspend fun uploadFilesToApi(): List<String> = withContext(Dispatchers.IO) {
        val urls = mutableListOf<String>()
        val mediaTypeText = "text/plain".toMediaType()

        for (uri in fileManager.getArquivos()) {
            try {
                val fileName = getFileNameFromUri(uri)
                val contentResolver = requireContext().contentResolver

                // Detecta o tipo MIME do arquivo
                val mimeType = contentResolver.getType(uri)
                    ?: getMimeTypeFromExtension(fileName)

                val mediaTypeStream = mimeType.toMediaType()

                val fileBytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IOException("Falha ao ler o arquivo $fileName")

                val codigoBody = atividadeSelecionada.id?.toRequestBody(mediaTypeText) ?: continue
                val nomeBody = fileName.toRequestBody(mediaTypeText)
                val responsavelBody = usuarioId.toRequestBody(mediaTypeText)
                val tipoArquivoBody = mimeType.toRequestBody(mediaTypeText)

                val fileRequestBody = fileBytes.toRequestBody(mediaTypeStream)
                val partFile = MultipartBody.Part.createFormData(
                    name = "arquivo",
                    filename = fileName,
                    body = fileRequestBody
                )

                val response = RetrofitClient.apiService.adicionarArquivo(
                    codigo = codigoBody,
                    arquivo = partFile,
                    responsavel = responsavelBody,
                    tipoarquivo = tipoArquivoBody,
                    nome = nomeBody
                )

                urls += response.arquivo

            } catch (e: Exception) {
                Log.e("uploadFilesToApi", "Erro ao enviar arquivo: ${e.message}", e)
            }
        }

        urls
    }

    private fun saveRespostaFirebase(
        respostaTexto: String,
        iaJson: String,
        fileUrls: List<String>
    ) {
        checkAlunoOrWarn(isAluno(), "Apenas ALUNO pode enviar resposta.") {
            val respostasRef = database.child("respostas")

            val respostaId = respostasRef.push().key ?: run {
                hideLoadingUI()
                Toast.makeText(
                    requireContext(),
                    "Falha ao gerar ID da resposta.",
                    Toast.LENGTH_SHORT
                ).show()
                return@checkAlunoOrWarn
            }

            val respostaMap = mapOf(
                "texto" to respostaTexto,
                "atividadeId" to (atividadeSelecionada.id ?: "").toString(),
                "integracaoIA" to JSONObject(iaJson).toString(2),
                "fileUrls" to fileUrls,
                "usuarioId" to usuarioId,
                "timestamp" to System.currentTimeMillis()
            )

            respostasRef.child(respostaId).setValue(respostaMap)
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Resposta salva com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()
                    clearUiAfterSubmit()
                }
                .addOnFailureListener {
                    hideLoadingUI()
                    Toast.makeText(
                        requireContext(),
                        "Falha ao salvar no Firebase",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun stripCodeFence(s: String): String {
        var t = s.trim()
        if (t.startsWith("```")) {
            // remove o prefixo ``` ou ```json
            t = t.substringAfter("```").trimStart()
            if (t.startsWith("json", ignoreCase = true)) {
                t = t.removePrefix("json").trimStart()
            }
            // remove o sufixo ```
            t = t.substringBeforeLast("```").trim()
        }
        return t
    }

    private fun clearUiAfterSubmit() {
        isLoading = false
        hideLoadingUI()

        // Limpar campos
        edtRespostaTexto.text.clear()
        fileManager.clearArquivos()
        arquivoAdapter.notifyDataSetChanged()

        // 🔹 Limpar seleção do adapter
        if (::atividadeAdapter.isInitialized) {
            atividadeAdapter.clearSelection()
        }

        // 🔹 Ocultar botão fechar
        hideButtonClose()

        // 🔹 Limpar atividade selecionada
        atividadeSelecionada = AtividadeAluno()

        // 🔹 Decide se mostra inputs ou não
        if (isAluno()) showAlunoInputs(true) else hideButonsRespostaAtividade()
    }

    private fun hideLoadingUI() {
        isLoading = false
        progressAguarde.visibility = View.GONE
        txtAguarde.visibility = View.GONE
        showAlunoInputs(isAluno())
    }

    @SuppressLint("Range")
    private fun getFileNameFromUri(uri: Uri): String {
        var name = uri.lastPathSegment.orEmpty()
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) {
                    name = it.getString(idx)
                }
            }
        }
        return name
    }

    private fun mostrarOpcoesDeArquivo() {
        val options = arrayOf("Tirar Foto", "Selecionar Arquivo")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Escolher opção")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val photoFile = File(requireContext().cacheDir, "photo_${photoCount}.jpg")
                        lastPhotoUri = FileProvider.getUriForFile(
                            requireContext(),
                            "${requireContext().packageName}.fileprovider",
                            photoFile
                        )
                        photoCount++
                        takePhotoLauncher.launch(lastPhotoUri)
                    }

                    1 -> pickFileLauncher.launch("*/*")
                }
            }
            .show()
    }

    //=====================DIALOGO IA
    private fun showAiFeedbackBottomSheet(deepseekJsonBlock: String) {
        val ctx = requireContext()
        val dialog = BottomSheetDialog(ctx)
        val view = LayoutInflater.from(ctx).inflate(R.layout.dialog_ai_feedback, null)
        dialog.setContentView(view)

        val btnClose = view.findViewById<MaterialButton>(R.id.btnClose)
        val btnOk = view.findViewById<MaterialButton>(R.id.btnOk)
        val btnCopy = view.findViewById<MaterialButton>(R.id.btnCopy)
        val tvScore = view.findViewById<TextView>(R.id.tvScore)
        val cpiScore = view.findViewById<CircularProgressIndicator>(R.id.cpiScore)
        val containerStrengths = view.findViewById<LinearLayout>(R.id.containerStrengths)
        val containerImprovements = view.findViewById<LinearLayout>(R.id.containerImprovements)
        val tvObservations = view.findViewById<TextView>(R.id.tvObservations)

        // 1) Remover cercas ```json ... ```
        val cleaned = stripCodeFence(deepseekJsonBlock)

        // 2) Tentar parse
        val (percentual, strengths, improvements, observations) = parseDeepseek(cleaned)

        // 3) Preencher UI
        val score = percentual.coerceIn(0, 100)
        cpiScore.setProgress(score, true)
        tvScore.text = "${score}%"

        fun addBullet(container: LinearLayout, text: String) {
            val tv = TextView(
                ctx,
                null,
                0,
                com.google.android.material.R.style.TextAppearance_Material3_BodyMedium
            )
            tv.text = "• $text"
            tv.setPadding(6, 6, 6, 6)
            container.addView(tv)
        }

        containerStrengths.removeAllViews()
        strengths.forEach { addBullet(containerStrengths, it) }

        containerImprovements.removeAllViews()
        improvements.forEach { addBullet(containerImprovements, it) }

        tvObservations.text = observations

        // 4) Ações
        btnCopy.setOnClickListener {
            val obsOnly = extractObservacoesOnly("{\"deepseek_response\":\"$cleaned\"}")
            val clip = android.content.ClipData.newPlainText("Observações IA", obsOnly)
            val cm =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            cm.setPrimaryClip(clip)
            Toast.makeText(ctx, "Observações copiadas", Toast.LENGTH_SHORT).show()
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        btnOk.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun parseDeepseek(cleanedJson: String): FeedbackParsed {
        // Tenta JSON objeto direto
        return try {
            val obj = JSONObject(cleanedJson)
            FeedbackParsed(
                percentual = obj.optInt("percentual", 0),
                strengths = obj.optJSONArray("pontos_fortes")?.toListOfStrings().orEmpty(),
                improvements = obj.optJSONArray("pontos_de_melhoria")?.toListOfStrings().orEmpty(),
                observations = obj.optString("observacoes", "")
            )
        } catch (_: Exception) {
            // Falha no parse: retorna tudo no campo observações
            FeedbackParsed(0, emptyList(), emptyList(), cleanedJson)
        }
    }

    private fun JSONArray.toListOfStrings(): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until length()) list += optString(i)
        return list
    }

    private fun extractObservacoesOnly(fullJsonFromApi: String): String {
        return try {
            // o servidor te devolve um JSON com a chave deepseek_response (que às vezes vem cercado por ```json)
            val root = JSONObject(fullJsonFromApi)
            val raw = root.optString("deepseek_response", "")
            if (raw.isBlank()) return "—"

            val cleaned = stripCodeFence(raw)
            val inner = try {
                JSONObject(cleaned)
            } catch (_: Exception) {
                // pode vir array/string; se não for objeto com 'observacoes', retorna tudo
                return cleaned
            }
            inner.optString("observacoes", "—")
        } catch (_: Exception) {
            "—"
        }
    }

    private fun isAluno(): Boolean =
        AuthSessionService.hasRole(requireContext(), "ALUNO")

    private fun showAlunoInputs(show: Boolean) {
        val visible = if (show) View.VISIBLE else View.GONE
        edtRespostaTexto.visibility = visible
        btnAdicionarArquivo.visibility = visible
        btnEnviarResposta.visibility = visible
        recyclerArquivos.visibility = visible

        edtRespostaTexto.isEnabled = show
        btnAdicionarArquivo.isEnabled = show
        btnEnviarResposta.isEnabled = show

        // 🔹 mostrar botão fechar apenas se os inputs estiverem visíveis
        btnFechar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun aplicarVisaoAluno() = showAlunoInputs(isAluno())

    private fun checkAlunoOrWarn(condition: Boolean, message: String, onAllowed: () -> Unit) {
        if (!condition) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            return
        }
        onAllowed()
    }

    // show inputs
    private fun showButons() {
        edtRespostaTexto.visibility = View.VISIBLE
        btnAdicionarArquivo.visibility = View.VISIBLE
        btnEnviarResposta.visibility = View.VISIBLE
        recyclerArquivos.visibility = View.VISIBLE
    }

    // hide inputs
    private fun hideButonsRespostaAtividade() {
        edtRespostaTexto.visibility = View.GONE
        btnAdicionarArquivo.visibility = View.GONE
        btnEnviarResposta.visibility = View.GONE
        recyclerArquivos.visibility = View.GONE
    }

    private fun showAguarde() {
        progressAguarde.visibility = View.VISIBLE
        txtAguarde.visibility = View.VISIBLE
    }

    private fun hideAguarde() {
        progressAguarde.visibility = View.GONE
        txtAguarde.visibility = View.GONE
    }

    private fun showButtonClose() {
        btnFechar.visibility = View.VISIBLE
    }

    private fun hideButtonClose() {
        btnFechar.visibility = View.GONE
    }

    private val arquivoRepository: ArquivoRepository by lazy {
        ArquivoRepositoryImpl(RetrofitClient.apiService)
    }

    private val arquivoStorage: ArquivoStorageManager by lazy {
        ArquivoStorageManager(requireContext())
    }

    private val notificacaoManager: NotificacaoManager by lazy {
        NotificacaoManager(requireContext())
    }

    private val visualizadorArquivo: VisualizadorArquivo by lazy {
        VisualizadorArquivo(requireContext())
    }
}