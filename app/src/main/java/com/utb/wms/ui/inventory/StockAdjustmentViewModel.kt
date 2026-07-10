package com.utb.wms.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utb.wms.domain.model.Item
import com.utb.wms.domain.model.Location
import com.utb.wms.domain.model.bolehMenyesuaikanStok
import com.utb.wms.domain.repository.AuthRepository
import com.utb.wms.domain.repository.InventoryRepository
import com.utb.wms.domain.repository.MasterDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface PesanSesuai {

    data class Berhasil(val sku: String, val jumlah: Int) : PesanSesuai

    data object TidakBerwenang : PesanSesuai

    data object SesiBerakhir : PesanSesuai

    data object Gagal : PesanSesuai
}

data class StockAdjustmentState(
    val items: List<Item> = emptyList(),
    val locations: List<Location> = emptyList(),
    val item: Item? = null,
    val location: Location? = null,
    val stokSaatIni: Int? = null,
    val jumlahBaru: String = "",
    val alasan: String = "",
    val berwenang: Boolean = true,
    val sedangMenyimpan: Boolean = false,
    val pesan: PesanSesuai? = null,
) {
    val jumlahAngka: Int?
        get() = jumlahBaru.toIntOrNull()

    val selisih: Int?
        get() {
            val sekarang = stokSaatIni ?: return null
            val baru = jumlahAngka ?: return null
            return baru - sekarang
        }

    val dapatDisimpan: Boolean
        get() = berwenang &&
            item != null &&
            location != null &&
            alasan.isNotBlank() &&
            selisih != null &&
            selisih != 0 &&
            !sedangMenyimpan
}

class StockAdjustmentViewModel(
    masterDataRepository: MasterDataRepository,
    private val inventoryRepository: InventoryRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(StockAdjustmentState())

    val state: StateFlow<StockAdjustmentState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            masterDataRepository.observeItems().collect { daftar ->
                _state.update { it.copy(items = daftar) }
            }
        }
        viewModelScope.launch {
            masterDataRepository.observeLocations().collect { daftar ->
                _state.update { it.copy(locations = daftar) }
            }
        }
        viewModelScope.launch {
            authRepository.currentUser.collect { pengguna ->
                _state.update { it.copy(berwenang = pengguna?.bolehMenyesuaikanStok == true) }
            }
        }
    }

    fun pilihItem(item: Item) {
        _state.update { it.copy(item = item, stokSaatIni = null) }
        muatStok()
    }

    fun pilihLokasi(location: Location) {
        _state.update { it.copy(location = location, stokSaatIni = null) }
        muatStok()
    }

    fun ubahJumlah(nilai: String) {
        _state.update { it.copy(jumlahBaru = nilai.filter(Char::isDigit).take(6)) }
    }

    fun ubahAlasan(nilai: String) {
        _state.update { it.copy(alasan = nilai) }
    }

    fun pesanDibaca() {
        _state.update { it.copy(pesan = null) }
    }

    fun simpan() {
        val keadaan = _state.value
        if (!keadaan.dapatDisimpan) return

        val item = keadaan.item ?: return
        val location = keadaan.location ?: return
        val jumlah = keadaan.jumlahAngka ?: return

        val operator = authRepository.currentUser.value
        if (operator == null) {
            _state.update { it.copy(pesan = PesanSesuai.SesiBerakhir) }
            return
        }
        if (!operator.bolehMenyesuaikanStok) {
            _state.update { it.copy(pesan = PesanSesuai.TidakBerwenang) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(sedangMenyimpan = true, pesan = null) }

            val hasil = runCatching {
                inventoryRepository.adjustStock(
                    item = item,
                    location = location,
                    jumlahBaru = jumlah,
                    alasan = keadaan.alasan.trim(),
                    operator = operator,
                )
            }

            hasil.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            stokSaatIni = jumlah,
                            jumlahBaru = "",
                            alasan = "",
                            sedangMenyimpan = false,
                            pesan = PesanSesuai.Berhasil(item.sku, jumlah),
                        )
                    }
                },
                onFailure = {
                    _state.update {
                        it.copy(sedangMenyimpan = false, pesan = PesanSesuai.Gagal)
                    }
                },
            )
        }
    }

    private fun muatStok() {
        val keadaan = _state.value
        val item = keadaan.item ?: return
        val location = keadaan.location ?: return

        viewModelScope.launch {
            val tersedia = inventoryRepository.stockAt(item.sku, location.kode)
            _state.update { it.copy(stokSaatIni = tersedia) }
        }
    }
}
