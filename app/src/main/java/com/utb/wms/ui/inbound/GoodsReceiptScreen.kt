package com.utb.wms.ui.inbound

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.FilledTonalIconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.utb.wms.R
import com.utb.wms.domain.model.Item
import com.utb.wms.domain.model.Location
import com.utb.wms.domain.model.Supplier
import com.utb.wms.ui.common.LabeledDropdown
import com.utb.wms.ui.theme.WMSMobileTheme

@Composable
fun GoodsReceiptRoute(
    viewModel: GoodsReceiptViewModel,
    onBack: () -> Unit,
    onPindai: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    GoodsReceiptScreen(
        state = state,
        onBack = onBack,
        onPilihSupplier = viewModel::pilihSupplier,
        onPilihItem = viewModel::pilihItem,
        onPilihLokasi = viewModel::pilihLokasi,
        onUbahQty = viewModel::ubahQty,
        onTambahBaris = viewModel::tambahBaris,
        onHapusBaris = viewModel::hapusBaris,
        onSimpan = viewModel::simpan,
        onPesanDibaca = viewModel::pesanDibaca,
        onPindai = onPindai,
        onPindaiDibaca = viewModel::pindaiDibaca,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoodsReceiptScreen(
    state: GoodsReceiptState,
    onBack: () -> Unit,
    onPilihSupplier: (Supplier) -> Unit,
    onPilihItem: (Long, Item) -> Unit,
    onPilihLokasi: (Long, Location) -> Unit,
    onUbahQty: (Long, String) -> Unit,
    onTambahBaris: () -> Unit,
    onHapusBaris: (Long) -> Unit,
    onSimpan: () -> Unit,
    onPesanDibaca: () -> Unit,
    onPindai: (Long) -> Unit,
    onPindaiDibaca: () -> Unit,
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

    val pindai = state.pindai
    val teksPindai = when (pindai) {
        is HasilPindai.Terpilih -> stringResource(R.string.pindai_terpilih, pindai.nama)
        is HasilPindai.TidakDikenali -> stringResource(R.string.pindai_tidak_dikenali, pindai.kode)
        null -> null
    }

    LaunchedEffect(teksPindai) {
        if (teksPindai != null) {
            snackbarHostState.showSnackbar(teksPindai)
            onPindaiDibaca()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Penerimaan Barang") },
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
                        Text("Simpan Penerimaan")
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
            LabeledDropdown(
                label = "Pemasok",
                options = state.suppliers,
                selected = state.supplier,
                optionLabel = { it.nama },
                onSelect = onPilihSupplier,
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
                KartuBarisPenerimaan(
                    nomor = indeks + 1,
                    baris = baris,
                    items = state.items,
                    locations = state.locations,
                    dapatDihapus = state.baris.size > 1,
                    onPilihItem = { onPilihItem(baris.id, it) },
                    onPilihLokasi = { onPilihLokasi(baris.id, it) },
                    onUbahQty = { onUbahQty(baris.id, it) },
                    onHapus = { onHapusBaris(baris.id) },
                    onPindai = { onPindai(baris.id) },
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun KartuBarisPenerimaan(
    nomor: Int,
    baris: BarisPenerimaan,
    items: List<Item>,
    locations: List<Location>,
    dapatDihapus: Boolean,
    onPilihItem: (Item) -> Unit,
    onPilihLokasi: (Location) -> Unit,
    onUbahQty: (String) -> Unit,
    onHapus: () -> Unit,
    onPindai: () -> Unit,
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                LabeledDropdown(
                    label = "Barang",
                    options = items,
                    selected = baris.item,
                    optionLabel = { "${it.nama} (${it.sku})" },
                    onSelect = onPilihItem,
                    modifier = Modifier.weight(1f),
                )

                Spacer(Modifier.width(8.dp))

                FilledTonalIconButton(onClick = onPindai) {
                    Icon(
                        painter = painterResource(R.drawable.ic_qr_code_scanner),
                        contentDescription = stringResource(R.string.pindai_ikon),
                    )
                }
            }

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

            OutlinedTextField(
                value = baris.qty,
                onValueChange = onUbahQty,
                label = { Text("Kuantitas") },
                suffix = { Text(baris.item?.satuan.orEmpty()) },
                singleLine = true,
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
private fun GoodsReceiptScreenPreview() {
    val bolt = Item(sku = "BOLT-M8-30", nama = "Bolt M8x30mm", satuan = "pcs", stokMinimum = 50)
    val rakA = Location(kode = "A-01", nama = "Rak A-01", kapasitas = 500)
    val pemasok = Supplier(id = "SUP-001", nama = "PT Sumber Makmur")

    WMSMobileTheme {
        GoodsReceiptScreen(
            state = GoodsReceiptState(
                suppliers = listOf(pemasok),
                items = listOf(bolt),
                locations = listOf(rakA),
                supplier = pemasok,
                baris = listOf(BarisPenerimaan(id = 1, item = bolt, location = rakA, qty = "20")),
            ),
            onBack = {},
            onPilihSupplier = {},
            onPilihItem = { _, _ -> },
            onPilihLokasi = { _, _ -> },
            onUbahQty = { _, _ -> },
            onTambahBaris = {},
            onHapusBaris = {},
            onSimpan = {},
            onPesanDibaca = {},
            onPindai = {},
            onPindaiDibaca = {},
        )
    }
}
