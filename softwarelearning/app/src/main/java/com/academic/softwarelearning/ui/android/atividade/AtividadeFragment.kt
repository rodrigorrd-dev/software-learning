package com.academic.softwarelearning.service

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.academic.softwarelearning.R
import com.academic.softwarelearning.ui.android.adapter.ArquivoAdapter
import com.academic.softwarelearning.ui.android.adapter.AtividadeAdapter
import com.academic.softwarelearning.domain.model.Atividade
import com.academic.softwarelearning.infrastructure.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AtividadeFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var atividadesList: RecyclerView
    private lateinit var atividadesAdapter: AtividadeAdapter
    private lateinit var spinnerCurso: Spinner
    private lateinit var edtTitulo: EditText
    private lateinit var edtDescricao: EditText
    private lateinit var edtDataInicio: EditText
    private lateinit var edtDataFim: EditText
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnAdicionar: Button
    private lateinit var btnAdicionarArquivos: Button
    private val usuarioId: String = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    private lateinit var recyclerViewArquivos: RecyclerView
    private lateinit var arquivoAdapter: ArquivoAdapter
    private val arquivosAnexados = mutableListOf<Uri>()
    private var lastPhotoUri: Uri? = null
    private var photoCount = 1
    private val calendar = Calendar.getInstance()

    // ------- Launchers -------
    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                lastPhotoUri?.let { uri ->
                    arquivosAnexados.add(uri)
                    arquivoAdapter.notifyItemInserted(arquivosAnexados.size - 1)
                    Log.d("AtividadeFragment", "Foto tirada: ${uri.lastPathSegment}")
                }
            } else {
                Log.d("AtividadeFragment", "Falha ao tirar a foto")
            }
        }

    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                arquivosAnexados.add(it)
                arquivoAdapter.notifyItemInserted(arquivosAnexados.size - 1)
                Log.d("AtividadeFragment", "Arquivo selecionado: ${getFileNameFromUri(it)}")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_atividade, container, false)

        // Firebase
        database = FirebaseDatabase.getInstance().reference.child("atividades")

        // Campos
        spinnerCurso = rootView.findViewById(R.id.spinnerCurso)
        edtTitulo = rootView.findViewById(R.id.edtTitulo)
        edtDescricao = rootView.findViewById(R.id.edtDescricao)
        edtDataInicio = rootView.findViewById(R.id.edtDataInicio)
        edtDataFim = rootView.findViewById(R.id.edtDataFim)
        spinnerStatus = rootView.findViewById(R.id.spinnerStatus)
        btnAdicionar = rootView.findViewById(R.id.btnAdicionar)
        btnAdicionarArquivos = rootView.findViewById(R.id.btnAdicionarArquivos)
        recyclerViewArquivos = rootView.findViewById(R.id.recyclerViewArquivos)
        atividadesList = rootView.findViewById(R.id.recyclerViewAtividades)

        // Spinner Status
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            arrayOf("Atribuída", "Cancelada", "Concluída")
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerStatus.adapter = it
        }

        // RecyclerView atividades
        atividadesList.layoutManager = LinearLayoutManager(requireContext())
        atividadesAdapter = AtividadeAdapter { atividade -> removeAtividade(atividade) }
        atividadesList.adapter = atividadesAdapter

        // RecyclerView Arquivos
        recyclerViewArquivos.layoutManager = LinearLayoutManager(requireContext())
        arquivoAdapter = ArquivoAdapter(arquivosAnexados)
        recyclerViewArquivos.adapter = arquivoAdapter

        // Pickers de data (dd/MM/yyyy)
        edtDataInicio.setOnClickListener {
            showDatePicker { y, m, d -> edtDataInicio.setText("%02d/%02d/%04d".format(d, m + 1, y)) }
        }
        edtDataFim.setOnClickListener {
            showDatePicker { y, m, d -> edtDataFim.setText("%02d/%02d/%04d".format(d, m + 1, y)) }
        }

        // Botões
        btnAdicionarArquivos.setOnClickListener { openFilePicker() }
        btnAdicionar.setOnClickListener { addAtividade() }

        // Carregar dados
        loadCursos()
        loadAtividades()

        return rootView
    }

    // ---------- UI helpers ----------
    private fun showDatePicker(onDateSet: (year: Int, month: Int, day: Int) -> Unit) {
        DatePickerDialog(
            requireContext(),
            { _, y, m, d -> onDateSet(y, m, d) },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun openFilePicker() {
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
            }.show()
    }

    // ---------- Firebase: Cursos ----------
    private fun loadCursos() {
        FirebaseDatabase.getInstance().reference.child("cursos")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val cursos = snap.children.mapNotNull { it.child("nome").getValue(String::class.java) }
                    ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cursos).also {
                        it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerCurso.adapter = it
                    }
                }
                override fun onCancelled(err: DatabaseError) {
                    Log.e("AtividadeFragment", "Erro ao carregar cursos: ${err.message}")
                }
            })
    }

    // ---------- Firebase: Atividades ----------
    private fun loadAtividades() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Atividade>()
                snapshot.children.forEach { ds ->
                    val id = ds.key ?: return@forEach
                    val responsavel = ds.child("responsavel").getValue(String::class.java) ?: ""
                    val tipoarquivo = ds.child("tipoarquivo").getValue(String::class.java) ?: ""
                    val titulo = ds.child("titulo").getValue(String::class.java) ?: ""
                    val descricao = ds.child("descricao").getValue(String::class.java) ?: ""
                    val status = ds.child("status").getValue(String::class.java) ?: ""

                    val dataIni = snapshotLongFlexible(ds, "dataInicio")
                    val dataFim = snapshotLongFlexible(ds, "dataFim")

                    val arquivos = mutableListOf<String>()
                    val nodeArquivos = ds.child("arquivos")
                    if (nodeArquivos.exists()) {
                        nodeArquivos.children.forEach { a ->
                            a.getValue(String::class.java)?.let(arquivos::add)
                        }
                    }

                    lista += Atividade(
                        id = id,
                        responsavel = responsavel,
                        tipoarquivo = tipoarquivo,
                        titulo = titulo,
                        descricao = descricao,
                        arquivos = arquivos,
                        dataInicio = dataIni,
                        dataFim = dataFim,
                        status = status
                    )
                }
                atividadesAdapter.submitList(lista)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AtividadeFragment", "Erro ao carregar atividades: ${error.message}")
            }
        })
    }

    /** Lê um campo numérico do snapshot (Long/Double/String numérica) e retorna em millis. */
    private fun snapshotLongFlexible(ds: DataSnapshot, key: String): Long {
        val node = ds.child(key)
        if (!node.exists()) return 0L
        val v = node.value
        return when (v) {
            is Number -> v.toLong()
            is String -> v.filter { it.isDigit() }.toLongOrNull() ?: 0L
            else -> 0L
        }
    }

    private fun converterParaTimestamp(dataStr: String): Long {
        return try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .parse(dataStr)?.time ?: 0L
        } catch (e: Exception) {
            Log.e("ConversaoData", "Falha ao converter data '$dataStr': ${e.message}")
            0L
        }
    }

    private fun addAtividade() {
        val titulo = edtTitulo.text.toString().trim()
        val responsavel = usuarioId
        val desc = edtDescricao.text.toString().trim()
        val dtIni = converterParaTimestamp(edtDataInicio.text.toString())
        val dtFim = converterParaTimestamp(edtDataFim.text.toString())
        val status = spinnerStatus.selectedItem.toString()
        val curso = spinnerCurso.selectedItem?.toString().orEmpty()

        if (titulo.isBlank() || desc.isBlank()) {
            Toast.makeText(context, "Preencha título e descrição", Toast.LENGTH_SHORT).show()
            return
        }
        if (dtIni == 0L || dtFim == 0L) {
            Toast.makeText(context, "Defina datas válidas (dd/MM/yyyy)", Toast.LENGTH_SHORT).show()
            return
        }

        val tipoarquivo = ""
        val ref = database.push()
        val id = ref.key ?: return
        val atividade = Atividade(
            id = id,
            responsavel = responsavel,
            tipoarquivo = tipoarquivo,
            titulo = titulo,
            descricao = desc,
            arquivos = emptyList(),
            dataInicio = dtIni,
            dataFim = dtFim,
            status = status
        )

        ref.setValue(atividade).addOnCompleteListener {
            if (it.isSuccessful) {
                lifecycleScope.launch {
                    val urls = uploadFilesToApi(id)
                    database.child(id).child("arquivos").setValue(urls)
                }
                edtTitulo.text.clear()
                edtDescricao.text.clear()
                edtDataInicio.text.clear()
                edtDataFim.text.clear()
                arquivosAnexados.clear()
                arquivoAdapter.notifyDataSetChanged()
                Toast.makeText(context, "Atividade adicionada ao curso $curso", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Falha ao adicionar atividade", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ---------- Upload ----------
    private suspend fun uploadFilesToApi(codigoAtividade: String): List<String> =
        withContext(Dispatchers.IO) {
            val urls = mutableListOf<String>()
            val mediaText = "text/plain".toMediaType()
            val usuarioId = FirebaseAuth.getInstance().currentUser?.uid ?: "desconhecido"

            arquivosAnexados.forEach { uri ->
                try {
                    val name = getFileNameFromUri(uri)
                    val contentResolver = requireContext().contentResolver
                    val mimeType = contentResolver.getType(uri) ?: "*/*"
                    val mediaStream = mimeType.toMediaType()
                    val bytes = contentResolver.openInputStream(uri)!!.readBytes()

                    val partFile = MultipartBody.Part.createFormData(
                        "arquivo", name, bytes.toRequestBody(mediaStream)
                    )

                    val response = RetrofitClient.apiService.adicionarArquivo(
                        codigo = codigoAtividade.toRequestBody(mediaText),
                        arquivo = partFile,
                        responsavel = usuarioId.toRequestBody(mediaText),
                        tipoarquivo = mimeType.toRequestBody(mediaText),
                        nome = name.toRequestBody(mediaText)
                    )

                    urls += response.arquivo
                } catch (e: Exception) {
                    Log.e("UploadErro", "Falha no upload do arquivo ${uri.path}: ${e.message}", e)
                }
            }
            urls
        }

    private fun getFileNameFromUri(uri: Uri): String {
        return context?.contentResolver
            ?.query(uri, null, null, null, null)
            ?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(idx)
            } ?: "arquivo_desconhecido"
    }

    private fun removeAtividade(atividade: Atividade) {
        database.child(atividade.id).removeValue()
            .addOnCompleteListener {
                val msg = if (it.isSuccessful) "Removida com sucesso" else "Falha ao remover"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
    }
}
