package com.utb.wms.domain.repository

import com.utb.wms.domain.model.BarisLaporanMutasi
import com.utb.wms.domain.model.BarisLaporanStok

interface ReportRepository {

    suspend fun laporanStok(): List<BarisLaporanStok>

    suspend fun laporanMutasi(dari: Long, sampai: Long): List<BarisLaporanMutasi>
}
