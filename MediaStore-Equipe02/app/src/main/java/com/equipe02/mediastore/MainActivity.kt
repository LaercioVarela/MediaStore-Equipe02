package com.equipe02.mediastore

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var repositorio: MediaStoreRepository
    private lateinit var lista: LinearLayout
    private lateinit var status: TextView
    private lateinit var pagina: TextView
    private lateinit var consultar: Button
    private lateinit var anterior: Button
    private lateinit var proxima: Button
    // Guardamos apenas os marcadores de página, não todas as imagens da galeria.
    private val marcadores = mutableListOf<Long?>(null)
    private var indice = 0
    private var atual = emptyList<Imagem>()
    private var ocupado = false
    private var fim = false

    // A API de resultados recebe a resposta assíncrona da caixa de permissões.
    private val solicitar = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (temAcesso()) reiniciar() else status.text = "Permissão negada. Autorize o acesso às imagens nas configurações."
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        repositorio = MediaStoreRepository(contentResolver)
        lista = findViewById(R.id.lista)
        status = findViewById(R.id.status)
        pagina = findViewById(R.id.pagina)
        consultar = findViewById(R.id.consultar)
        anterior = findViewById(R.id.anterior)
        proxima = findViewById(R.id.proxima)
        // Listeners reagem a eventos sem espera ativa (polling).
        consultar.setOnClickListener { if (temAcesso()) reiniciar() else solicitar.launch(permissoes()) }
        anterior.setOnClickListener { if (indice > 0) carregar(indice - 1, marcadores[indice - 1]) }
        proxima.setOnClickListener { atual.lastOrNull()?.let { carregar(indice + 1, it.id) } }
    }

    private fun permissoes(): Array<String> = when {
        Build.VERSION.SDK_INT >= 34 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        Build.VERSION.SDK_INT >= 33 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    private fun temAcesso() = permissoes().any { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }

    private fun reiniciar() { carregar(0, null, true) }

    private fun carregar(destino: Int, marcador: Long?, reset: Boolean = false) {
        if (ocupado) return
        ocupado = true
        atualizarBotoes()
        status.text = "Consultando imagens..."
        // lifecycleScope cancela a corrotina quando a Activity é destruída.
        lifecycleScope.launch {
            try {
                val resultado = repositorio.consultar(marcador)
                // Se a próxima página estiver vazia, preservamos a página atual.
                if (resultado.isEmpty() && destino > indice && !reset) {
                    fim = true
                    status.text = "Fim da galeria • ${atual.size} imagens nesta página"
                    return@launch
                }
                if (reset) { marcadores.clear(); marcadores.add(null) }
                if (destino >= marcadores.size) marcadores.add(marcador) else marcadores[destino] = marcador
                indice = destino
                atual = resultado
                fim = resultado.size < MediaStoreRepository.LIMITE
                // Após withContext(IO), voltamos à Main Thread para atualizar a interface.
                lista.removeAllViews()
                Log.i("MEDIA_STORE", "Página ${indice + 1}: ${resultado.size} imagens")
                resultado.forEach { imagem ->
                    val kb = String.format(Locale.US, "%.2f KB", imagem.tamanhoKB)
                    Log.i("MEDIA_STORE", "${imagem.nome} - $kb")
                    val linha = TextView(this@MainActivity).apply {
                        text = "${imagem.nome}\n$kb"
                        textSize = 16f
                        setTextColor(Color.WHITE)
                        setBackgroundColor(Color.parseColor("#1E293B"))
                        val p = (14 * resources.displayMetrics.density).toInt()
                        setPadding(p, p, p, p)
                    }
                    val params = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = (8 * resources.displayMetrics.density).toInt() }
                    lista.addView(linha, params)
                }
                pagina.text = "Página ${indice + 1}"
                status.text = if (resultado.isEmpty()) "Nenhuma imagem acessível. Adicione fotos ao emulador." else "${resultado.size} imagens nesta página • limite 10"
            } catch (e: CancellationException) {
                throw e // Não engolir cancelamentos do ciclo de vida.
            } catch (e: Exception) {
                Log.e("MEDIA_STORE", "Falha na consulta", e)
                status.text = "Não foi possível consultar a galeria. Verifique as permissões."
            } finally {
                ocupado = false
                atualizarBotoes()
            }
        }
    }
    private fun atualizarBotoes() {
        consultar.isEnabled = !ocupado
        anterior.isEnabled = !ocupado && indice > 0
        proxima.isEnabled = !ocupado && !fim && atual.isNotEmpty()
    }
}
