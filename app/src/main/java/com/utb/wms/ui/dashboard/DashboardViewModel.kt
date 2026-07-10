package com.utb.wms.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.utb.wms.domain.model.DocumentStatus
import com.utb.wms.domain.model.User
import com.utb.wms.domain.repository.AuthRepository
import com.utb.wms.domain.repository.InboundRepository
import com.utb.wms.domain.repository.InventoryRepository
import com.utb.wms.domain.repository.OutboundRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardState(
    val pengguna: User? = null,
    val stokMenipis: Int = 0,
    val dokumenMenunggu: Int = 0,
)

class DashboardViewModel(
    authRepository: AuthRepository,
    inventoryRepository: InventoryRepository,
    inboundRepository: InboundRepository,
    outboundRepository: OutboundRepository,
) : ViewModel() {

    val state: StateFlow<DashboardState> = combine(
        authRepository.currentUser,
        inventoryRepository.observeStocks(),
        inboundRepository.observeGoodsReceiptsByStatus(DocumentStatus.DRAFT),
        outboundRepository.observeGoodsIssuesByStatus(DocumentStatus.DRAFT),
    ) { pengguna, stok, penerimaan, pengeluaran ->
        DashboardState(
            pengguna = pengguna,
            stokMenipis = stok.count { it.dibawahMinimum },
            dokumenMenunggu = penerimaan.size + pengeluaran.size,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardState(),
    )

    companion object {
        fun factory(
            authRepository: AuthRepository,
            inventoryRepository: InventoryRepository,
            inboundRepository: InboundRepository,
            outboundRepository: OutboundRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DashboardViewModel(
                    authRepository,
                    inventoryRepository,
                    inboundRepository,
                    outboundRepository,
                )
            }
        }
    }
}
