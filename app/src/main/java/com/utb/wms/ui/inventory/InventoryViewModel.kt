package com.utb.wms.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.utb.wms.domain.model.Stock
import com.utb.wms.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class StockListState(
    val baris: List<Stock> = emptyList(),
    val jumlahMenipis: Int = 0,
    val hanyaMenipis: Boolean = false,
)

class InventoryViewModel(
    inventoryRepository: InventoryRepository,
) : ViewModel() {

    private val _hanyaMenipis = MutableStateFlow(false)

    val state: StateFlow<StockListState> =
        combine(inventoryRepository.observeStocks(), _hanyaMenipis) { semua, saring ->
            StockListState(
                baris = if (saring) semua.filter { it.dibawahMinimum } else semua,
                jumlahMenipis = semua.count { it.dibawahMinimum },
                hanyaMenipis = saring,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StockListState(),
        )

    fun saringMenipis(aktif: Boolean) {
        _hanyaMenipis.update { aktif }
    }

    companion object {
        fun factory(inventoryRepository: InventoryRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { InventoryViewModel(inventoryRepository) }
            }
    }
}
