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
        stockDao.semuaStok()
            .map { baris ->
                BarisLaporanStok(
                    sku = baris.item.sku,
                    namaItem = baris.item.nama,
                    satuan = baris.item.satuan,
                    kodeLokasi = baris.location.kode,
                    namaLokasi = baris.location.nama,
                    jumlahStok = baris.stock.jumlahStok,
                    stokMinimum = baris.item.stokMinimum,
                )
            }
            .sortedWith(
                compareByDescending<BarisLaporanStok> { it.dibawahMinimum }
                    .thenBy { it.sku }
                    .thenBy { it.kodeLokasi },
            )

    override suspend fun laporanMutasi(dari: Long, sampai: Long): List<BarisLaporanMutasi> {
        require(dari <= sampai) { "Tanggal awal tidak boleh melewati tanggal akhir" }

        return movementDao.mutasiRentang(dari, sampai).map { baris ->
            BarisLaporanMutasi(
                tanggal = baris.movement.tanggal,
                sku = baris.item.sku,
                namaItem = baris.item.nama,
                kodeLokasi = baris.location.kode,
                tipe = baris.movement.tipe,
                qty = baris.movement.qty,
                referensi = baris.movement.referensi,
                keterangan = baris.movement.keterangan,
            )
        }
    }
}
