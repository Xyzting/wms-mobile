package com.utb.wms.data.repository

import com.utb.wms.data.local.TransactionRunner
import com.utb.wms.data.local.dao.GoodsIssueDao
import com.utb.wms.data.local.dao.MovementDao
import com.utb.wms.data.local.dao.StockDao
import com.utb.wms.data.local.entity.GoodsIssueDetailEntity
import com.utb.wms.data.local.entity.GoodsIssueEntity
import com.utb.wms.data.local.entity.StockMovementEntity
import com.utb.wms.domain.model.DocumentStatus
import com.utb.wms.domain.model.GoodsIssue
import com.utb.wms.domain.model.GoodsIssueDetail
import com.utb.wms.domain.model.MovementType
import com.utb.wms.domain.model.User
import com.utb.wms.domain.repository.DocumentResult
import com.utb.wms.domain.repository.OutboundRepository
import com.utb.wms.domain.repository.OutboundResult
import kotlinx.coroutines.flow.Flow

class OutboundRepositoryImpl(
    private val transactionRunner: TransactionRunner,
    private val goodsIssueDao: GoodsIssueDao,
    private val stockDao: StockDao,
    private val movementDao: MovementDao,
) : OutboundRepository {

    override suspend fun createGoodsIssue(
        tujuan: String,
        operator: User,
        details: List<GoodsIssueDetail>,
        tanggal: Long,
    ): OutboundResult {
        require(details.isNotEmpty()) { "Detail pengeluaran tidak boleh kosong" }
        require(details.all { it.qty > 0 }) { "Kuantitas harus lebih dari nol" }

        return transactionRunner.transaction {
            val kebutuhan = details
                .groupBy { detail -> detail.item.sku to detail.location.kode }
                .mapValues { (_, baris) -> baris.sumOf { it.qty } }

            for ((kunci, diminta) in kebutuhan) {
                val (sku, kodeLokasi) = kunci
                val tersedia = stockDao.find(sku, kodeLokasi)?.jumlahStok ?: 0
                if (tersedia < diminta) {
                    return@transaction OutboundResult.InsufficientStock(
                        sku = sku,
                        tersedia = tersedia,
                        diminta = diminta,
                    )
                }
            }

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
                    status = DocumentStatus.POSTED,
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

            details.forEach { detail ->
                val lama = stockDao.find(detail.item.sku, detail.location.kode)
                    ?: error("Baris stok ${detail.item.sku} di ${detail.location.kode} hilang")
                stockDao.upsert(lama.copy(jumlahStok = lama.jumlahStok - detail.qty))
                movementDao.insert(
                    StockMovementEntity(
                        id = "MOV-${movementDao.count() + 1}",
                        sku = detail.item.sku,
                        locationKode = detail.location.kode,
                        tipe = MovementType.OUTBOUND,
                        qty = detail.qty,
                        tanggal = tanggal,
                        referensi = noIssue,
                    ),
                )
            }

            OutboundResult.Success(
                GoodsIssue(
                    id = id,
                    noIssue = noIssue,
                    tanggal = tanggal,
                    tujuan = tujuan,
                    operator = operator,
                    status = DocumentStatus.POSTED,
                    details = details,
                ),
            )
        }
    }

    override fun observeGoodsIssues(): Flow<List<GoodsIssue>> =
        TODO("BE-2: pakai goodsIssueDao.observeIssues()")

    override fun observeGoodsIssuesByStatus(status: DocumentStatus): Flow<List<GoodsIssue>> =
        TODO("BE-2: pakai goodsIssueDao.observeIssuesByStatus(status)")

    override suspend fun findGoodsIssue(id: String): GoodsIssue? =
        TODO("BE-2: pakai goodsIssueDao.findById(id)")

    override suspend fun validateGoodsIssue(
        id: String,
        approver: User,
        catatan: String?,
    ): DocumentResult = TODO(
        "BE-2: cermin dari validateGoodsReceipt. Kecukupan stok belum diperiksa di sini.",
    )

    override suspend fun postGoodsIssue(id: String, tanggal: Long): DocumentResult = TODO(
        "BE-2: hanya dari VALIDATED. Periksa kecukupan stok tiap pasangan sku dan lokasi " +
            "lebih dulu, kembalikan InsufficientStock tanpa menulis apa pun bila kurang. " +
            "Bila cukup, kurangi stok, catat StockMovement bertipe OUTBOUND, " +
            "lalu ubah status menjadi POSTED.",
    )

    override suspend fun cancelGoodsIssue(id: String, catatan: String?): DocumentResult = TODO(
        "BE-2: cermin dari cancelGoodsReceipt.",
    )
}
