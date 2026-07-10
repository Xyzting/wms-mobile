package com.utb.wms.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.utb.wms.domain.model.Item
import com.utb.wms.domain.repository.MasterDataRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class ItemCatalogViewModel(
    masterDataRepository: MasterDataRepository,
) : ViewModel() {

    private val _kataKunci = MutableStateFlow("")

    val hasil: StateFlow<List<Item>> = _kataKunci
        .flatMapLatest { kunci -> masterDataRepository.searchItems(kunci) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun cari(teks: String) {
        _kataKunci.value = teks
    }

    companion object {

        fun factory(
            masterDataRepository: MasterDataRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { ItemCatalogViewModel(masterDataRepository) }
        }
    }
}
