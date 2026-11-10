@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.sekreku.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.example.sekreku.util.FotoStorage
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SekreTopBar(
    judul: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                judul,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

@Composable
fun KonfirmasiHapusDialog(nama: String, onKonfirmasi: () -> Unit, onBatal: () -> Unit) {
    AlertDialog(
        onDismissRequest = onBatal,
        title = { Text("Konfirmasi Hapus") },
        text = { Text("Yakin ingin menghapus \"$nama\"?") },
        confirmButton = {
            TextButton(onClick = onKonfirmasi) {
                Text("Ya, Hapus", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onBatal) { Text("Batal") }
        }
    )
}

@Composable
fun KolomCari(query: String, onQueryChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text(label) },
        singleLine = true,
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Hapus pencarian")
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun BarisFilter(pilihan: List<String>, terpilih: String, onPilih: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        pilihan.forEach { p ->
            FilterChip(
                selected = terpilih == p,
                onClick = { onPilih(p) },
                label = { Text(p) }
            )
        }
    }
}

@Composable
fun KeadaanKosong(pesan: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = pesan,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * [UAS-a] Komponen UI dasar: OutlinedTextField.
 * [UAS-d] CLIENT-SIDE VALIDATION: pesan error tampil lewat isError + supportingText.
 */
@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = { error?.let { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        minLines = minLines,
        modifier = Modifier.fillMaxWidth()
    )
}

/** [UAS-a] Menampilkan gambar (foto dari penyimpanan internal) memakai Coil, tanpa jaringan. */
@Composable
fun FotoView(path: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context).data(File(path)).build(),
        contentDescription = "Foto",
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(8.dp))
    )
}

/**
 * Pilih foto dari galeri. Foto langsung DISALIN ke penyimpanan internal aplikasi,
 * lalu path-nya dikirim lewat onFotoDipilih.
 */
@Composable
fun FotoPicker(
    fotoPath: String?,
    onFotoDipilih: (String) -> Unit,
    onFotoDihapus: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val hasil = withContext(Dispatchers.IO) {
                    FotoStorage.salinKeInternal(context, uri)
                }
                if (hasil != null) {
                    onFotoDipilih(hasil)
                } else {
                    Toast.makeText(context, "Gagal menyalin foto", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Foto (opsional)", style = MaterialTheme.typography.labelLarge)
        if (fotoPath != null) {
            FotoView(
                path = fotoPath,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    launcher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            ) {
                Text(if (fotoPath == null) "Pilih dari Galeri" else "Ganti Foto")
            }
            if (fotoPath != null) {
                TextButton(onClick = onFotoDihapus) {
                    Text("Hapus Foto", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}