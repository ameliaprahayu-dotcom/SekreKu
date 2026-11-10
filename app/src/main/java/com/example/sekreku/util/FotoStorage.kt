package com.example.sekreku.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

/**
 * Foto dari galeri DISALIN ke penyimpanan internal aplikasi (filesDir/foto),
 * sehingga tetap ada walaupun file aslinya dihapus dari galeri.
 * Panggil fungsi ini dari background thread (Dispatchers.IO).
 */
object FotoStorage {

    fun salinKeInternal(context: Context, uri: Uri): String? {
        return try {
            val folder = File(context.filesDir, "foto").apply { mkdirs() }
            val tujuan = File(folder, "foto_${UUID.randomUUID()}.jpg")
            val input = context.contentResolver.openInputStream(uri) ?: return null
            input.use { masuk ->
                tujuan.outputStream().use { keluar -> masuk.copyTo(keluar) }
            }
            tujuan.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun hapus(path: String?) {
        if (path.isNullOrBlank()) return
        try {
            File(path).delete()
        } catch (e: Exception) {
            // diabaikan: file mungkin sudah tidak ada
        }
    }
}
