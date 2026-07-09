package com.utb.wms.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.utb.wms.domain.model.Item
import com.utb.wms.domain.model.Location
import com.utb.wms.domain.model.Stock
import com.utb.wms.ui.common.appContainer
import com.utb.wms.ui.theme.WMSMobileTheme

@Composable
fun InventoryRoute(onBack: () -> Unit) {
    val container = appContainer()
    val viewModel: InventoryViewModel = viewModel {
        InventoryViewModel(container.inventoryRepository)
    }
    val stocks by viewModel.stocks.collectAsStateWithLifecycle()

    InventoryScreen(stocks = stocks, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    stocks: List<Stock>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Stok Gudang") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (stocks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Belum ada data stok",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(stocks, key = { it.id }) { stock ->
                    BarisStok(stock)
                }
            }
        }
    }
}

@Composable
private fun BarisStok(stock: Stock) {
    val stokMenipis = stock.jumlahStok < stock.item.stokMinimum

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stock.item.nama,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stock.item.sku,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stock.location.nama,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${stock.jumlahStok} ${stock.item.satuan}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (stokMenipis) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                if (stokMenipis) {
                    Spacer(Modifier.height(6.dp))
                    PenandaStokMinimum(stock.item.stokMinimum)
                }
            }
        }
    }
}

@Composable
private fun PenandaStokMinimum(stokMinimum: Int) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Min. $stokMinimum",
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InventoryScreenPreview() {
    val bolt = Item(sku = "BOLT-M8-30", nama = "Bolt M8x30mm", satuan = "pcs", stokMinimum = 50)
    val plat = Item(sku = "PLT-STL-2", nama = "Plat Baja 2mm", satuan = "lembar", stokMinimum = 10)
    val rakA = Location(kode = "A-01", nama = "Rak A-01", kapasitas = 500)
    val rakB = Location(kode = "B-01", nama = "Rak B-01", kapasitas = 300)

    WMSMobileTheme {
        InventoryScreen(
            stocks = listOf(
                Stock(id = "STK-1", item = bolt, location = rakA, jumlahStok = 120),
                Stock(id = "STK-3", item = plat, location = rakB, jumlahStok = 6),
            ),
            onBack = {},
        )
    }
}
