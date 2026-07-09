package com.utb.wms.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.utb.wms.ui.common.ComingSoonScreen
import com.utb.wms.ui.dashboard.DashboardRoute
import com.utb.wms.ui.inventory.InventoryRoute
import com.utb.wms.ui.login.LoginRoute

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
            LoginRoute(
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardRoute(
                onOpenInbound = { navController.navigate(Routes.INBOUND) },
                onOpenOutbound = { navController.navigate(Routes.OUTBOUND) },
                onOpenInventory = { navController.navigate(Routes.INVENTORY) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.INVENTORY) {
            InventoryRoute(onBack = { navController.popBackStack() })
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
