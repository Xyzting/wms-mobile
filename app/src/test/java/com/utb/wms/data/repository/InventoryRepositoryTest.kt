package com.utb.wms.data.repository

import com.utb.wms.data.local.entity.StockEntity
import com.utb.wms.domain.model.MovementType
import com.utb.wms.domain.repository.InventoryRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InventoryRepositoryTest {

    private val stockDao = FakeStockDao(
        listOf(StockEntity("STK-1", Contoh.item.sku, Contoh.lokasi.kode, 100)),
    )
    private val movementDao = FakeMovementDao()

    private val repository: InventoryRepository = InventoryRepositoryImpl(
        transactionRunner = FakeTransactionRunner(),
        stockDao = stockDao,
        movementDao = movementDao,
        waktu = Contoh.jam,
    )

    @Test
    fun `penyesuaian naik mencatat pergerakan bertanda positif`() = runTest {
        repository.adjustStock(
            item = Contoh.item,
            location = Contoh.lokasi,
            jumlahBaru = 130,
            alasan = "Hasil stock opname",
            operator = Contoh.supervisor,
        )

        assertEquals(130, stockDao.find(Contoh.item.sku, Contoh.lokasi.kode)?.jumlahStok)

        val pergerakan = movementDao.baris.single()
        assertEquals(MovementType.ADJUSTMENT, pergerakan.tipe)
        assertEquals(30, pergerakan.qty)
        assertEquals("Hasil stock opname", pergerakan.keterangan)
        assertEquals(Contoh.supervisor.id, pergerakan.operatorId)
        assertEquals(Contoh.WAKTU_SETUJU, pergerakan.tanggal)
    }

    @Test
    fun `penyesuaian turun mencatat pergerakan bertanda negatif`() = runTest {
        repository.adjustStock(
            item = Contoh.item,
            location = Contoh.lokasi,
            jumlahBaru = 85,
            alasan = "Barang rusak",
            operator = Contoh.supervisor,
        )

        assertEquals(85, stockDao.find(Contoh.item.sku, Contoh.lokasi.kode)?.jumlahStok)
        assertEquals(-15, movementDao.baris.single().qty)
    }

    @Test
    fun `penyesuaian tanpa selisih tidak mencatat pergerakan`() = runTest {
        repository.adjustStock(
            item = Contoh.item,
            location = Contoh.lokasi,
            jumlahBaru = 100,
            alasan = "Sesuai catatan",
            operator = Contoh.supervisor,
        )

        assertEquals(100, stockDao.find(Contoh.item.sku, Contoh.lokasi.kode)?.jumlahStok)
        assertTrue(movementDao.baris.isEmpty())
    }

    @Test
    fun `penyesuaian pada lokasi tanpa baris stok membuat baris baru`() = runTest {
        repository.adjustStock(
            item = Contoh.itemKedua,
            location = Contoh.lokasiKedua,
            jumlahBaru = 40,
            alasan = "Temuan stock opname",
            operator = Contoh.supervisor,
        )

        assertEquals(2, stockDao.baris.size)
        assertEquals(40, stockDao.find(Contoh.itemKedua.sku, Contoh.lokasiKedua.kode)?.jumlahStok)
        assertEquals(40, movementDao.baris.single().qty)
    }

    @Test
    fun `alasan kosong ditolak`() = runTest {
        val gagal = runCatching {
            repository.adjustStock(
                item = Contoh.item,
                location = Contoh.lokasi,
                jumlahBaru = 120,
                alasan = "   ",
                operator = Contoh.supervisor,
            )
        }

        assertTrue(gagal.exceptionOrNull() is IllegalArgumentException)
        assertEquals(100, stockDao.find(Contoh.item.sku, Contoh.lokasi.kode)?.jumlahStok)
    }

    @Test
    fun `jumlah baru negatif ditolak`() = runTest {
        val gagal = runCatching {
            repository.adjustStock(
                item = Contoh.item,
                location = Contoh.lokasi,
                jumlahBaru = -1,
                alasan = "Salah ketik",
                operator = Contoh.supervisor,
            )
        }

        assertTrue(gagal.exceptionOrNull() is IllegalArgumentException)
    }
}
