package com.example.sekreku.ui.notulensi

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.sekreku.DetailActivity
import com.example.sekreku.data.AppDatabase
import com.example.sekreku.data.NotulensiEntity
import com.example.sekreku.navigation.Rute
import com.example.sekreku.ui.components.*
import com.example.sekreku.util.Fmt
import com.example.sekreku.util.FotoStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ======================= DAFTAR =======================

@Composable
fun NotulensiListScreen(navController: NavHostController) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).notulensiDao() }
    val scope = rememberCoroutineScope()

    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("Semua") }
    var idHapus by rememberSaveable { mutableStateOf<Int?>(null) }

    val daftar = remember { mutableStateListOf<NotulensiEntity>() }

    // [UAS-e] CRUD - READ: ambil semua data dari Room (Dispatchers.IO)
    suspend fun muatUlang() {
        val data = withContext(Dispatchers.IO) { dao.getAll() }
        daftar.clear()
        daftar.addAll(data)
    }

    LaunchedEffect(Unit) { muatUlang() }

    // [UAS-d] REACTIVE UI: daftar langsung tersaring saat mengetik / memilih filter
    // Pencarian + filter lampiran, diurutkan dari tanggal rapat terbaru
    val tampil = daftar
        .filter { n ->
            (filter == "Semua" || n.fotoPath != null) &&
                    (query.isBlank() ||
                            n.judul.contains(query, ignoreCase = true) ||
                            n.peserta.contains(query, ignoreCase = true) ||
                            n.pembahasan.contains(query, ignoreCase = true))
        }
        .sortedByDescending { Fmt.kunciUrut(it.tanggal) }

    // [UAS-e] CRUD - DELETE: dialog konfirmasi sebelum data dihapus
    idHapus?.let { id ->
        val target = daftar.firstOrNull { it.id == id }
        KonfirmasiHapusDialog(
            nama = target?.judul ?: "",
            onKonfirmasi = {
                idHapus = null
                if (target != null) {
                    scope.launch {
                        // [UAS-d] DEFENSIVE: operasi database dibungkus try-catch
                        try {
                            withContext(Dispatchers.IO) {
                                dao.delete(target)
                                FotoStorage.hapus(target.fotoPath)
                            }
                            muatUlang()
                            Toast.makeText(context, "Notulensi dihapus", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Gagal menghapus data", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            onBatal = { idHapus = null }
        )
    }

    Scaffold(
        topBar = {
            SekreTopBar("Notulensi Rapat", onBack = { navController.popBackStack() })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Rute.notulensiForm(-1)) }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah notulensi")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            KolomCari(query, { query = it }, "Cari judul / peserta / pembahasan")
            BarisFilter(listOf("Semua", "Ada Lampiran Foto"), if (filter == "Semua") "Semua" else "Ada Lampiran Foto") {
                filter = if (it == "Semua") "Semua" else "Lampiran"
            }
            Text(
                text = "Daftar Notulensi (${tampil.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            if (tampil.isEmpty()) {
                KeadaanKosong(
                    if (daftar.isEmpty()) "Belum ada notulensi.\nKetuk tombol + untuk menambah."
                    else "Tidak ada notulensi yang cocok."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(tampil, key = { it.id }) { notulen ->
                        NotulensiCard(
                            notulen = notulen,
                            onDetail = {
                                // [UAS-c] EXPLICIT INTENT -> DetailActivity, kirim data lewat putExtra
                                val intent = Intent(context, DetailActivity::class.java)
                                intent.putExtra(DetailActivity.EXTRA_JENIS, "Notulensi")
                                intent.putExtra(DetailActivity.EXTRA_JUDUL, notulen.judul)
                                intent.putStringArrayListExtra(DetailActivity.EXTRA_LABEL, arrayListOf("Tanggal", "Peserta", "Pembahasan", "Tindak Lanjut"))
                                intent.putStringArrayListExtra(DetailActivity.EXTRA_ISI, arrayListOf(notulen.tanggal, notulen.peserta, notulen.pembahasan, notulen.tindakLanjut))
                                intent.putExtra(DetailActivity.EXTRA_FOTO, notulen.fotoPath)
                                context.startActivity(intent)
                            },
                            onEdit = { navController.navigate(Rute.notulensiForm(notulen.id)) },
                            onDelete = { idHapus = notulen.id }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotulensiCard(
    notulen: NotulensiEntity,
    onDetail: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // Ketuk kartu untuk membuka / menutup isi lengkap
    var terbuka by rememberSaveable(notulen.id) { mutableStateOf(false) }
    val warna = MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { terbuka = !terbuka },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = notulen.judul, fontWeight = FontWeight.Bold, color = warna)
            Text(text = notulen.tanggal, fontSize = 12.sp, color = warna)
            Text(
                text = "Peserta: ${notulen.peserta}",
                fontSize = 13.sp,
                color = warna,
                maxLines = if (terbuka) Int.MAX_VALUE else 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notulen.pembahasan,
                color = warna,
                maxLines = if (terbuka) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )
            if (terbuka) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Tindak lanjut:", fontWeight = FontWeight.SemiBold, color = warna)
                Text(text = notulen.tindakLanjut.ifBlank { "-" }, color = warna)
                if (notulen.fotoPath != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    FotoView(
                        path = notulen.fotoPath,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (terbuka) "Ketuk untuk menutup" else "Ketuk untuk membuka",
                    fontSize = 11.sp,
                    color = warna,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    TextButton(onClick = onDetail) { Text("Detail") }
                    TextButton(onClick = onEdit) { Text("Edit") }
                    TextButton(onClick = onDelete) {
                        Text("Hapus", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// ======================= FORM (TAMBAH / EDIT) =======================

@Composable
fun NotulensiFormScreen(navController: NavHostController, idEdit: Int) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).notulensiDao() }
    val scope = rememberCoroutineScope()
    val modeEdit = idEdit != -1

    // [UAS-d] ROTASI LAYAR: rememberSaveable -> isi form tidak hilang saat layar diputar
    var judul by rememberSaveable { mutableStateOf("") }
    var tanggal by rememberSaveable { mutableStateOf(Fmt.hariIni()) }
    var peserta by rememberSaveable { mutableStateOf("") }
    var pembahasan by rememberSaveable { mutableStateOf("") }
    var tindakLanjut by rememberSaveable { mutableStateOf("") }
    var fotoPath by rememberSaveable { mutableStateOf<String?>(null) }
    var fotoAsli by rememberSaveable { mutableStateOf<String?>(null) }
    var sudahMuat by rememberSaveable { mutableStateOf(!modeEdit) }
    // [UAS-d] DEFENSIVE: cegah tombol Simpan ditekan berulang (data ganda)
    var sedangSimpan by remember { mutableStateOf(false) }

    var judulError by rememberSaveable { mutableStateOf<String?>(null) }
    var tanggalError by rememberSaveable { mutableStateOf<String?>(null) }
    var pesertaError by rememberSaveable { mutableStateOf<String?>(null) }
    var pembahasanError by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(idEdit) {
        if (modeEdit && !sudahMuat) {
            val data = withContext(Dispatchers.IO) { dao.getById(idEdit) }
            // [UAS-d] DEFENSIVE: data tidak ditemukan -> kembali, tidak crash
            if (data == null) {
                navController.popBackStack()
            } else {
                judul = data.judul
                tanggal = data.tanggal
                peserta = data.peserta
                pembahasan = data.pembahasan
                tindakLanjut = data.tindakLanjut
                fotoPath = data.fotoPath
                fotoAsli = data.fotoPath
                sudahMuat = true
            }
        }
    }

    fun batalkan() {
        if (fotoPath != null && fotoPath != fotoAsli) FotoStorage.hapus(fotoPath)
        navController.popBackStack()
    }
    BackHandler { batalkan() }

    Scaffold(
        topBar = {
            SekreTopBar(
                judul = if (modeEdit) "Edit Notulensi" else "Tambah Notulensi",
                onBack = { batalkan() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FormField(judul, { judul = it; judulError = null }, "Judul Rapat", judulError)
            FormField(
                tanggal, { tanggal = it; tanggalError = null }, "Tanggal (dd/MM/yyyy)", tanggalError
            )
            FormField(peserta, { peserta = it; pesertaError = null }, "Peserta (pisahkan dengan koma)", pesertaError, minLines = 2)
            FormField(pembahasan, { pembahasan = it; pembahasanError = null }, "Pembahasan", pembahasanError, minLines = 4)
            FormField(tindakLanjut, { tindakLanjut = it }, "Tindak Lanjut (opsional)", null, minLines = 3)

            FotoPicker(
                fotoPath = fotoPath,
                onFotoDipilih = { baru ->
                    if (fotoPath != null && fotoPath != fotoAsli) FotoStorage.hapus(fotoPath)
                    fotoPath = baru
                },
                onFotoDihapus = {
                    if (fotoPath != null && fotoPath != fotoAsli) FotoStorage.hapus(fotoPath)
                    fotoPath = null
                }
            )

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        // [UAS-d] CLIENT-SIDE VALIDATION + REACTIVE UI (pesan error hilang saat mengetik)
                        var valid = true
                        if (judul.isBlank()) { judulError = "Judul rapat wajib diisi!"; valid = false }
                        if (!Fmt.tanggalValid(tanggal)) {
                            tanggalError = "Tanggal harus berformat dd/MM/yyyy dan valid!"
                            valid = false
                        }
                        if (peserta.isBlank()) { pesertaError = "Peserta wajib diisi!"; valid = false }
                        if (pembahasan.isBlank()) { pembahasanError = "Pembahasan wajib diisi!"; valid = false }
                        if (!valid) return@Button
                        if (sedangSimpan) return@Button

                        val data = NotulensiEntity(
                            id = if (modeEdit) idEdit else 0,
                            judul = judul.trim(),
                            tanggal = tanggal.trim(),
                            peserta = peserta.trim(),
                            pembahasan = pembahasan.trim(),
                            tindakLanjut = tindakLanjut.trim(),
                            fotoPath = fotoPath
                        )
                        val fotoLama = fotoAsli
                        val fotoBaru = fotoPath

                        sedangSimpan = true
                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    // [UAS-e] CRUD - UPDATE (mode edit) atau CREATE (mode tambah) ke Room
                                    if (modeEdit) dao.update(data) else dao.insert(data)
                                    if (fotoLama != null && fotoLama != fotoBaru) FotoStorage.hapus(fotoLama)
                                }
                                Toast.makeText(
                                    context,
                                    if (modeEdit) "Notulensi diperbarui" else "Notulensi tersimpan",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.popBackStack()
                            } catch (e: Exception) {
                                sedangSimpan = false
                                Toast.makeText(context, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !sedangSimpan,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (modeEdit) "Perbarui Notulensi" else "Simpan Notulensi")
                }
                OutlinedButton(onClick = { batalkan() }) { Text("Batal") }
            }
        }
    }
}