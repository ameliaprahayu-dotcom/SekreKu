package com.example.sekreku.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val DAFTAR_KONDISI = listOf("Baik", "Rusak Ringan", "Rusak Berat")

/** [UAS-d] CLIENT-SIDE VALIDATION: helper format & validasi tanggal (dd/MM/yyyy) dan jam (HH:mm). */
object Fmt {
    private val regexTanggal = Regex("^\\d{2}/\\d{2}/\\d{4}$")
    private val regexJam = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")

    fun hariIni(): String = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date())

    fun waktuSekarang(): String =
        SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date())

    fun parseTanggal(teks: String): Date? {
        if (!regexTanggal.matches(teks)) return null
        return try {
            SimpleDateFormat("dd/MM/yyyy", Locale.US).apply { isLenient = false }.parse(teks)
        } catch (e: Exception) {
            null
        }
    }

    fun tanggalValid(teks: String): Boolean = parseTanggal(teks.trim()) != null

    /** Format jam harus HH:mm (00:00 - 23:59). */
    fun jamValid(teks: String): Boolean = regexJam.matches(teks.trim())

    /** Kunci pengurutan kronologis dari tanggal (dd/MM/yyyy) + jam (HH:mm). */
    fun kunciUrut(tanggal: String, jam: String = "00:00"): Long {
        val dasar = parseTanggal(tanggal)?.time ?: 0L
        val bagian = jam.split(":")
        val jamAngka = bagian.getOrNull(0)?.toIntOrNull() ?: 0
        val menitAngka = bagian.getOrNull(1)?.toIntOrNull() ?: 0
        return dasar + (jamAngka * 60L + menitAngka) * 60_000L
    }
}