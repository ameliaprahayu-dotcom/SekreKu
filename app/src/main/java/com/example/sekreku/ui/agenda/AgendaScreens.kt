package com.example.sekreku.ui.agenda

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.sekreku.data.AgendaEntity
import com.example.sekreku.DetailActivity
import com.example.sekreku.data.AppDatabase
import com.example.sekreku.navigation.Rute
import com.example.sekreku.ui.components.*
import com.example.sekreku.util.Fmt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val FILTER_AGENDA = listOf("Semua", "Belum Selesai", "Selesai")

// ======================= DAFTAR =======================

@Composable
fun AgendaListScreen(navController: NavHostController) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).agendaDao() }
    val scope = rememberCoroutineScope()

    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("Semua") }
    var idHapus by rememberSaveable { mutableStateOf<Int?>(null) }

    val daftar = remember { mutableStateListOf<AgendaEntity>() }

    // [UAS-e] CRUD - READ: ambil semua data dari Room (Dispatchers.IO)
    suspend fun muatUlang() {
        val data = withContext(Dispatchers.IO) { dao.getAll() }
        daftar.clear()
        daftar.addAll(data)
    }

    LaunchedEffect(Unit) { muatUlang() }

    // [UAS-d] REACTIVE UI: daftar langsung tersaring saat mengetik / memilih filter
    // Belum selesai di atas, lalu urut kronologis (tanggal + jam)
    val tampil = daftar
        .filter { a ->
            (filter == "Semua" ||
                    (filter == "Belum Selesai" && !a.selesai) ||
                    (filter == "Selesai" && a.selesai)) &&
                    (query.isBlank() ||
                            a.judul.contains(query, ignoreCase = true) ||
                            a.lokasi.contains(query, ignoreCase = true))
        }
        .sortedWith(compareBy({ it.selesai }, { Fmt.kunciUrut(it.tanggal, it.jam) }))

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
                            withContext(Dispatchers.IO) { dao.delete(target) }
                            muatUlang()
                            Toast.makeText(context, "Agenda dihapus", Toast.LENGTH_SHORT).show()
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
            SekreTopBar("Agenda dan Jadwal", onBack = { navController.popBackStack() })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Rute.agendaForm(-1)) }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah agenda")
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
            KolomCari(query, { query = it }, "Cari judul / lokasi")
            BarisFilter(FILTER_AGENDA, filter) { filter = it }
            Text(
                text = "Daftar Agenda (${tampil.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            if (tampil.isEmpty()) {
                KeadaanKosong(
                    if (daftar.isEmpty()) "Belum ada agenda.\nKetuk tombol + untuk menambah."
                    else "Tidak ada agenda yang cocok."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(tampil, key = { it.id }) { agenda ->
                        AgendaCard(
                            agenda = agenda,
                            onToggleSelesai = { centang ->
                                scope.launch {
                                    // [UAS-e] CRUD - UPDATE status selesai (centang langsung tersimpan)
                                    try {
                                        withContext(Dispatchers.IO) { dao.setSelesai(agenda.id, centang) }
                                        muatUlang()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Gagal mengubah status", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onDetail = {
                                // [UAS-c] EXPLICIT INTENT -> DetailActivity, kirim data lewat putExtra
                                val intent = Intent(context, DetailActivity::class.java)
                                intent.putExtra(DetailActivity.EXTRA_JENIS, "Agenda")
                                intent.putExtra(DetailActivity.EXTRA_JUDUL, agenda.judul)
                                intent.putStringArrayListExtra(DetailActivity.EXTRA_LABEL, arrayListOf("Tanggal", "Jam", "Lokasi", "Status"))
                                intent.putStringArrayListExtra(DetailActivity.EXTRA_ISI, arrayListOf(agenda.tanggal, agenda.jam, agenda.lokasi, if (agenda.selesai) "Selesai" else "Belum selesai"))
                                context.startActivity(intent)
                            },
                            onEdit = { navController.navigate(Rute.agendaForm(agenda.id)) },
                            onDelete = { idHapus = agenda.id }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AgendaCard(
    agenda: AgendaEntity,
    onToggleSelesai: (Boolean) -> Unit,
    onDetail: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val wadah = if (agenda.selesai) MaterialTheme.colorScheme.surfaceVariant
    else MaterialTheme.colorScheme.primaryContainer
    val warna = if (agenda.selesai) MaterialTheme.colorScheme.onSurfaceVariant
    else MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = wadah)
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = agenda.selesai, onCheckedChange = onToggleSelesai)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = agenda.judul,
                        fontWeight = FontWeight.Bold,
                        color = warna,
                        textDecoration = if (agenda.selesai) TextDecoration.LineThrough else null
                    )
                    Text(text = "${agenda.tanggal} • ${agenda.jam}", fontSize = 13.sp, color = warna)
                    Text(text = agenda.lokasi, fontSize = 13.sp, color = warna)
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
fun AgendaFormScreen(navController: NavHostController, idEdit: Int) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).agendaDao() }
    val scope = rememberCoroutineScope()
    val modeEdit = idEdit != -1

    // [UAS-d] ROTASI LAYAR: rememberSaveable -> isi form tidak hilang saat layar diputar
    var judul by rememberSaveable { mutableStateOf("") }
    var tanggal by rememberSaveable { mutableStateOf(Fmt.hariIni()) }
    var jam by rememberSaveable { mutableStateOf("") }
    var lokasi by rememberSaveable { mutableStateOf("") }
    var selesai by rememberSaveable { mutableStateOf(false) }
    var sudahMuat by rememberSaveable { mutableStateOf(!modeEdit) }
    // [UAS-d] DEFENSIVE: cegah tombol Simpan ditekan berulang (data ganda)
    var sedangSimpan by remember { mutableStateOf(false) }

    var judulError by rememberSaveable { mutableStateOf<String?>(null) }
    var tanggalError by rememberSaveable { mutableStateOf<String?>(null) }
    var jamError by rememberSaveable { mutableStateOf<String?>(null) }
    var lokasiError by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(idEdit) {
        if (modeEdit && !sudahMuat) {
            val data = withContext(Dispatchers.IO) { dao.getById(idEdit) }
            // [UAS-d] DEFENSIVE: data tidak ditemukan -> kembali, tidak crash
            if (data == null) {
                navController.popBackStack()
            } else {
                judul = data.judul
                tanggal = data.tanggal
                jam = data.jam
                lokasi = data.lokasi
                selesai = data.selesai
                sudahMuat = true
            }
        }
    }

    Scaffold(
        topBar = {
            SekreTopBar(
                judul = if (modeEdit) "Edit Agenda" else "Tambah Agenda",
                onBack = { navController.popBackStack() }
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
            FormField(judul, { judul = it; judulError = null }, "Judul Agenda", judulError)
            FormField(
                tanggal, { tanggal = it; tanggalError = null }, "Tanggal (dd/MM/yyyy)", tanggalError
            )
            FormField(
                jam, { jam = it; jamError = null }, "Jam (HH:mm, mis. 09:30)", jamError
            )
            FormField(lokasi, { lokasi = it; lokasiError = null }, "Lokasi", lokasiError)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Sudah selesai")
                Switch(checked = selesai, onCheckedChange = { selesai = it })
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        // [UAS-d] CLIENT-SIDE VALIDATION + REACTIVE UI (pesan error hilang saat mengetik)
                        var valid = true
                        if (judul.isBlank()) { judulError = "Judul agenda wajib diisi!"; valid = false }
                        if (!Fmt.tanggalValid(tanggal)) {
                            tanggalError = "Tanggal harus berformat dd/MM/yyyy dan valid!"
                            valid = false
                        }
                        if (!Fmt.jamValid(jam)) {
                            jamError = "Jam harus berformat HH:mm (00:00 - 23:59)!"
                            valid = false
                        }
                        if (lokasi.isBlank()) { lokasiError = "Lokasi wajib diisi!"; valid = false }
                        if (!valid) return@Button
                        if (sedangSimpan) return@Button

                        val data = AgendaEntity(
                            id = if (modeEdit) idEdit else 0,
                            judul = judul.trim(),
                            tanggal = tanggal.trim(),
                            jam = jam.trim(),
                            lokasi = lokasi.trim(),
                            selesai = selesai
                        )

                        sedangSimpan = true
                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    // [UAS-e] CRUD - UPDATE (mode edit) atau CREATE (mode tambah) ke Room
                                    if (modeEdit) dao.update(data) else dao.insert(data)
                                }
                                Toast.makeText(
                                    context,
                                    if (modeEdit) "Agenda diperbarui" else "Agenda tersimpan",
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
                    Text(if (modeEdit) "Perbarui Agenda" else "Simpan Agenda")
                }
                OutlinedButton(onClick = { navController.popBackStack() }) { Text("Batal") }
            }
        }
    }
}