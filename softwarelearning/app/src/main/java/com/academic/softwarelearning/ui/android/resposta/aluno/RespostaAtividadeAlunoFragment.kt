package com.academic.softwarelearning.ui.resposta.aluno

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.academic.softwarelearning.R
import com.academic.softwarelearning.ui.android.adapter.RespostaAtividadeAlunoAdapter
import com.academic.softwarelearning.domain.model.RespostaAtividadeAluno
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.database.*
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class RespostaAtividadeAlunoFragment : Fragment() {

    // --- Ajuste os caminhos se necessário ---
    private companion object {
        const val PATH_ATIVIDADES = "atividades"   // nó onde estão as atividades (titulo, status, datas…)
        const val PATH_RESPOSTAS  = "respostas"    // nó das respostas (atividadeId, texto, timestamp, usuarioId…)
        const val FIELD_TIMESTAMP_RESPOSTA = "timestamp"  // Long (ms) em respostas
        const val FIELD_ATIVIDADE_ID       = "atividadeId"
        const val FIELD_TEXTO_RESPOSTA     = "texto"
    }

    // UI
    private lateinit var recyclerView: RecyclerView
    private lateinit var edtBusca: TextInputEditText
    private lateinit var chipGroupStatus: ChipGroup
    private lateinit var chipAtivas: Chip
    private lateinit var chipEncerradas: Chip
    private lateinit var chipPendentes: Chip
    private lateinit var btnPeriodo: MaterialButton
    private lateinit var txtPeriodoSelecionado: MaterialTextView

    // Firebase
    private lateinit var dbAtividades: DatabaseReference
    private lateinit var dbRespostas: DatabaseReference

    // Dados
    private lateinit var respostaAdapter: RespostaAtividadeAlunoAdapter
    private val cachePeriodoRespostas = mutableListOf<RespostaAtividadeAluno>() // respostas trazidas pela query de período
    private val exibidos = mutableListOf<RespostaAtividadeAluno>()              // lista mostrada no adapter

    // Mapa de atividades (join)
    private val atividades = mutableMapOf<String, AtividadeInfo>()
    private var atividadesCarregadas = false

    // Estado de filtros
    private data class Filtro(
        val texto: String = "",                         // busca em titulo/descricao da atividade e texto da resposta
        val statusSelecionados: Set<String> = emptySet(), // "ATIVA","ENCERRADA","PENDENTE"
        val periodoInicial: Long? = null,               // UTC millis
        val periodoFinal: Long? = null
    )
    private var filtroAtual = Filtro()

    // Modelo mínimo para as atividades (só o que usamos para filtrar)
    private data class AtividadeInfo(
        val id: String,
        val titulo: String,
        val descricao: String?,
        val statusNorm: String,    // normalizado
        val dataInicio: Long?,     // opcional (se quiser usar depois)
        val dataFim: Long?         // opcional
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_resposta_atividade_aluno, container, false)

        // Recycler
        recyclerView = view.findViewById(R.id.recyclerRespostas)
        recyclerView.layoutManager = LinearLayoutManager(context)
        respostaAdapter = RespostaAtividadeAlunoAdapter(exibidos)
        recyclerView.adapter = respostaAdapter

        // Filtros
        edtBusca = view.findViewById(R.id.edtBusca)
        chipGroupStatus = view.findViewById(R.id.chipGroupStatus)
        chipAtivas = view.findViewById(R.id.chipAtivas)
        chipEncerradas = view.findViewById(R.id.chipEncerradas)
        chipPendentes = view.findViewById(R.id.chipPendentes)
        btnPeriodo = view.findViewById(R.id.btnPeriodo)
        txtPeriodoSelecionado = view.findViewById(R.id.txtPeriodoSelecionado)

        initFiltroListeners()

        // Firebase refs
        val db = FirebaseDatabase.getInstance()
        dbAtividades = db.getReference(PATH_ATIVIDADES)
        dbRespostas  = db.getReference(PATH_RESPOSTAS)

        // 1) Carrega atividades (uma vez) e, depois, 2) consulta respostas por período
        carregarAtividades { consultarRespostasPorPeriodo() }

        return view
    }

    // ----------------- Carregamento de Atividades -----------------

    private fun carregarAtividades(onReady: () -> Unit) {
        if (atividadesCarregadas) { onReady(); return }

        dbAtividades.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                atividades.clear()
                for (a in snapshot.children) {
                    val id = a.key ?: continue
                    val titulo = a.child("titulo").getValue(String::class.java) ?: ""
                    val descricao = a.child("descricao").getValue(String::class.java)
                    val statusRaw = a.child("status").getValue(String::class.java)
                    val statusNorm = normalizeStatus(statusRaw)
                    val dataInicio = a.child("dataInicio").getValue(Long::class.java)
                    val dataFim = a.child("dataFim").getValue(Long::class.java)

                    atividades[id] = AtividadeInfo(
                        id = id,
                        titulo = titulo,
                        descricao = descricao,
                        statusNorm = statusNorm,
                        dataInicio = dataInicio,
                        dataFim = dataFim
                    )
                }
                atividadesCarregadas = true
                onReady()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Erro ao carregar atividades", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ----------------- Listeners de Filtro -----------------

    private fun initFiltroListeners() {
        // Texto (filtra localmente após a query por período)
        edtBusca.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtroAtual = filtroAtual.copy(texto = s?.toString().orEmpty())
                aplicarJoinEBuscaLocal()
            }
        })

        // Chips de status (aplicados localmente usando o status da ATIVIDADE)
        chipGroupStatus.setOnCheckedStateChangeListener { _, _ ->
            filtroAtual = filtroAtual.copy(statusSelecionados = statusSelecionados())
            aplicarJoinEBuscaLocal()
        }

        // Período -> consulta no servidor pelas RESPOSTAS.timestamp
        btnPeriodo.setOnClickListener {
            val picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Selecione o período (resposta)")
                .build()

            picker.addOnPositiveButtonClickListener { range ->
                val inicio = range.first
                val fim = endOfDayInclusive(range.second)
                filtroAtual = filtroAtual.copy(periodoInicial = inicio, periodoFinal = fim)
                txtPeriodoSelecionado.text = "Período: ${picker.headerText}"
                consultarRespostasPorPeriodo()
            }

            picker.show(parentFragmentManager, "date_range_picker")
        }

        // Limpar período (long click)
        txtPeriodoSelecionado.setOnLongClickListener {
            filtroAtual = filtroAtual.copy(periodoInicial = null, periodoFinal = null)
            txtPeriodoSelecionado.text = ""
            consultarRespostasPorPeriodo()
            true
        }
    }

    private fun statusSelecionados(): Set<String> {
        val set = mutableSetOf<String>()
        if (chipAtivas.isChecked) set += "ATIVA"
        if (chipEncerradas.isChecked) set += "ENCERRADA"
        if (chipPendentes.isChecked) set += "PENDENTE"
        return set
    }

    // ----------------- Consulta de Respostas (período) -----------------

    /** Consulta respostas no período (server-side), depois aplica join com atividades e filtros locais. */
    private fun consultarRespostasPorPeriodo() {
        var q: Query = dbRespostas.orderByChild(FIELD_TIMESTAMP_RESPOSTA)

        filtroAtual.periodoInicial?.let { q = q.startAt(it.toDouble()) }
        filtroAtual.periodoFinal?.let { q = q.endAt(it.toDouble()) }

        q.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cachePeriodoRespostas.clear()
                for (child in snapshot.children) {
                    child.getValue(RespostaAtividadeAluno::class.java)?.let { cachePeriodoRespostas += it }
                }
                aplicarJoinEBuscaLocal()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Erro ao consultar respostas", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ----------------- Join (Resposta x Atividade) + filtros locais -----------------

    private fun aplicarJoinEBuscaLocal() {
        val textoQuery = filtroAtual.texto.trim().lowercase()
        val filtraTexto = textoQuery.isNotBlank()
        val filtraStatus = filtroAtual.statusSelecionados.isNotEmpty()

        val resultado = cachePeriodoRespostas.filter { r ->
            // Pega os campos da Resposta
            val atividadeId = r.getString(FIELD_ATIVIDADE_ID) ?: ""
            val textoResposta = r.getString(FIELD_TEXTO_RESPOSTA)?.lowercase().orEmpty()

            // Faz o join com a Atividade correspondente
            val atv = atividades[atividadeId]

            // Se não encontrou a atividade, opcionalmente você pode esconder a resposta
            if (atv == null) return@filter false

            // 1) Texto: busca em título/descrição da ATIVIDADE e também no texto da RESPOSTA
            val tituloOk = atv.titulo.lowercase().contains(textoQuery)
            val descOk   = (atv.descricao ?: "").lowercase().contains(textoQuery)
            val respOk   = textoResposta.contains(textoQuery)
            val textoPassa = if (!filtraTexto) true else (tituloOk || descOk || respOk)

            // 2) Status: usa status da ATIVIDADE (com normalização)
            val statusPassa = if (!filtraStatus) true
            else filtroAtual.statusSelecionados.contains(atv.statusNorm)

            textoPassa && statusPassa
        }

        exibidos.clear()
        exibidos.addAll(resultado)
        respostaAdapter.notifyDataSetChanged()
    }

    // ----------------- Helpers -----------------

    /** Normaliza rótulos vindos de Atividade para: ATIVA | ENCERRADA | PENDENTE */
    private fun normalizeStatus(raw: String?): String {
        val u = (raw ?: "").trim().uppercase()
        return when {
            u in setOf("ATIVA", "ATIVO", "ABERTA", "OPEN", "ATRIBUIDA", "ATRIBUÍDA", "EM_ANDAMENTO") -> "ATIVA"
            u in setOf("ENCERRADA", "FECHADA", "CLOSED", "FINALIZADA", "CONCLUIDA", "CONCLUÍDA")      -> "ENCERRADA"
            u in setOf("PENDENTE", "PENDING", "AGUARDANDO")                                           -> "PENDENTE"
            else -> u
        }
    }

    /** Fim do último dia selecionado (23:59:59.999) */
    private fun endOfDayInclusive(endUtcMs: Long): Long =
        endUtcMs + (24 * 60 * 60 * 1000 - 1)

    /** Acesso seguro a campos do model RespostaAtividadeAluno sem depender do nome do getter */
    private fun RespostaAtividadeAluno.getString(field: String): String? =
        try {
            val f = this.javaClass.getDeclaredField(field)
            f.isAccessible = true
            f.get(this)?.toString()
        } catch (_: Throwable) { null }

    // Utils (coloque no Adapter ou no Fragment)
    private fun formatDate(msOrSec: Long?): String {
        if (msOrSec == null) return "-"
        val millis = if (msOrSec < 1_000_000_000_000L) msOrSec * 1000 else msOrSec
        val df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
        return df.format(Date(millis))
    }

    private fun formatRange(start: Long?, end: Long?): String {
        val s = formatDate(start)
        val e = formatDate(end)
        return "$s • $e"
    }

}
