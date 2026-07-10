package com.utb.wms.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.utb.wms.domain.model.MovementType
import com.utb.wms.domain.model.StockMovement
import com.utb.wms.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class StockMovementState(
    val baris: List<StockMovement> = emptyList(),
    val menyaring: Boolean = false,
)

class StockMovementViewModel(
    inventoryRepository: InventoryRepository,
) : ViewModel() {

    private val _kataKunci = MutableStateFlow("")

    private val _tipe = MutableStateFlow<MovementType?>(null)

    val state: StateFlow<StockMovementState> = combine(
        inventoryRepository.observeMovements(),
        _kataKunci,
        _tipe,
    ) { semua, kunci, tipe ->
        StockMovementState(
            baris = semua.filter { baris ->
                (tipe == null || baris.tipe == tipe) && cocok(baris, kunci)
            },
            menyaring = kunci.isNotBlank() || tipe != null,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StockMovementState(),
    )

    fun cari(kataKunci: String) {
        _kataKunci.update { kataKunci.trim() }
    }

    fun saringTipe(tipe: MovementType?) {
        _tipe.update { tipe }
    }

    private fun cocok(baris: StockMovement, kataKunci: String): Boolean {
        if (kataKunci.isBlank()) return true
        return baris.item.sku.contains(kataKunci, ignoreCase = true) ||
            baris.item.nama.contains(kataKunci, ignoreCase = true)
    }

    companion object {
        fun factory(inventoryRepository: InventoryRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { StockMovementViewModel(inventoryRepository) }
            }
    }
}
