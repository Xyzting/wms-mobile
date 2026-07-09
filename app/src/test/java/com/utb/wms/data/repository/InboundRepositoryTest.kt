package com.utb.wms.data.repository

import com.utb.wms.data.local.entity.StockEntity
import com.utb.wms.domain.model.DocumentStatus
import com.utb.wms.domain.model.GoodsReceiptDetail
import com.utb.wms.domain.model.Item
import com.utb.wms.domain.model.Location
import com.utb.wms.domain.model.MovementType
import com.utb.wms.domain.model.Role
import com.utb.wms.domain.model.Supplier
import com.utb.wms.domain.model.User
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InboundRepositoryTest {

    private val item = Item(sku = "BOLT-M8-30", nama = "Bolt M8x30mm", satuan = "pcs", stokMinimum = 50)
    private val lokasi = Location(kode = "A-01", nama = "Rak A-01", kapasitas = 500)
    private val supplier = Supplier(id = "SUP-001", nama = "PT Sumber Makmur")
    private val operator = User(
        id = "U-02",
        username = "operator",
        password = "operator123",
        nama = "Budi Santoso",
        role = Role(id = "R-02", namaRole = "Operator"),
    )
    private val tanggal = 1_720_000_000_000L

    @Test
    fun `penerimaan barang menambah stok dan mencatat pergerakan`() = runTest {
        val stockDao = FakeStockDao(listOf(StockEntity("STK-1", item.sku, lokasi.kode, 100)))
        val movementDao = FakeMovementDao()
        val receiptDao = FakeGoodsReceiptDao()
        val repository = InboundRepositoryImpl(
            transactionRunner = FakeTransactionRunner(),
            goodsReceiptDao = receiptDao,
            stockDao = stockDao,
            movementDao = movementDao,
        )

        val receipt = repository.createGoodsReceipt(
            supplier = supplier,
            operator = operator,
            details = listOf(GoodsReceiptDetail(item, lokasi, 20)),
            tanggal = tanggal,
        )

        assertEquals("GR-0001", receipt.noReceipt)
        assertEquals(DocumentStatus.POSTED, receipt.status)
        assertEquals(120, stockDao.find(item.sku, lokasi.kode)?.jumlahStok ?: 0)

        assertEquals(1, movementDao.baris.size)
        val pergerakan = movementDao.baris.first()
        assertEquals(MovementType.INBOUND, pergerakan.tipe)
        assertEquals(20, pergerakan.qty)
        assertEquals("GR-0001", pergerakan.referensi)

        assertEquals(1, receiptDao.header.size)
        assertEquals(1, receiptDao.detail.size)
    }

    @Test
    fun `penerimaan pada lokasi tanpa stok membuat baris stok baru`() = runTest {
        val stockDao = FakeStockDao()
        val repository = InboundRepositoryImpl(
            transactionRunner = FakeTransactionRunner(),
            goodsReceiptDao = FakeGoodsReceiptDao(),
            stockDao = stockDao,
            movementDao = FakeMovementDao(),
        )

        repository.createGoodsReceipt(
            supplier = supplier,
            operator = operator,
            details = listOf(GoodsReceiptDetail(item, lokasi, 35)),
            tanggal = tanggal,
        )

        assertEquals(1, stockDao.baris.size)
        assertEquals(35, stockDao.find(item.sku, lokasi.kode)?.jumlahStok ?: 0)
    }

    @Test
    fun `detail kosong ditolak`() = runTest {
        val repository = InboundRepositoryImpl(
            transactionRunner = FakeTransactionRunner(),
            goodsReceiptDao = FakeGoodsReceiptDao(),
            stockDao = FakeStockDao(),
            movementDao = FakeMovementDao(),
        )

        val gagal = runCatching {
            repository.createGoodsReceipt(
                supplier = supplier,
                operator = operator,
                details = emptyList(),
                tanggal = tanggal,
            )
        }

        assertTrue(gagal.exceptionOrNull() is IllegalArgumentException)
    }
}
