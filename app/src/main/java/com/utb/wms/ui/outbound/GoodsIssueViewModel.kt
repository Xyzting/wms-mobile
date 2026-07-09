package com.utb.wms.ui.outbound

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utb.wms.domain.model.GoodsIssueDetail
import com.utb.wms.domain.model.Item
import com.utb.wms.domain.model.Location
import com.utb.wms.domain.repository.AuthRepository
import com.utb.wms.domain.repository.InventoryRepository
import com.utb.wms.domain.repository.MasterDataRepository
import com.utb.wms.domain.repository.OutboundRepository
import com.utb.wms.domain.repository.OutboundResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BarisPengeluaran(
    val id: Long,
    val item: Item? = null,
    val location: Location? = null,
    val qty: String = "",
    val tersedia: Int? = null,
) {
    val qtyAngka: Int?
        get() = qty.toIntOrNull()

    val melebihiStok: Boolean
        get() = tersedia != null && (qtyAngka ?: 0) > tersedia

    val lengkap: Boolean
        get() = item != null && location != null && (qtyAngka ?: 0) > 0
}

data class GoodsIssueState(
    val items: List<Item> = emptyList(),
    val locations: List<Location> = emptyList(),
    val tujuan: String = "",
    val baris: List<BarisPengeluaran> = listOf(BarisPengeluaran(id = 1)),
    val sedangMenyimpan: Boolean = false,
    val pesan: String? = null,
    val galat: String? = null,
) {
    val dapatDisimpan: Boolean
        get() = tujuan.isNotBlank() &&
            baris.isNotEmpty() &&
            baris.all { it.lengkap && !it.melebihiStok } &&
            !sedangMenyimpan
}

class GoodsIssueViewModel(
    private val masterDataRepository: MasterDataRepository,
    private val inventoryRepository: InventoryRepository,
    private val outboundRepository: OutboundRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(GoodsIssueState())

    val state: StateFlow<GoodsIssueState> = _state.asStateFlow()

    private var idBerikutnya = 2L

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
    }

    fun ubahTujuan(nilai: String) {
        _state.update { it.copy(tujuan = nilai, galat = null) }
    }

    fun tambahBaris() {
        _state.update { it.copy(baris = it.baris + BarisPengeluaran(id = idBerikutnya++)) }
    }

    fun hapusBaris(id: Long) {
        _state.update { keadaan ->
            val tersisa = keadaan.baris.filterNot { it.id == id }
            keadaan.copy(
                baris = tersisa.ifEmpty { listOf(BarisPengeluaran(id = idBerikutnya++)) },
            )
        }
    }

    fun pilihItem(id: Long, item: Item) {
        ubahBaris(id) { it.copy(item = item, tersedia = null) }
        segarkanStok(id)
    }

    fun pilihLokasi(id: Long, location: Location) {
        ubahBaris(id) { it.copy(location = location, tersedia = null) }
        segarkanStok(id)
    }

    fun ubahQty(id: Long, qty: String) =
        ubahBaris(id) { it.copy(qty = qty.filter(Char::isDigit).take(6)) }

    fun pesanDibaca() {
        _state.update { it.copy(pesan = null, galat = null) }
    }

    fun simpan() {
        val keadaan = _state.value
        if (!keadaan.dapatDisimpan) return

        val operator = authRepository.currentUser.value
        if (operator == null) {
            _state.update { it.copy(galat = "Sesi berakhir, silakan masuk kembali") }
            return
        }

        val details = keadaan.baris.mapNotNull { baris ->
            val item = baris.item ?: return@mapNotNull null
            val location = baris.location ?: return@mapNotNull null
            val qty = baris.qtyAngka ?: return@mapNotNull null
            GoodsIssueDetail(item = item, location = location, qty = qty)
        }
        if (details.size != keadaan.baris.size) {
            _state.update { it.copy(galat = "Ada baris yang belum lengkap") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(sedangMenyimpan = true, pesan = null, galat = null) }

            val hasil = runCatching {
                outboundRepository.createGoodsIssue(
                    tujuan = keadaan.tujuan.trim(),
                    operator = operator,
                    details = details,
                    tanggal = System.currentTimeMillis(),
                )
            }

            hasil.fold(
                onSuccess = { keluaran -> tanganiHasil(keluaran) },
                onFailure = { penyebab ->
                    _state.update {
                        it.copy(
                            sedangMenyimpan = false,
                            galat = penyebab.message ?: "Gagal menyimpan pengeluaran",
                        )
                    }
                },
            )
        }
    }

    private fun tanganiHasil(hasil: OutboundResult) {
        when (hasil) {
            is OutboundResult.Success -> {
                _state.update {
                    it.copy(
                        tujuan = "",
                        baris = listOf(BarisPengeluaran(id = idBerikutnya++)),
                        sedangMenyimpan = false,
                        pesan = "Pengeluaran ${hasil.issue.noIssue} tersimpan",
                    )
                }
            }

            is OutboundResult.InsufficientStock -> {
                _state.update { keadaan ->
                    keadaan.copy(
                        sedangMenyimpan = false,
                        galat = "Stok ${hasil.sku} tidak mencukupi: " +
                            "tersedia ${hasil.tersedia}, diminta ${hasil.diminta}",
                        baris = keadaan.baris.map { baris ->
                            if (baris.item?.sku == hasil.sku) {
                                baris.copy(tersedia = hasil.tersedia)
                            } else {
                                baris
                            }
                        },
                    )
                }
            }
        }
    }

    private fun segarkanStok(id: Long) {
        val baris = _state.value.baris.firstOrNull { it.id == id } ?: return
        val item = baris.item ?: return
        val location = baris.location ?: return

        viewModelScope.launch {
            val tersedia = inventoryRepository.stockAt(item.sku, location.kode)
            ubahBaris(id) { it.copy(tersedia = tersedia) }
        }
    }

    private fun ubahBaris(id: Long, transform: (BarisPengeluaran) -> BarisPengeluaran) {
        _state.update { keadaan ->
            keadaan.copy(
                baris = keadaan.baris.map { if (it.id == id) transform(it) else it },
                galat = null,
            )
        }
    }
}
