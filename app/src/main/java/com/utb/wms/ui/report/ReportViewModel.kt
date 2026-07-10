package com.utb.wms.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.utb.wms.domain.model.BarisLaporanMutasi
import com.utb.wms.domain.model.BarisLaporanStok
import com.utb.wms.domain.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReportViewModel(
    private val reportRepository: ReportRepository,
    private val waktu: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {

    private val _laporanStok = MutableStateFlow<List<BarisLaporanStok>>(emptyList())

    val laporanStok: StateFlow<List<BarisLaporanStok>> = _laporanStok.asStateFlow()

    private val _laporanMutasi = MutableStateFlow<List<BarisLaporanMutasi>>(emptyList())

    val laporanMutasi: StateFlow<List<BarisLaporanMutasi>> = _laporanMutasi.asStateFlow()

    init {
        muat()
    }

    fun muat() {
        viewModelScope.launch {
            _laporanStok.value = runCatching { reportRepository.laporanStok() }
                .getOrDefault(emptyList())

            val sampai = waktu()
            val dari = sampai - RENTANG_HARI * SEHARI_MILIDETIK
            _laporanMutasi.value = runCatching { reportRepository.laporanMutasi(dari, sampai) }
                .getOrDefault(emptyList())
        }
    }

    companion object {

        const val RENTANG_HARI = 30L

        private const val SEHARI_MILIDETIK = 24L * 60L * 60L * 1000L

        fun factory(reportRepository: ReportRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { ReportViewModel(reportRepository) }
            }
    }
}
