package com.utb.wms.ui.inventory

import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.utb.wms.R
import com.utb.wms.domain.model.Item
import com.utb.wms.domain.model.Location
import com.utb.wms.ui.common.LabeledDropdown
import com.utb.wms.ui.common.appContainer

@Composable
fun StockAdjustmentRoute(onBack: () -> Unit) {
    val container = appContainer()
    val viewModel: StockAdjustmentViewModel = viewModel {
        StockAdjustmentViewModel(
            masterDataRepository = container.masterDataRepository,
            inventoryRepository = container.inventoryRepository,
            authRepository = container.authRepository,
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    StockAdjustmentScreen(
        state = state,
        onBack = onBack,
        onPilihItem = viewModel::pilihItem,
        onPilihLokasi = viewModel::pilihLokasi,
        onUbahJumlah = viewModel::ubahJumlah,
        onUbahAlasan = viewModel::ubahAlasan,
        onSimpan = viewModel::simpan,
        onPesanDibaca = viewModel::pesanDibaca,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockAdjustmentScreen(
    state: StockAdjustmentState,
    onBack: () -> Unit,
    onPilihItem: (Item) -> Unit,
    onPilihLokasi: (Location) -> Unit,
    onUbahJumlah: (String) -> Unit,
    onUbahAlasan: (String) -> Unit,
    onSimpan: () -> Unit,
    onPesanDibaca: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val pesan = state.pesan
    val teks = if (pesan != null) teksPesan(pesan) else null

    LaunchedEffect(teks) {
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
                title = { Text(stringResource(R.string.judul_penyesuaian_stok)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.umum_kembali),
                        )
                    }
                },
            )
        },
        bottomBar = {
            if (state.berwenang) {
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
                            Text(stringResource(R.string.sesuai_simpan))
                        }
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
            if (!state.berwenang) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.sesuai_tidak_berwenang),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                    )
                }
                return@Column
            }

            LabeledDropdown(
                label = stringResource(R.string.sesuai_barang),
                options = state.items,
                selected = state.item,
                optionLabel = { "${it.nama} (${it.sku})" },
                onSelect = onPilihItem,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            LabeledDropdown(
                label = stringResource(R.string.sesuai_lokasi),
                options = state.locations,
                selected = state.location,
                optionLabel = { "${it.nama} (${it.kode})" },
                onSelect = onPilihLokasi,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            val stokSaatIni = state.stokSaatIni
            Text(
                text = if (stokSaatIni == null) {
                    stringResource(R.string.sesuai_stok_belum_dipilih)
                } else {
                    stringResource(
                        R.string.sesuai_stok_saat_ini,
                        stokSaatIni,
                        state.item?.satuan.orEmpty(),
                    )
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.jumlahBaru,
                onValueChange = onUbahJumlah,
                label = { Text(stringResource(R.string.sesuai_jumlah_baru)) },
                suffix = { Text(state.item?.satuan.orEmpty()) },
                singleLine = true,
                supportingText = {
                    val selisih = state.selisih
                    if (selisih != null && selisih != 0) {
                        Text(stringResource(R.string.sesuai_selisih, bertanda(selisih)))
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.alasan,
                onValueChange = onUbahAlasan,
                label = { Text(stringResource(R.string.sesuai_alasan)) },
                minLines = 2,
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun teksPesan(pesan: PesanSesuai): String = when (pesan) {
    is PesanSesuai.Berhasil -> stringResource(R.string.sesuai_berhasil, pesan.sku, pesan.jumlah)
    PesanSesuai.TidakBerwenang -> stringResource(R.string.sesuai_tidak_berwenang)
    PesanSesuai.SesiBerakhir -> stringResource(R.string.sesuai_sesi_berakhir)
    PesanSesuai.Gagal -> stringResource(R.string.sesuai_gagal)
}

private fun bertanda(selisih: Int): String = if (selisih > 0) "+$selisih" else "$selisih"
