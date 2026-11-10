package com.example.sekreku.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.sekreku.R
import com.example.sekreku.data.AppDatabase
import com.example.sekreku.navigation.Rute
import com.example.sekreku.ui.components.SekreTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DashboardScreen(
    navController: NavHostController,
    modeGelap: Boolean,
    onModeGelapChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }

    var jmlInventaris by remember { mutableIntStateOf(0) }
    var jmlNotulensi by remember { mutableIntStateOf(0) }
    var jmlAgenda by remember { mutableIntStateOf(0) }
    var jmlAgendaBelum by remember { mutableIntStateOf(0) }

    // [UAS-d] REACTIVE UI: angka ringkasan diperbarui otomatis tiap Dashboard tampil
    // Dibaca ulang setiap Dashboard tampil (termasuk saat kembali dari modul lain)
    LaunchedEffect(Unit) {
        jmlInventaris = withContext(Dispatchers.IO) { db.inventarisDao().hitung() }
        jmlNotulensi = withContext(Dispatchers.IO) { db.notulensiDao().hitung() }
        jmlAgenda = withContext(Dispatchers.IO) { db.agendaDao().hitung() }
        jmlAgendaBelum = withContext(Dispatchers.IO) { db.agendaDao().hitungBelumSelesai() }
    }

    Scaffold(
        topBar = {
            SekreTopBar(
                judul = "SekreKu",
                actions = {
                    Text(
                        text = if (modeGelap) "Gelap" else "Terang",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(checked = modeGelap, onCheckedChange = onModeGelapChange)
                    Spacer(modifier = Modifier.width(12.dp))
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // [UAS-a] UI disusun dengan Column, Row, Box + komponen dasar (Text, Button, Image)
            // [UAS-b] IMAGE dari folder res/drawable (file: res/drawable/logo_sekreku.xml)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_sekreku),
                    contentDescription = "Logo SekreKu",
                    modifier = Modifier.size(96.dp)
                )
            }


            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RingkasanCard("Barang Inventaris", jmlInventaris, Modifier.weight(1f))
                RingkasanCard("Notulensi Rapat", jmlNotulensi, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RingkasanCard("Total Agenda", jmlAgenda, Modifier.weight(1f))
                RingkasanCard("Agenda Belum Selesai", jmlAgendaBelum, Modifier.weight(1f), sorot = true)
            }

            HorizontalDivider()
            Text("Menu", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

            MenuCard(
                judul = "Inventaris Kantor",
                deskripsi = "Nama, kategori, jumlah, lokasi, kondisi, dan foto barang",
                onClick = { navController.navigate(Rute.INVENTARIS) }
            )
            MenuCard(
                judul = "Notulensi Rapat",
                deskripsi = "Judul, tanggal, peserta, pembahasan, tindak lanjut, dan foto lampiran",
                onClick = { navController.navigate(Rute.NOTULENSI) }
            )
            MenuCard(
                judul = "Agenda dan Jadwal",
                deskripsi = "Judul, tanggal, jam, lokasi, dan status selesai",
                onClick = { navController.navigate(Rute.AGENDA) }
            )

            Text(
                text = "Catatan: data hanya ada di HP ini. Jika aplikasi di-uninstall atau HP hilang, data ikut hilang.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RingkasanCard(label: String, nilai: Int, modifier: Modifier, sorot: Boolean = false) {
    val wadah = if (sorot) MaterialTheme.colorScheme.tertiaryContainer
    else MaterialTheme.colorScheme.secondaryContainer
    val isi = if (sorot) MaterialTheme.colorScheme.onTertiaryContainer
    else MaterialTheme.colorScheme.onSecondaryContainer

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = wadah)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = nilai.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = isi)
            Text(text = label, fontSize = 13.sp, color = isi)
        }
    }
}

@Composable
private fun MenuCard(judul: String, deskripsi: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = judul,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(text = deskripsi, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}