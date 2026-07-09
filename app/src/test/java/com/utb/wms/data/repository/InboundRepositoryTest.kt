package com.utb.wms.data.repository

import com.utb.wms.data.local.entity.StockEntity
import com.utb.wms.domain.model.DocumentStatus
import com.utb.wms.domain.model.GoodsReceiptDetail
import com.utb.wms.domain.model.MovementType
import com.utb.wms.domain.repository.DocumentResult
import com.utb.wms.domain.repository.InboundRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InboundRepositoryTest {

    private val stockDao = FakeStockDao(
        listOf(StockEntity("STK-1", Contoh.item.sku, Contoh.lokasi.kode, 100)),
    )
    private val movementDao = FakeMovementDao()
    private val receiptDao = FakeGoodsReceiptDao()

    private val repository: InboundRepository = InboundRepositoryImpl(
        transactionRunner = FakeTransactionRunner(),
        goodsReceiptDao = receiptDao,
        stockDao = stockDao,
        movementDao = movementDao,
        waktu = Contoh.jam,
    )

    private val detail = listOf(GoodsReceiptDetail(Contoh.item, Contoh.lokasi, 20))

    private suspend fun buatDraft() = repository.createGoodsReceipt(
        supplier = Contoh.supplier,
        operator = Contoh.operator,
        details = detail,
        tanggal = Contoh.TANGGAL,
    )

    @Test
    fun `penerimaan baru berstatus draft dan belum menyentuh stok`() = runTest {
        val receipt = buatDraft()

        assertEquals("GR-0001", receipt.noReceipt)
        assertEquals(DocumentStatus.DRAFT, receipt.status)
        assertEquals(100, stockDao.find(Contoh.item.sku, Contoh.lokasi.kode)?.jumlahStok)
        assertTrue(movementDao.baris.isEmpty())
        assertEquals(1, receiptDao.header.size)
        assertEquals(1, receiptDao.detail.size)
    }

    @Test
    fun `detail kosong ditolak`() = runTest {
        val gagal = runCatching {
            repository.createGoodsReceipt(
                supplier = Contoh.supplier,
                operator = Contoh.operator,
                details = emptyList(),
                tanggal = Contoh.TANGGAL,
            )
        }

        assertTrue(gagal.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `validasi dari draft menyimpan penyetuju dan tidak menggerakkan stok`() = runTest {
        val receipt = buatDraft()

        val hasil = repository.validateGoodsReceipt(receipt.id, Contoh.supervisor)

        assertEquals(DocumentResult.Success(receipt.id, DocumentStatus.VALIDATED), hasil)
        val header = receiptDao.header.single()
        assertEquals(DocumentStatus.VALIDATED, header.status)
        assertEquals(Contoh.supervisor.id, header.approvedBy)
        assertEquals(Contoh.WAKTU_SETUJU, header.approvedAt)
        assertEquals(100, stockDao.find(Contoh.item.sku, Contoh.lokasi.kode)?.jumlahStok)
        assertTrue(movementDao.baris.isEmpty())
    }

    @Test
    fun `posting dari validated menambah stok dan mencatat pergerakan`() = runTest {
        val receipt = buatDraft()
        repository.validateGoodsReceipt(receipt.id, Contoh.supervisor)

        val hasil = repository.postGoodsReceipt(receipt.id, Contoh.TANGGAL)

        assertEquals(DocumentResult.Success(receipt.id, DocumentStatus.POSTED), hasil)
        assertEquals(120, stockDao.find(Contoh.item.sku, Contoh.lokasi.kode)?.jumlahStok)

        val pergerakan = movementDao.baris.single()
        assertEquals(MovementType.INBOUND, pergerakan.tipe)
        assertEquals(20, pergerakan.qty)
        assertEquals("GR-0001", pergerakan.referensi)
        assertEquals(Contoh.operator.id, pergerakan.operatorId)

        val header = receiptDao.header.single()
        assertEquals(DocumentStatus.POSTED, header.status)
        assertEquals(Contoh.supervisor.id, header.approvedBy)
        assertEquals(Contoh.WAKTU_SETUJU, header.approvedAt)
    }

    @Test
    fun `posting pada lokasi tanpa baris stok membuat baris baru`() = runTest {
        val receipt = repository.createGoodsReceipt(
            supplier = Contoh.supplier,
            operator = Contoh.operator,
            details = listOf(GoodsReceiptDetail(Contoh.itemKedua, Contoh.lokasiKedua, 35)),
            tanggal = Contoh.TANGGAL,
        )
        repository.validateGoodsReceipt(receipt.id, Contoh.supervisor)

        repository.postGoodsReceipt(receipt.id, Contoh.TANGGAL)

        assertEquals(2, stockDao.baris.size)
        assertEquals(35, stockDao.find(Contoh.itemKedua.sku, Contoh.lokasiKedua.kode)?.jumlahStok)
    }

    @Test
    fun `posting langsung dari draft ditolak`() = runTest {
        val receipt = buatDraft()

        val hasil = repository.postGoodsReceipt(receipt.id, Contoh.TANGGAL)

        assertEquals(
            DocumentResult.InvalidTransition(DocumentStatus.DRAFT, DocumentStatus.POSTED),
            hasil,
        )
        assertEquals(100, stockDao.find(Contoh.item.sku, Contoh.lokasi.kode)?.jumlahStok)
        assertTrue(movementDao.baris.isEmpty())
    }

    @Test
    fun `validasi ulang dokumen yang sudah posted ditolak`() = runTest {
        val receipt = buatDraft()
        repository.validateGoodsReceipt(receipt.id, Contoh.supervisor)
        repository.postGoodsReceipt(receipt.id, Contoh.TANGGAL)

        val hasil = repository.validateGoodsReceipt(receipt.id, Contoh.supervisor)

        assertEquals(
            DocumentResult.InvalidTransition(DocumentStatus.POSTED, DocumentStatus.VALIDATED),
            hasil,
        )
    }

    @Test
    fun `pembatalan dari draft mengubah status menjadi cancelled`() = runTest {
        val receipt = buatDraft()

        val hasil = repository.cancelGoodsReceipt(receipt.id, catatan = "Salah pemasok")

        assertEquals(DocumentResult.Success(receipt.id, DocumentStatus.CANCELLED), hasil)
        val header = receiptDao.header.single()
        assertEquals(DocumentStatus.CANCELLED, header.status)
        assertEquals("Salah pemasok", header.catatan)
        assertNull(header.approvedBy)
        assertTrue(movementDao.baris.isEmpty())
    }

    @Test
    fun `pembatalan dokumen yang sudah posted ditolak`() = runTest {
        val receipt = buatDraft()
        repository.validateGoodsReceipt(receipt.id, Contoh.supervisor)
        repository.postGoodsReceipt(receipt.id, Contoh.TANGGAL)

        val hasil = repository.cancelGoodsReceipt(receipt.id)

        assertEquals(
            DocumentResult.InvalidTransition(DocumentStatus.POSTED, DocumentStatus.CANCELLED),
            hasil,
        )
        assertEquals(120, stockDao.find(Contoh.item.sku, Contoh.lokasi.kode)?.jumlahStok)
    }

    @Test
    fun `dokumen yang tidak dikenal menghasilkan not found`() = runTest {
        assertEquals(DocumentResult.NotFound, repository.postGoodsReceipt("GR-999", Contoh.TANGGAL))
        assertEquals(DocumentResult.NotFound, repository.cancelGoodsReceipt("GR-999"))
        assertNull(repository.findGoodsReceipt("GR-999"))
    }
}
