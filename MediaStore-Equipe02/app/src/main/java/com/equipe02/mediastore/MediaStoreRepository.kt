package com.equipe02.mediastore

import android.content.ContentResolver
import android.os.Bundle
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreRepository(private val resolver: ContentResolver) {
    companion object { const val LIMITE = 10 }

    // A consulta é suspensa e executada em IO: não bloqueia a thread da interface.
    suspend fun consultar(aposId: Long?): List<Imagem> = withContext(Dispatchers.IO) {
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val id = MediaStore.Images.Media._ID
        val nome = MediaStore.Images.Media.DISPLAY_NAME
        val tamanho = MediaStore.Images.Media.SIZE
        // Projeção mínima reduz a transferência de dados pelo Content Provider.
        val argumentos = Bundle().apply {
            putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(id))
            putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, ContentResolver.QUERY_SORT_DIRECTION_ASCENDING)
            putInt(ContentResolver.QUERY_ARG_LIMIT, LIMITE)
            // Paginação por chave: evita percorrer todos os registros das páginas anteriores.
            if (aposId != null) {
                putString(ContentResolver.QUERY_ARG_SQL_SELECTION, "$id > ?")
                putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, arrayOf(aposId.toString()))
            }
        }
        val imagens = ArrayList<Imagem>(LIMITE)
        // use fecha o Cursor mesmo quando uma exceção acontece.
        resolver.query(uri, arrayOf(id, nome, tamanho), argumentos, null)?.use { cursor ->
            val colunaId = cursor.getColumnIndexOrThrow(id)
            val colunaNome = cursor.getColumnIndexOrThrow(nome)
            val colunaTamanho = cursor.getColumnIndexOrThrow(tamanho)
            // Limite defensivo caso o provedor não respeite QUERY_ARG_LIMIT.
            while (imagens.size < LIMITE && cursor.moveToNext()) {
                imagens.add(Imagem(cursor.getLong(colunaId), cursor.getString(colunaNome) ?: "Sem nome", cursor.getLong(colunaTamanho)))
            }
        }
        imagens
    }
}
