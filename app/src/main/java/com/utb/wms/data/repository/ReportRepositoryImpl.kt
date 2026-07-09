package com.utb.wms.data.repository

import com.utb.wms.data.local.dao.MovementDao
import com.utb.wms.data.local.dao.StockDao
import com.utb.wms.domain.model.BarisLaporanMutasi
import com.utb.wms.domain.model.BarisLaporanStok
import com.utb.wms.domain.repository.ReportRepository

class ReportRepositoryImpl(
    private val stockDao: StockDao,
    private val movementDao: MovementDao,
) : ReportRepository {

    override suspend fun laporanStok(): List<BarisLaporanStok> =
        TODO("BE-2: petakan stockDao.semuaStok(), urutkan yang di bawah minimum lebih dulu")

    override suspend fun laporanMutasi(dari: Long, sampai: Long): List<BarisLaporanMutasi> =
        TODO("BE-2: pakai movementDao.mutasiRentang(dari, sampai)")
}
