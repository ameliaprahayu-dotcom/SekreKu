package com.example.sekreku.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.sekreku.ui.agenda.AgendaFormScreen
import com.example.sekreku.ui.agenda.AgendaListScreen
import com.example.sekreku.ui.dashboard.DashboardScreen
import com.example.sekreku.ui.inventaris.InventarisFormScreen
import com.example.sekreku.ui.inventaris.InventarisListScreen
import com.example.sekreku.ui.notulensi.NotulensiFormScreen
import com.example.sekreku.ui.notulensi.NotulensiListScreen
import com.example.sekreku.ui.theme.SekreKuTheme

@Composable
private fun PreviewWrapper(gelap: Boolean = false, content: @Composable () -> Unit) {
    SekreKuTheme(darkTheme = gelap) {
        Surface(modifier = Modifier.fillMaxSize()) { content() }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "1. Dashboard (Terang)")
@Composable
fun PreviewDashboardTerang() {
    PreviewWrapper { DashboardScreen(rememberNavController(), false) {} }
}

@Preview(showBackground = true, showSystemUi = true, name = "2. Dashboard (Gelap)")
@Composable
fun PreviewDashboardGelap() {
    PreviewWrapper(gelap = true) { DashboardScreen(rememberNavController(), true) {} }
}

@Preview(showBackground = true, showSystemUi = true, name = "3. Inventaris - Daftar")
@Composable
fun PreviewInventarisList() {
    PreviewWrapper { InventarisListScreen(rememberNavController()) }
}

@Preview(showBackground = true, showSystemUi = true, name = "4. Inventaris - Form")
@Composable
fun PreviewInventarisForm() {
    PreviewWrapper { InventarisFormScreen(rememberNavController(), -1) }
}

@Preview(showBackground = true, showSystemUi = true, name = "5. Notulensi - Daftar")
@Composable
fun PreviewNotulensiList() {
    PreviewWrapper { NotulensiListScreen(rememberNavController()) }
}

@Preview(showBackground = true, showSystemUi = true, name = "6. Notulensi - Form")
@Composable
fun PreviewNotulensiForm() {
    PreviewWrapper { NotulensiFormScreen(rememberNavController(), -1) }
}

@Preview(showBackground = true, showSystemUi = true, name = "7. Agenda - Daftar")
@Composable
fun PreviewAgendaList() {
    PreviewWrapper { AgendaListScreen(rememberNavController()) }
}

@Preview(showBackground = true, showSystemUi = true, name = "8. Agenda - Form (Gelap)")
@Composable
fun PreviewAgendaForm() {
    PreviewWrapper(gelap = true) { AgendaFormScreen(rememberNavController(), -1) }
}