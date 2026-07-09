package com.utb.wms.ui.outbound

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.utb.wms.domain.model.Item
import com.utb.wms.domain.model.Location
import com.utb.wms.ui.common.LabeledDropdown
import com.utb.wms.ui.common.appContainer
import com.utb.wms.ui.theme.WMSMobileTheme

@Composable
fun GoodsIssueRoute(onBack: () -> Unit) {
    val container = appContainer()
    val viewModel: GoodsIssueViewModel = viewModel {
        GoodsIssueViewModel(
            masterDataRepository = container.masterDataRepository,
            inventoryRepository = container.inventoryRepository,
            outboundRepository = container.outboundRepository,
            authRepository = container.authRepository,
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    GoodsIssueScreen(
        state = state,
        onBack = onBack,
        onUbahTujuan = viewModel::ubahTujuan,
        onPilihItem = viewModel::pilihItem,
        onPilihLokasi = viewModel::pilihLokasi,
        onUbahQty = viewModel::ubahQty,
        onTambahBaris = viewModel::tambahBaris,
        onHapusBaris = viewModel::hapusBaris,
        onSimpan = viewModel::simpan,
        onPesanDibaca = viewModel::pesanDibaca,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoodsIssueScreen(
    state: GoodsIssueState,
    onBack: () -> Unit,
    onUbahTujuan: (String) -> Unit,
    onPilihItem: (Long, Item) -> Unit,
    onPilihLokasi: (Long, Location) -> Unit,
    onUbahQty: (Long, String) -> Unit,
    onTambahBaris: () -> Unit,
    onHapusBaris: (Long) -> Unit,
    onSimpan: () -> Unit,
    onPesanDibaca: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.pesan, state.galat) {
        val teks = state.pesan ?: state.galat
        if (teks != null) {
            snackbarHostState.showSnackbar(teks)
            onPesanDibaca()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Pengeluaran Barang") },
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
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = onSimpan,
                    enabled = state.dapatDisimpan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                ) {
                    if (state.sedangMenyimpan) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text("Simpan Pengeluaran")
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = state.tujuan,
                onValueChange = onUbahTujuan,
                label = { Text("Tujuan") },
                placeholder = { Text("Contoh: Produksi Line A") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Detail Barang",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onTambahBaris) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.size(4.dp))
                    Text("Tambah Baris")
                }
            }

            Spacer(Modifier.height(8.dp))

            state.baris.forEachIndexed { indeks, baris ->
                KartuBarisPengeluaran(
                    nomor = indeks + 1,
                    baris = baris,
                    items = state.items,
                    locations = state.locations,
                    dapatDihapus = state.baris.size > 1,
                    onPilihItem = { onPilihItem(baris.id, it) },
                    onPilihLokasi = { onPilihLokasi(baris.id, it) },
                    onUbahQty = { onUbahQty(baris.id, it) },
                    onHapus = { onHapusBaris(baris.id) },
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun KartuBarisPengeluaran(
    nomor: Int,
    baris: BarisPengeluaran,
    items: List<Item>,
    locations: List<Location>,
    dapatDihapus: Boolean,
    onPilihItem: (Item) -> Unit,
    onPilihLokasi: (Location) -> Unit,
    onUbahQty: (String) -> Unit,
    onHapus: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Baris $nomor",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                if (dapatDihapus) {
                    IconButton(onClick = onHapus) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Hapus baris $nomor",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            LabeledDropdown(
                label = "Barang",
                options = items,
                selected = baris.item,
                optionLabel = { "${it.nama} (${it.sku})" },
                onSelect = onPilihItem,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))

            LabeledDropdown(
                label = "Lokasi",
                options = locations,
                selected = baris.location,
                optionLabel = { "${it.nama} (${it.kode})" },
                onSelect = onPilihLokasi,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))

            val tersedia = baris.tersedia
            OutlinedTextField(
                value = baris.qty,
                onValueChange = onUbahQty,
                label = { Text("Kuantitas") },
                suffix = { Text(baris.item?.satuan.orEmpty()) },
                isError = baris.melebihiStok,
                singleLine = true,
                supportingText = {
                    when {
                        baris.melebihiStok -> Text("Melebihi stok tersedia")
                        tersedia != null -> Text("Tersedia $tersedia ${baris.item?.satuan.orEmpty()}")
                        else -> Text("Pilih barang dan lokasi untuk melihat sisa stok")
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GoodsIssueScreenPreview() {
    val bolt = Item(sku = "BOLT-M8-30", nama = "Bolt M8x30mm", satuan = "pcs", stokMinimum = 50)
    val rakA = Location(kode = "A-01", nama = "Rak A-01", kapasitas = 500)

    WMSMobileTheme {
        GoodsIssueScreen(
            state = GoodsIssueState(
                items = listOf(bolt),
                locations = listOf(rakA),
                tujuan = "Produksi Line A",
                baris = listOf(
                    BarisPengeluaran(id = 1, item = bolt, location = rakA, qty = "30", tersedia = 120),
                ),
            ),
            onBack = {},
            onUbahTujuan = {},
            onPilihItem = { _, _ -> },
            onPilihLokasi = { _, _ -> },
            onUbahQty = { _, _ -> },
            onTambahBaris = {},
            onHapusBaris = {},
            onSimpan = {},
            onPesanDibaca = {},
        )
    }
}

@Preview(showBackground = true, name = "Stok kurang")
@Composable
private fun GoodsIssueScreenStokKurangPreview() {
    val plat = Item(sku = "PLT-STL-2", nama = "Plat Baja 2mm", satuan = "lembar", stokMinimum = 10)
    val rakB = Location(kode = "B-01", nama = "Rak B-01", kapasitas = 300)

    WMSMobileTheme {
        GoodsIssueScreen(
            state = GoodsIssueState(
                items = listOf(plat),
                locations = listOf(rakB),
                tujuan = "Produksi Line B",
                baris = listOf(
                    BarisPengeluaran(id = 1, item = plat, location = rakB, qty = "50", tersedia = 15),
                ),
            ),
            onBack = {},
            onUbahTujuan = {},
            onPilihItem = { _, _ -> },
            onPilihLokasi = { _, _ -> },
            onUbahQty = { _, _ -> },
            onTambahBaris = {},
            onHapusBaris = {},
            onSimpan = {},
            onPesanDibaca = {},
        )
    }
}
