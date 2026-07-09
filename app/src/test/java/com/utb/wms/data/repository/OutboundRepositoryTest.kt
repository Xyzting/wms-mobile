package com.utb.wms.data.repository

import com.utb.wms.data.local.entity.StockEntity
import com.utb.wms.domain.model.DocumentStatus
import com.utb.wms.domain.model.GoodsIssueDetail
import com.utb.wms.domain.model.MovementType
import com.utb.wms.domain.repository.DocumentResult
import com.utb.wms.domain.repository.OutboundRepository
import com.utb.wms.domain.repository.OutboundResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OutboundRepositoryTest {

    private val stockDao = FakeStockDao(
        listOf(StockEntity("STK-1", Contoh.item.sku, Contoh.lokasi.kode, 100)),
    )
    private val movementDao = FakeMovementDao()
    private val issueDao = FakeGoodsIssueDao()

    private val repository: OutboundRepository = OutboundRepositoryImpl(
        transactionRunner = FakeTransactionRunner(),
        goodsIssueDao = issueDao,
        stockDao = stockDao,
        movementDao = movementDao,
        waktu = Contoh.jam,
    )

    private suspend fun buatDraft(qty: Int = 30): String {
        val hasil = repository.createGoodsIssue(
            tujuan = "Produksi Lini 2",
            operator = Contoh.operator,
            details = listOf(GoodsIssueDetail(Contoh.item, Contoh.lokasi, qty)),
            tanggal = Contoh.TANGGAL,
        )
        return (hasil as OutboundResult.Success).issue.id
    }

    @Test
    fun `pengeluaran baru berstatus draft dan belum mengurangi stok`() = runTest {
        val hasil = repository.createGoodsIssue(
            tujuan = "Produksi Lini 2",
            operator = Contoh.operator,
            details = listOf(GoodsIssueDetail(Contoh.item, Contoh.lokasi, 30)),
            tanggal = Contoh.TANGGAL,
        )

        assertTrue(hasil is OutboundResult.Success)
        val issue = (hasil as OutboundResult.Success).issue
        assertEquals("GI-0001", issue.noIssue)
        assertEquals(DocumentStatus.DRAFT, issue.status)
        assertEquals(100, stockDao.find(Contoh.item.sku, Contoh.lokasi.kode)?.jumlahStok)
        assertTrue(movementDao.baris.isEmpty())
    }

    @Test
    fun `detail kosong ditolak`() = runTest {
        val gagal = runCatching {
            repository.createGoodsIssue(
                tujuan = "Produksi Lini 2",
                operator = Contoh.operator,
                details = emptyList(),
                tanggal = Contoh.TANGGAL,
            )
        }

        assertTrue(gagal.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `kuantitas nol ditolak`() = runTest {
        val gagal = runCatching {
            repository.createGoodsIssue(
                tujuan = "Produksi Lini 2",
                operator = Contoh.operator,
                details = listOf(GoodsIssueDetail(Contoh.item, Contoh.lokasi, 0)),
                tanggal = Contoh.TANGGAL,
            )
        }

        assertTrue(gagal.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `posting dari validated mengurangi stok dan mencatat pergerakan`() = runTest {
        val id = buatDraft(qty = 30)
        repository.validateGoodsIssue(id, Contoh.supervisor)

        val hasil = repository.postGoodsIssue(id, Contoh.TANGGAL)

        assertEquals(DocumentResult.Success(id, DocumentStatus.POSTED), hasil)
        assertEquals(70, stockDao.find(Contoh.item.sku, Contoh.lokasi.kode)?.jumlahStok)

        val pergerakan = movementDao.baris.single()
        assertEquals(MovementType.OUTBOUND, pergerakan.tipe)
        assertEquals(30, pergerakan.qty)
        assertEquals("GI-0001", pergerakan.referensi)
        assertEquals(Contoh.operator.id, pergerakan.operatorId)
    }

    @Test
    fun `posting saat stok kurang tidak menulis apa pun`() = runTest {
        val id = buatDraft(qty = 150)
        repository.validateGoodsIssue(id, Contoh.supervisor)

        val hasil = repository.postGoodsIssue(id, Contoh.TANGGAL)

        assertEquals(DocumentResult.InsufficientStock(Contoh.item.sku, 100, 150), hasil)
        assertEquals(100, stockDao.find(Contoh.item.sku, Contoh.lokasi.kode)?.jumlahStok)
        assertTrue(movementDao.baris.isEmpty())
        assertEquals(DocumentStatus.VALIDATED, issueDao.header.single().status)
    }

    @Test
    fun `kebutuhan dijumlahkan lebih dulu untuk sku dan lokasi yang sama`() = runTest {
        val hasil = repository.createGoodsIssue(
            tujuan = "Produksi Lini 2",
            operator = Contoh.operator,
            details = listOf(
                GoodsIssueDetail(Contoh.item, Contoh.lokasi, 60),
                GoodsIssueDetail(Contoh.item, Contoh.lokasi, 60),
            ),
            tanggal = Contoh.TANGGAL,
        )
        val id = (hasil as OutboundResult.Success).issue.id
        repository.validateGoodsIssue(id, Contoh.supervisor)

        val posting = repository.postGoodsIssue(id, Contoh.TANGGAL)

        assertEquals(DocumentResult.InsufficientStock(Contoh.item.sku, 100, 120), posting)
        assertEquals(100, stockDao.find(Contoh.item.sku, Contoh.lokasi.kode)?.jumlahStok)
        assertTrue(movementDao.baris.isEmpty())
    }

    @Test
    fun `posting langsung dari draft ditolak`() = runTest {
        val id = buatDraft()

        val hasil = repository.postGoodsIssue(id, Contoh.TANGGAL)

        assertEquals(
            DocumentResult.InvalidTransition(DocumentStatus.DRAFT, DocumentStatus.POSTED),
            hasil,
        )
        assertEquals(100, stockDao.find(Contoh.item.sku, Contoh.lokasi.kode)?.jumlahStok)
    }

    @Test
    fun `pembatalan dari validated mempertahankan penyetuju`() = runTest {
        val id = buatDraft()
        repository.validateGoodsIssue(id, Contoh.supervisor)

        val hasil = repository.cancelGoodsIssue(id, catatan = "Permintaan dibatalkan")

        assertEquals(DocumentResult.Success(id, DocumentStatus.CANCELLED), hasil)
        val header = issueDao.header.single()
        assertEquals(DocumentStatus.CANCELLED, header.status)
        assertEquals(Contoh.supervisor.id, header.approvedBy)
        assertEquals(Contoh.WAKTU_SETUJU, header.approvedAt)
        assertEquals("Permintaan dibatalkan", header.catatan)
        assertEquals(100, stockDao.find(Contoh.item.sku, Contoh.lokasi.kode)?.jumlahStok)
    }

    @Test
    fun `dokumen yang tidak dikenal menghasilkan not found`() = runTest {
        assertEquals(DocumentResult.NotFound, repository.postGoodsIssue("GI-999", Contoh.TANGGAL))
        assertEquals(
            DocumentResult.NotFound,
            repository.validateGoodsIssue("GI-999", Contoh.supervisor),
        )
    }
}
