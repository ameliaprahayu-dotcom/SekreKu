package com.example.sekreku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.sekreku.navigation.AppNavigation
import com.example.sekreku.ui.theme.SekreKuTheme

/*
 * ===================== PETA POIN "NOTED PROJECT UAS" =====================
 * Cari tag berikut dengan Ctrl+Shift+F (Find in Files) untuk melihat lokasinya:
 *  [UAS-a] UI rapi: Column, Row, Box + Text, OutlinedTextField, Button, Image
 *  [UAS-b] Image dari folder drawable (logo_sekreku.xml di Dashboard)
 *  [UAS-c] Minimal 2 layar dengan Explicit Intent + kirim data (DetailActivity)
 *  [UAS-d] Validasi sisi klien, defensive programming, rotasi layar, reactive UI
 *  [UAS-e] CRUD (Create, Read, Update, Delete)
 *  [UAS-f] Data tersimpan di database lokal (Room)
 * =========================================================================
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Awal mengikuti pengaturan sistem; bisa diubah lewat saklar di Dashboard.
            // [UAS-d] ROTASI LAYAR: rememberSaveable agar pilihan tema tidak hilang saat layar diputar.
            val sistemGelap = isSystemInDarkTheme()
            var modeGelap by rememberSaveable { mutableStateOf(sistemGelap) }

            SekreKuTheme(darkTheme = modeGelap) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        modeGelap = modeGelap,
                        onModeGelapChange = { modeGelap = it }
                    )
                }
            }
        }
    }
}