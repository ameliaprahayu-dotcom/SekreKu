package com.example.sekreku

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sekreku.ui.components.FotoView
import com.example.sekreku.ui.components.SekreTopBar
import com.example.sekreku.ui.theme.SekreKuTheme
import java.io.File

/**
 * [UAS-c] LAYAR KE-2. Dibuka dari layar daftar memakai EXPLICIT INTENT:
 *     Intent(context, DetailActivity::class.java) + putExtra(...)
 * Data (jenis, judul, label, isi, foto) DIKIRIM antar layar lewat Intent extras
 * lalu DITERIMA di sini lewat intent.getStringExtra(...).
 */
class DetailActivity : ComponentActivity() {

    companion object {
        // Kunci data Intent (dipakai pengirim dan penerima agar tidak salah ketik)
        const val EXTRA_JENIS = "extra_jenis"
        const val EXTRA_JUDUL = "extra_judul"
        const val EXTRA_LABEL = "extra_label"
        const val EXTRA_ISI = "extra_isi"
        const val EXTRA_FOTO = "extra_foto"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // [UAS-c] TERIMA data yang dikirim lewat Intent
        val jenis = intent.getStringExtra(EXTRA_JENIS)
        val judul = intent.getStringExtra(EXTRA_JUDUL)

        // [UAS-d] DEFENSIVE PROGRAMMING: jika data tidak lengkap, jangan crash -> tutup layar
        if (jenis == null || judul == null) {
            Toast.makeText(this, "Data tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val label = intent.getStringArrayListExtra(EXTRA_LABEL) ?: arrayListOf()
        val isi = intent.getStringArrayListExtra(EXTRA_ISI) ?: arrayListOf()
        val jumlahBaris = minOf(label.size, isi.size)
        // [UAS-d] DEFENSIVE: foto hanya ditampilkan bila file-nya benar-benar ada
        val fotoPath = intent.getStringExtra(EXTRA_FOTO)?.takeIf { File(it).exists() }

        setContent {
            SekreKuTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        topBar = { SekreTopBar(judul = "Detail $jenis", onBack = { finish() }) }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = judul,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (fotoPath != null) {
                                FotoView(
                                    path = fotoPath,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                )
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    for (i in 0 until jumlahBaris) {
                                        Column {
                                            Text(
                                                text = label[i],
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = isi[i].ifBlank { "-" },
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = { finish() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Kembali")
                            }
                        }
                    }
                }
            }
        }
    }
}