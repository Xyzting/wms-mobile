package com.utb.wms.ui.masterdata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.utb.wms.domain.model.Item
import com.utb.wms.domain.model.Location
import com.utb.wms.domain.model.Supplier
import com.utb.wms.domain.repository.MasterDataRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MasterDataViewModel(
    private val masterDataRepository: MasterDataRepository,
) : ViewModel() {

    private val _galat = Channel<String>(Channel.BUFFERED)

    val galat: Flow<String> = _galat.receiveAsFlow()

    val items: StateFlow<List<Item>> = masterDataRepository.observeAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val locations: StateFlow<List<Location>> = masterDataRepository.observeAllLocations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val suppliers: StateFlow<List<Supplier>> = masterDataRepository.observeAllSuppliers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun simpanItem(item: Item) = jalankan { masterDataRepository.simpanItem(item) }

    fun simpanLocation(location: Location) = jalankan {
        masterDataRepository.simpanLocation(location)
    }

    fun simpanSupplier(supplier: Supplier) = jalankan {
        masterDataRepository.simpanSupplier(supplier)
    }

    fun nonaktifkanItem(sku: String) = jalankan { masterDataRepository.nonaktifkanItem(sku) }

    fun nonaktifkanLocation(kode: String) = jalankan {
        masterDataRepository.nonaktifkanLocation(kode)
    }

    fun nonaktifkanSupplier(id: String) = jalankan {
        masterDataRepository.nonaktifkanSupplier(id)
    }

    private fun jalankan(aksi: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { aksi() }.onFailure { penyebab ->
                _galat.send(penyebab.message ?: "Data gagal disimpan")
            }
        }
    }

    companion object {

        fun factory(
            masterDataRepository: MasterDataRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { MasterDataViewModel(masterDataRepository) }
        }
    }
}
