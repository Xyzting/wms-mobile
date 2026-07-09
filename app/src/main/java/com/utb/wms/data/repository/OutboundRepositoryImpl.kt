package com.utb.wms.data.repository

import com.utb.wms.data.local.TransactionRunner
import com.utb.wms.data.local.dao.GoodsIssueDao
import com.utb.wms.data.local.dao.MovementDao
import com.utb.wms.data.local.dao.StockDao
import com.utb.wms.data.local.entity.GoodsIssueDetailEntity
import com.utb.wms.data.local.entity.GoodsIssueEntity
import com.utb.wms.data.local.entity.StockMovementEntity
import com.utb.wms.data.local.mapper.toDomain
import com.utb.wms.domain.model.DocumentStatus
import com.utb.wms.domain.model.GoodsIssue
import com.utb.wms.domain.model.GoodsIssueDetail
import com.utb.wms.domain.model.MovementType
import com.utb.wms.domain.model.User
import com.utb.wms.domain.model.bolehPindahKe
import com.utb.wms.domain.repository.DocumentResult
import com.utb.wms.domain.repository.OutboundRepository
import com.utb.wms.domain.repository.OutboundResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OutboundRepositoryImpl(
    private val transactionRunner: TransactionRunner,
    private val goodsIssueDao: GoodsIssueDao,
    private val stockDao: StockDao,
    private val movementDao: MovementDao,
    private val waktu: () -> Long = { System.currentTimeMillis() },
) : OutboundRepository {

    override suspend fun createGoodsIssue(
        tujuan: String,
        operator: User,
        details: List<GoodsIssueDetail>,
        tanggal: Long,
    ): OutboundResult {
        require(tujuan.isNotBlank()) { "Tujuan pengeluaran wajib diisi" }
        require(details.isNotEmpty()) { "Detail pengeluaran tidak boleh kosong" }
        require(details.all { it.qty > 0 }) { "Kuantitas harus lebih dari nol" }

        return transactionRunner.transaction {
            val urut = goodsIssueDao.count() + 1
            val id = "GI-$urut"
            val noIssue = "GI-%04d".format(urut)

            goodsIssueDao.insertHeader(
                GoodsIssueEntity(
                    id = id,
                    noIssue = noIssue,
                    tanggal = tanggal,
                    tujuan = tujuan,
                    operatorId = operator.id,
                    status = DocumentStatus.DRAFT,
                ),
            )
            goodsIssueDao.insertDetails(
                details.map { detail ->
                    GoodsIssueDetailEntity(
                        issueId = id,
                        sku = detail.item.sku,
                        locationKode = detail.location.kode,
                        qty = detail.qty,
                    )
                },
            )

            OutboundResult.Success(
                GoodsIssue(
                    id = id,
                    noIssue = noIssue,
                    tanggal = tanggal,
                    tujuan = tujuan,
                    operator = operator,
                    status = DocumentStatus.DRAFT,
                    details = details,
                ),
            )
        }
    }

    override fun observeGoodsIssues(): Flow<List<GoodsIssue>> =
        goodsIssueDao.observeIssues().map { daftar -> daftar.map { it.toDomain() } }

    override fun observeGoodsIssuesByStatus(status: DocumentStatus): Flow<List<GoodsIssue>> =
        goodsIssueDao.observeIssuesByStatus(status).map { daftar -> daftar.map { it.toDomain() } }

    override suspend fun findGoodsIssue(id: String): GoodsIssue? =
        goodsIssueDao.findById(id)?.toDomain()

    override suspend fun validateGoodsIssue(
        id: String,
        approver: User,
        catatan: String?,
    ): DocumentResult = transactionRunner.transaction {
        val lama = goodsIssueDao.findById(id) ?: return@transaction DocumentResult.NotFound
        val sekarang = lama.issue.status
        if (!sekarang.bolehPindahKe(DocumentStatus.VALIDATED)) {
            return@transaction DocumentResult.InvalidTransition(sekarang, DocumentStatus.VALIDATED)
        }

        goodsIssueDao.updateStatus(
            id = id,
            status = DocumentStatus.VALIDATED,
            approvedBy = approver.id,
            approvedAt = waktu(),
            catatan = catatan ?: lama.issue.catatan,
        )
        DocumentResult.Success(id, DocumentStatus.VALIDATED)
    }

    override suspend fun postGoodsIssue(id: String, tanggal: Long): DocumentResult =
        transactionRunner.transaction {
            val lama = goodsIssueDao.findById(id) ?: return@transaction DocumentResult.NotFound
            val sekarang = lama.issue.status
            if (!sekarang.bolehPindahKe(DocumentStatus.POSTED)) {
                return@transaction DocumentResult.InvalidTransition(sekarang, DocumentStatus.POSTED)
            }

            val kebutuhan = lama.details
                .groupBy { baris -> baris.detail.sku to baris.detail.locationKode }
                .mapValues { (_, baris) -> baris.sumOf { it.detail.qty } }

            for ((kunci, diminta) in kebutuhan) {
                val (sku, kodeLokasi) = kunci
                val tersedia = stockDao.find(sku, kodeLokasi)?.jumlahStok ?: 0
                if (tersedia < diminta) {
                    return@transaction DocumentResult.InsufficientStock(sku, tersedia, diminta)
                }
            }

            lama.details.forEach { baris ->
                val sku = baris.detail.sku
                val kodeLokasi = baris.detail.locationKode
                val stokLama = stockDao.find(sku, kodeLokasi)
                    ?: error("Baris stok $sku di $kodeLokasi hilang")

                stockDao.upsert(stokLama.copy(jumlahStok = stokLama.jumlahStok - baris.detail.qty))
                movementDao.insert(
                    StockMovementEntity(
                        id = "MOV-${movementDao.count() + 1}",
                        sku = sku,
                        locationKode = kodeLokasi,
                        tipe = MovementType.OUTBOUND,
                        qty = baris.detail.qty,
                        tanggal = tanggal,
                        referensi = lama.issue.noIssue,
                        operatorId = lama.issue.operatorId,
                    ),
                )
            }

            goodsIssueDao.updateStatus(
                id = id,
                status = DocumentStatus.POSTED,
                approvedBy = lama.issue.approvedBy,
                approvedAt = lama.issue.approvedAt,
                catatan = lama.issue.catatan,
            )
            DocumentResult.Success(id, DocumentStatus.POSTED)
        }

    override suspend fun cancelGoodsIssue(id: String, catatan: String?): DocumentResult =
        transactionRunner.transaction {
            val lama = goodsIssueDao.findById(id) ?: return@transaction DocumentResult.NotFound
            val sekarang = lama.issue.status
            if (!sekarang.bolehPindahKe(DocumentStatus.CANCELLED)) {
                return@transaction DocumentResult.InvalidTransition(sekarang, DocumentStatus.CANCELLED)
            }

            goodsIssueDao.updateStatus(
                id = id,
                status = DocumentStatus.CANCELLED,
                approvedBy = lama.issue.approvedBy,
                approvedAt = lama.issue.approvedAt,
                catatan = catatan ?: lama.issue.catatan,
            )
            DocumentResult.Success(id, DocumentStatus.CANCELLED)
        }
}
