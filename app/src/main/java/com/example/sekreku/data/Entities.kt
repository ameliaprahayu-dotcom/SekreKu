package com.example.sekreku.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// [UAS-f] DATABASE LOKAL: tiga tabel Room (inventaris, notulensi, agenda) di dalam HP
@Entity(tableName = "inventaris")
data class InventarisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "nama") val nama: String,
    @ColumnInfo(name = "kategori") val kategori: String,
    @ColumnInfo(name = "jumlah") val jumlah: Int,
    @ColumnInfo(name = "lokasi") val lokasi: String,
    @ColumnInfo(name = "kondisi") val kondisi: String,
    // Path file foto di penyimpanan INTERNAL aplikasi (null = tanpa foto)
    @ColumnInfo(name = "foto_path") val fotoPath: String? = null,
    @ColumnInfo(name = "tanggal_dibuat") val tanggalDibuat: String
)

@Entity(tableName = "notulensi")
data class NotulensiEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "judul") val judul: String,
    @ColumnInfo(name = "tanggal") val tanggal: String,
    @ColumnInfo(name = "peserta") val peserta: String,
    @ColumnInfo(name = "pembahasan") val pembahasan: String,
    @ColumnInfo(name = "tindak_lanjut") val tindakLanjut: String,
    @ColumnInfo(name = "foto_path") val fotoPath: String? = null
)

@Entity(tableName = "agenda")
data class AgendaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "judul") val judul: String,
    @ColumnInfo(name = "tanggal") val tanggal: String,
    @ColumnInfo(name = "jam") val jam: String,
    @ColumnInfo(name = "lokasi") val lokasi: String,
    @ColumnInfo(name = "selesai") val selesai: Boolean = false
)