package com.utb.wms.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utb.wms.domain.model.Stock
import com.utb.wms.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class InventoryViewModel(
    inventoryRepository: InventoryRepository,
) : ViewModel() {

    val stocks: StateFlow<List<Stock>> = inventoryRepository.observeStocks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )
}
