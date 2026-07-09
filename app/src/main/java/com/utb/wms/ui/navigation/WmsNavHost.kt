package com.utb.wms.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.utb.wms.ui.common.ComingSoonScreen

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val INVENTORY = "inventory"
    const val INBOUND = "inbound"
    const val OUTBOUND = "outbound"
}

@Composable
fun WmsNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
    ) {
        composable(Routes.LOGIN) {
            ComingSoonScreen(
                judul = "Login",
                penanggungJawab = "FE-1",
                aksi = listOf(
                    "Lanjut ke Dashboard" to {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                ),
            )
        }

        composable(Routes.DASHBOARD) {
            ComingSoonScreen(
                judul = "Dashboard",
                penanggungJawab = "FE-1",
                aksi = listOf(
                    "Penerimaan Barang" to { navController.navigate(Routes.INBOUND) },
                    "Pengeluaran Barang" to { navController.navigate(Routes.OUTBOUND) },
                    "Stok Gudang" to { navController.navigate(Routes.INVENTORY) },
                ),
            )
        }

        composable(Routes.INVENTORY) {
            ComingSoonScreen(
                judul = "Stok Gudang",
                penanggungJawab = "FE-1",
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.INBOUND) {
            ComingSoonScreen(
                judul = "Penerimaan Barang",
                penanggungJawab = "FE-2",
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.OUTBOUND) {
            ComingSoonScreen(
                judul = "Pengeluaran Barang",
                penanggungJawab = "FE-2",
                onBack = { navController.popBackStack() },
            )
        }
    }
}
