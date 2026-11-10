package com.example.sekreku.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sekreku.ui.agenda.AgendaFormScreen
import com.example.sekreku.ui.agenda.AgendaListScreen
import com.example.sekreku.ui.dashboard.DashboardScreen
import com.example.sekreku.ui.inventaris.InventarisFormScreen
import com.example.sekreku.ui.inventaris.InventarisListScreen
import com.example.sekreku.ui.notulensi.NotulensiFormScreen
import com.example.sekreku.ui.notulensi.NotulensiListScreen

// [UAS-c] Form tambah/edit memakai Navigation Compose; layar Detail memakai EXPLICIT INTENT (DetailActivity)
object Rute {
    const val DASHBOARD = "dashboard"
    const val INVENTARIS = "inventaris"
    const val INVENTARIS_FORM = "inventaris_form/{id}"
    const val NOTULENSI = "notulensi"
    const val NOTULENSI_FORM = "notulensi_form/{id}"
    const val AGENDA = "agenda"
    const val AGENDA_FORM = "agenda_form/{id}"

    // id = -1 berarti mode TAMBAH, selain itu mode EDIT
    fun inventarisForm(id: Int = -1) = "inventaris_form/$id"
    fun notulensiForm(id: Int = -1) = "notulensi_form/$id"
    fun agendaForm(id: Int = -1) = "agenda_form/$id"
}

@Composable
fun AppNavigation(modeGelap: Boolean, onModeGelapChange: (Boolean) -> Unit) {
    val navController = rememberNavController()
    val argId = listOf(navArgument("id") { type = NavType.IntType })

    NavHost(navController = navController, startDestination = Rute.DASHBOARD) {
        composable(Rute.DASHBOARD) {
            DashboardScreen(navController, modeGelap, onModeGelapChange)
        }

        composable(Rute.INVENTARIS) { InventarisListScreen(navController) }
        composable(Rute.INVENTARIS_FORM, arguments = argId) { entry ->
            InventarisFormScreen(navController, entry.arguments?.getInt("id") ?: -1)
        }

        composable(Rute.NOTULENSI) { NotulensiListScreen(navController) }
        composable(Rute.NOTULENSI_FORM, arguments = argId) { entry ->
            NotulensiFormScreen(navController, entry.arguments?.getInt("id") ?: -1)
        }

        composable(Rute.AGENDA) { AgendaListScreen(navController) }
        composable(Rute.AGENDA_FORM, arguments = argId) { entry ->
            AgendaFormScreen(navController, entry.arguments?.getInt("id") ?: -1)
        }
    }
}