package com.example.sekreku.ui.inventaris

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.sekreku.DetailActivity
import com.example.sekreku.data.AppDatabase
import com.example.sekreku.data.InventarisEntity
import com.example.sekreku.navigation.Rute
import com.example.sekreku.ui.components.*
import com.example.sekreku.util.DAFTAR_KONDISI
import com.example.sekreku.util.FotoStorage
import com.example.sekreku.util.Fmt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ======================= DAFTAR =======================

@Composable
fun InventarisListScreen(navController: NavHostController) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).inventarisDao() }
    val scope = rememberCoroutineScope()

    var query by rememberSaveable { mutableStateOf("") }
    var filterKondisi by rememberSaveable { mutableStateOf("Semua") }
    var idHapus by rememberSaveable { mutableStateOf<Int?>(null) }

    val daftar = remember { mutableStateListOf<InventarisEntity>() }

    // READ: baca dari Room di background (IO), isi ke state di Main
    // [UAS-e] CRUD - READ: ambil semua data dari Room (Dispatchers.IO)
    suspend fun muatUlang() {
        val data = withContext(Dispatchers.IO) { dao.getAll() }
        daftar.clear()
        daftar.addAll(data)
    }

    LaunchedEffect(Unit) { muatUlang() }

    // [UAS-d] REACTIVE UI: daftar langsung tersaring saat mengetik / memilih filter
    // Pencarian + filter kondisi
    val tampil = daftar.filter { b ->
        (filterKondisi == "Semua" || b.kondisi == filterKondisi) &&
                (query.isBlank() ||
                        b.nama.contains(query, ignoreCase = true) ||
                        b.kategori.contains(query, ignoreCase = true) ||
                        b.lokasi.contains(query, ignoreCase = true))
    }

    // Dialog konfirmasi hapus
    // [UAS-e] CRUD - DELETE: dialog konfirmasi sebelum data dihapus
    idHapus?.let { id ->
        val target = daftar.firstOrNull { it.id == id }
        KonfirmasiHapusDialog(
            nama = target?.nama ?: "",
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
                            Toast.makeText(context, "Barang dihapus", Toast.LENGTH_SHORT).show()
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
            SekreTopBar("Inventaris Kantor", onBack = { navController.popBackStack() })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Rute.inventarisForm(-1)) }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah barang")
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
            KolomCari(query, { query = it }, "Cari nama / kategori / lokasi")
            BarisFilter(listOf("Semua") + DAFTAR_KONDISI, filterKondisi) { filterKondisi = it }
            Text(
                text = "Daftar Barang (${tampil.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            if (tampil.isEmpty()) {
                KeadaanKosong(
                    if (daftar.isEmpty()) "Belum ada barang.\nKetuk tombol + untuk menambah."
                    else "Tidak ada barang yang cocok."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(tampil, key = { it.id }) { barang ->
                        InventarisCard(
                            barang = barang,
                            onDetail = {
                                // [UAS-c] EXPLICIT INTENT -> DetailActivity, kirim data lewat putExtra
                                val intent = Intent(context, DetailActivity::class.java)
                                intent.putExtra(DetailActivity.EXTRA_JENIS, "Inventaris")
                                intent.putExtra(DetailActivity.EXTRA_JUDUL, barang.nama)
                                intent.putStringArrayListExtra(DetailActivity.EXTRA_LABEL, arrayListOf("Kategori", "Jumlah", "Lokasi", "Kondisi", "Dibuat"))
                                intent.putStringArrayListExtra(DetailActivity.EXTRA_ISI, arrayListOf(barang.kategori, barang.jumlah.toString(), barang.lokasi, barang.kondisi, barang.tanggalDibuat))
                                intent.putExtra(DetailActivity.EXTRA_FOTO, barang.fotoPath)
                                context.startActivity(intent)
                            },
                            onEdit = { navController.navigate(Rute.inventarisForm(barang.id)) },
                            onDelete = { idHapus = barang.id }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InventarisCard(
    barang: InventarisEntity,
    onDetail: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (barang.fotoPath != null) {
                    FotoView(path = barang.fotoPath, modifier = Modifier.size(72.dp))
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = barang.nama,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${barang.kategori} • ${barang.lokasi}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Jumlah: ${barang.jumlah} • Kondisi: ${barang.kondisi}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDetail) { Text("Detail") }
                TextButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = onDelete) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// ======================= FORM (TAMBAH / EDIT) =======================

@Composable
fun InventarisFormScreen(navController: NavHostController, idEdit: Int) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).inventarisDao() }
    val scope = rememberCoroutineScope()
    val modeEdit = idEdit != -1

    // [UAS-d] ROTASI LAYAR: rememberSaveable -> isi form tidak hilang saat layar diputar
    var nama by rememberSaveable { mutableStateOf("") }
    var kategori by rememberSaveable { mutableStateOf("") }
    var jumlah by rememberSaveable { mutableStateOf("") }
    var lokasi by rememberSaveable { mutableStateOf("") }
    var kondisi by rememberSaveable { mutableStateOf(DAFTAR_KONDISI.first()) }
    var fotoPath by rememberSaveable { mutableStateOf<String?>(null) }
    var fotoAsli by rememberSaveable { mutableStateOf<String?>(null) }
    var tanggalAsli by rememberSaveable { mutableStateOf("") }
    var sudahMuat by rememberSaveable { mutableStateOf(!modeEdit) }
    // [UAS-d] DEFENSIVE: cegah tombol Simpan ditekan berulang (data ganda)
    var sedangSimpan by remember { mutableStateOf(false) }

    var namaError by rememberSaveable { mutableStateOf<String?>(null) }
    var kategoriError by rememberSaveable { mutableStateOf<String?>(null) }
    var jumlahError by rememberSaveable { mutableStateOf<String?>(null) }
    var lokasiError by rememberSaveable { mutableStateOf<String?>(null) }

    // Mode edit: muat data lama sekali saja
    LaunchedEffect(idEdit) {
        if (modeEdit && !sudahMuat) {
            val data = withContext(Dispatchers.IO) { dao.getById(idEdit) }
            // [UAS-d] DEFENSIVE: data tidak ditemukan -> kembali, tidak crash
            if (data == null) {
                navController.popBackStack()
            } else {
                nama = data.nama
                kategori = data.kategori
                jumlah = data.jumlah.toString()
                lokasi = data.lokasi
                kondisi = data.kondisi
                fotoPath = data.fotoPath
                fotoAsli = data.fotoPath
                tanggalAsli = data.tanggalDibuat
                sudahMuat = true
            }
        }
    }

    // Foto baru yang belum disimpan dibersihkan saat batal / kembali
    fun batalkan() {
        if (fotoPath != null && fotoPath != fotoAsli) FotoStorage.hapus(fotoPath)
        navController.popBackStack()
    }
    BackHandler { batalkan() }

    Scaffold(
        topBar = {
            SekreTopBar(
                judul = if (modeEdit) "Edit Barang" else "Tambah Barang",
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
            FormField(nama, { nama = it; namaError = null }, "Nama Barang", namaError)
            FormField(kategori, { kategori = it; kategoriError = null }, "Kategori (mis. Elektronik, ATK)", kategoriError)
            FormField(
                jumlah, { jumlah = it; jumlahError = null }, "Jumlah", jumlahError,
                keyboardType = KeyboardType.Number
            )
            FormField(lokasi, { lokasi = it; lokasiError = null }, "Lokasi (mis. Ruang Arsip)", lokasiError)

            Text("Kondisi", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DAFTAR_KONDISI.forEach { k ->
                    FilterChip(
                        selected = kondisi == k,
                        onClick = { kondisi = k },
                        label = { Text(k) }
                    )
                }
            }

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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        // 1) Validasi
                        // [UAS-d] CLIENT-SIDE VALIDATION + REACTIVE UI (pesan error hilang saat mengetik)
                        var valid = true
                        if (nama.isBlank()) { namaError = "Nama barang wajib diisi!"; valid = false }
                        if (kategori.isBlank()) { kategoriError = "Kategori wajib diisi!"; valid = false }
                        // [UAS-d] DEFENSIVE: toIntOrNull() (bukan toInt()) agar input bukan angka tidak membuat crash
                        val jumlahAngka = jumlah.trim().toIntOrNull()
                        if (jumlahAngka == null || jumlahAngka <= 0) {
                            jumlahError = "Jumlah harus berupa angka positif!"
                            valid = false
                        }
                        if (lokasi.isBlank()) { lokasiError = "Lokasi wajib diisi!"; valid = false }
                        if (!valid) return@Button
                        if (sedangSimpan) return@Button

                        // 2) Salin nilai ke variabel lokal sebelum pindah thread
                        val data = InventarisEntity(
                            id = if (modeEdit) idEdit else 0,
                            nama = nama.trim(),
                            kategori = kategori.trim(),
                            jumlah = jumlahAngka ?: 0,
                            lokasi = lokasi.trim(),
                            kondisi = kondisi,
                            fotoPath = fotoPath,
                            tanggalDibuat = if (modeEdit) tanggalAsli.ifBlank { Fmt.waktuSekarang() }
                            else Fmt.waktuSekarang()
                        )
                        val fotoLama = fotoAsli
                        val fotoBaru = fotoPath

                        // 3) CREATE / UPDATE di background thread
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
                                    if (modeEdit) "Barang diperbarui" else "Barang tersimpan",
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
                    Text(if (modeEdit) "Perbarui Barang" else "Simpan Barang")
                }
                OutlinedButton(onClick = { batalkan() }) { Text("Batal") }
            }
        }
    }
}