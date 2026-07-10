package com.utb.wms.ui.inbound

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.utb.wms.domain.model.GoodsReceiptDetail
import com.utb.wms.domain.model.Item
import com.utb.wms.domain.model.Location
import com.utb.wms.domain.model.Supplier
import com.utb.wms.domain.repository.AuthRepository
import com.utb.wms.domain.repository.InboundRepository
import com.utb.wms.domain.repository.MasterDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface HasilPindai {

    data class Terpilih(val nama: String) : HasilPindai

    data class TidakDikenali(val kode: String) : HasilPindai
}

data class BarisPenerimaan(
    val id: Long,
    val item: Item? = null,
    val location: Location? = null,
    val qty: String = "",
) {
    val qtyAngka: Int?
        get() = qty.toIntOrNull()

    val lengkap: Boolean
        get() = item != null && location != null && (qtyAngka ?: 0) > 0
}

data class GoodsReceiptState(
    val suppliers: List<Supplier> = emptyList(),
    val items: List<Item> = emptyList(),
    val locations: List<Location> = emptyList(),
    val supplier: Supplier? = null,
    val baris: List<BarisPenerimaan> = listOf(BarisPenerimaan(id = 1)),
    val sedangMenyimpan: Boolean = false,
    val pesan: String? = null,
    val galat: String? = null,
    val pindai: HasilPindai? = null,
) {
    val dapatDisimpan: Boolean
        get() = supplier != null &&
            baris.isNotEmpty() &&
            baris.all { it.lengkap } &&
            !sedangMenyimpan
}

class GoodsReceiptViewModel(
    private val masterDataRepository: MasterDataRepository,
    private val inboundRepository: InboundRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(GoodsReceiptState())

    val state: StateFlow<GoodsReceiptState> = _state.asStateFlow()

    private var idBerikutnya = 2L

    private var barisDipindai: Long? = null

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
            masterDataRepository.observeSuppliers().collect { daftar ->
                _state.update { it.copy(suppliers = daftar) }
            }
        }
    }

    fun pilihSupplier(supplier: Supplier) {
        _state.update { it.copy(supplier = supplier, galat = null) }
    }

    fun tambahBaris() {
        _state.update { it.copy(baris = it.baris + BarisPenerimaan(id = idBerikutnya++)) }
    }

    fun hapusBaris(id: Long) {
        _state.update { keadaan ->
            val tersisa = keadaan.baris.filterNot { it.id == id }
            keadaan.copy(
                baris = tersisa.ifEmpty { listOf(BarisPenerimaan(id = idBerikutnya++)) },
            )
        }
    }

    fun pilihItem(id: Long, item: Item) = ubahBaris(id) { it.copy(item = item) }

    fun pilihLokasi(id: Long, location: Location) = ubahBaris(id) { it.copy(location = location) }

    fun ubahQty(id: Long, qty: String) =
        ubahBaris(id) { it.copy(qty = qty.filter(Char::isDigit).take(6)) }

    fun pesanDibaca() {
        _state.update { it.copy(pesan = null, galat = null) }
    }

    fun pindaiDibaca() {
        _state.update { it.copy(pindai = null) }
    }

    fun mulaiPindai(id: Long) {
        barisDipindai = id
    }

    fun terapkanBarcode(kode: String?) {
        val id = barisDipindai ?: return
        barisDipindai = null
        if (kode.isNullOrBlank()) return

        viewModelScope.launch {
            val item = masterDataRepository.findItemByBarcode(kode.trim())
            if (item == null) {
                _state.update { it.copy(pindai = HasilPindai.TidakDikenali(kode.trim())) }
            } else {
                ubahBaris(id) { it.copy(item = item) }
                _state.update { it.copy(pindai = HasilPindai.Terpilih(item.nama)) }
            }
        }
    }

    fun simpan() {
        val keadaan = _state.value
        if (!keadaan.dapatDisimpan) return

        val supplier = keadaan.supplier ?: return
        val operator = authRepository.currentUser.value
        if (operator == null) {
            _state.update { it.copy(galat = "Sesi berakhir, silakan masuk kembali") }
            return
        }

        val details = keadaan.baris.mapNotNull { baris ->
            val item = baris.item ?: return@mapNotNull null
            val location = baris.location ?: return@mapNotNull null
            val qty = baris.qtyAngka ?: return@mapNotNull null
            GoodsReceiptDetail(item = item, location = location, qty = qty)
        }
        if (details.size != keadaan.baris.size) {
            _state.update { it.copy(galat = "Ada baris yang belum lengkap") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(sedangMenyimpan = true, pesan = null, galat = null) }

            val hasil = runCatching {
                inboundRepository.createGoodsReceipt(
                    supplier = supplier,
                    operator = operator,
                    details = details,
                    tanggal = System.currentTimeMillis(),
                )
            }

            hasil.fold(
                onSuccess = { receipt ->
                    _state.update {
                        it.copy(
                            supplier = null,
                            baris = listOf(BarisPenerimaan(id = idBerikutnya++)),
                            sedangMenyimpan = false,
                            pesan = "Penerimaan ${receipt.noReceipt} tersimpan",
                        )
                    }
                },
                onFailure = { penyebab ->
                    _state.update {
                        it.copy(
                            sedangMenyimpan = false,
                            galat = penyebab.message ?: "Gagal menyimpan penerimaan",
                        )
                    }
                },
            )
        }
    }

    private fun ubahBaris(id: Long, transform: (BarisPenerimaan) -> BarisPenerimaan) {
        _state.update { keadaan ->
            keadaan.copy(
                baris = keadaan.baris.map { if (it.id == id) transform(it) else it },
                galat = null,
            )
        }
    }

    companion object {
        fun factory(
            masterDataRepository: MasterDataRepository,
            inboundRepository: InboundRepository,
            authRepository: AuthRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                GoodsReceiptViewModel(masterDataRepository, inboundRepository, authRepository)
            }
        }
    }
}
