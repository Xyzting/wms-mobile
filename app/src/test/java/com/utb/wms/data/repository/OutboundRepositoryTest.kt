package com.utb.wms.data.repository

import com.utb.wms.data.local.entity.StockEntity
import com.utb.wms.domain.model.DocumentStatus
import com.utb.wms.domain.model.GoodsIssueDetail
import com.utb.wms.domain.model.Item
import com.utb.wms.domain.model.Location
import com.utb.wms.domain.model.MovementType
import com.utb.wms.domain.model.Role
import com.utb.wms.domain.model.User
import com.utb.wms.domain.repository.OutboundResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OutboundRepositoryTest {

    private val item = Item(sku = "BOLT-M8-30", nama = "Bolt M8x30mm", satuan = "pcs", stokMinimum = 50)
    private val lokasi = Location(kode = "A-01", nama = "Rak A-01", kapasitas = 500)
    private val operator = User(
        id = "U-02",
        username = "operator",
        password = "operator123",
        nama = "Budi Santoso",
        role = Role(id = "R-02", namaRole = "Operator"),
    )
    private val tanggal = 1_720_000_000_000L

    private fun stokAwal(jumlah: Int) =
        FakeStockDao(listOf(StockEntity("STK-1", item.sku, lokasi.kode, jumlah)))

    @Test
    fun `pengeluaran barang mengurangi stok dan mencatat pergerakan`() = runTest {
        val stockDao = stokAwal(100)
        val movementDao = FakeMovementDao()
        val issueDao = FakeGoodsIssueDao()
        val repository = OutboundRepositoryImpl(
            transactionRunner = FakeTransactionRunner(),
            goodsIssueDao = issueDao,
            stockDao = stockDao,
            movementDao = movementDao,
        )

        val hasil = repository.createGoodsIssue(
            tujuan = "Produksi Line A",
            operator = operator,
            details = listOf(GoodsIssueDetail(item, lokasi, 30)),
            tanggal = tanggal,
        )

        assertTrue(hasil is OutboundResult.Success)
        val issue = (hasil as OutboundResult.Success).issue
        assertEquals("GI-0001", issue.noIssue)
        assertEquals(DocumentStatus.POSTED, issue.status)

        assertEquals(70, stockDao.find(item.sku, lokasi.kode)?.jumlahStok ?: 0)
        assertEquals(1, movementDao.baris.size)
        assertEquals(MovementType.OUTBOUND, movementDao.baris.first().tipe)
        assertEquals("GI-0001", movementDao.baris.first().referensi)
        assertEquals(1, issueDao.header.size)
    }

    @Test
    fun `stok kurang menolak dokumen tanpa menulis apa pun`() = runTest {
        val stockDao = stokAwal(100)
        val movementDao = FakeMovementDao()
        val issueDao = FakeGoodsIssueDao()
        val repository = OutboundRepositoryImpl(
            transactionRunner = FakeTransactionRunner(),
            goodsIssueDao = issueDao,
            stockDao = stockDao,
            movementDao = movementDao,
        )

        val hasil = repository.createGoodsIssue(
            tujuan = "Produksi Line A",
            operator = operator,
            details = listOf(GoodsIssueDetail(item, lokasi, 150)),
            tanggal = tanggal,
        )

        assertTrue(hasil is OutboundResult.InsufficientStock)
        val kurang = hasil as OutboundResult.InsufficientStock
        assertEquals(item.sku, kurang.sku)
        assertEquals(100, kurang.tersedia)
        assertEquals(150, kurang.diminta)

        assertEquals(100, stockDao.find(item.sku, lokasi.kode)?.jumlahStok ?: 0)
        assertEquals(0, movementDao.baris.size)
        assertEquals(0, issueDao.header.size)
        assertEquals(0, issueDao.detail.size)
    }

    @Test
    fun `dua baris pada sku dan lokasi sama dijumlahkan sebelum diperiksa`() = runTest {
        val stockDao = stokAwal(100)
        val movementDao = FakeMovementDao()
        val issueDao = FakeGoodsIssueDao()
        val repository = OutboundRepositoryImpl(
            transactionRunner = FakeTransactionRunner(),
            goodsIssueDao = issueDao,
            stockDao = stockDao,
            movementDao = movementDao,
        )

        val hasil = repository.createGoodsIssue(
            tujuan = "Produksi Line A",
            operator = operator,
            details = listOf(
                GoodsIssueDetail(item, lokasi, 60),
                GoodsIssueDetail(item, lokasi, 60),
            ),
            tanggal = tanggal,
        )

        assertTrue(hasil is OutboundResult.InsufficientStock)
        val kurang = hasil as OutboundResult.InsufficientStock
        assertEquals(100, kurang.tersedia)
        assertEquals(120, kurang.diminta)

        assertEquals(100, stockDao.find(item.sku, lokasi.kode)?.jumlahStok ?: 0)
        assertEquals(0, movementDao.baris.size)
    }

    @Test
    fun `pengeluaran tepat sejumlah stok diperbolehkan`() = runTest {
        val stockDao = stokAwal(100)
        val repository = OutboundRepositoryImpl(
            transactionRunner = FakeTransactionRunner(),
            goodsIssueDao = FakeGoodsIssueDao(),
            stockDao = stockDao,
            movementDao = FakeMovementDao(),
        )

        val hasil = repository.createGoodsIssue(
            tujuan = "Produksi Line A",
            operator = operator,
            details = listOf(GoodsIssueDetail(item, lokasi, 100)),
            tanggal = tanggal,
        )

        assertTrue(hasil is OutboundResult.Success)
        assertEquals(0, stockDao.find(item.sku, lokasi.kode)?.jumlahStok ?: 0)
    }
}
