package com.utb.wms.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.utb.wms.domain.model.Role
import com.utb.wms.domain.model.User
import com.utb.wms.ui.common.appContainer
import com.utb.wms.ui.theme.WMSMobileTheme

@Composable
fun DashboardRoute(
    onOpenInbound: () -> Unit,
    onOpenOutbound: () -> Unit,
    onOpenInventory: () -> Unit,
    onLogout: () -> Unit,
) {
    val authRepository = appContainer().authRepository
    val pengguna by authRepository.currentUser.collectAsStateWithLifecycle()

    DashboardScreen(
        pengguna = pengguna,
        onOpenInbound = onOpenInbound,
        onOpenOutbound = onOpenOutbound,
        onOpenInventory = onOpenInventory,
        onLogout = {
            authRepository.logout()
            onLogout()
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    pengguna: User?,
    onOpenInbound: () -> Unit,
    onOpenOutbound: () -> Unit,
    onOpenInventory: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Keluar",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            KartuSambutan(pengguna)

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Menu Utama",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(12.dp))

            KartuMenu(
                ikon = Icons.Filled.ArrowDownward,
                judul = "Penerimaan Barang",
                keterangan = "Catat barang masuk dari pemasok",
                onClick = onOpenInbound,
            )
            Spacer(Modifier.height(12.dp))
            KartuMenu(
                ikon = Icons.Filled.ArrowUpward,
                judul = "Pengeluaran Barang",
                keterangan = "Keluarkan barang dari gudang",
                onClick = onOpenOutbound,
            )
            Spacer(Modifier.height(12.dp))
            KartuMenu(
                ikon = Icons.AutoMirrored.Filled.List,
                judul = "Stok Gudang",
                keterangan = "Lihat saldo stok tiap lokasi",
                onClick = onOpenInventory,
            )
        }
    }
}

@Composable
private fun KartuSambutan(pengguna: User?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Selamat datang,",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = pengguna?.nama ?: "Pengguna",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = pengguna?.role?.namaRole ?: "-",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun KartuMenu(
    ikon: ImageVector,
    judul: String,
    keterangan: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = ikon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = judul,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = keterangan,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    WMSMobileTheme {
        DashboardScreen(
            pengguna = User(
                id = "U-02",
                username = "operator",
                password = "operator123",
                nama = "Budi Santoso",
                role = Role(id = "R-02", namaRole = "Operator"),
            ),
            onOpenInbound = {},
            onOpenOutbound = {},
            onOpenInventory = {},
            onLogout = {},
        )
    }
}
