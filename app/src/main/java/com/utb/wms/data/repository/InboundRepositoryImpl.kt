package com.utb.wms.data.repository

import com.utb.wms.data.local.TransactionRunner
import com.utb.wms.data.local.dao.GoodsReceiptDao
import com.utb.wms.data.local.dao.MovementDao
import com.utb.wms.data.local.dao.StockDao
import com.utb.wms.data.local.entity.GoodsReceiptDetailEntity
import com.utb.wms.data.local.entity.GoodsReceiptEntity
import com.utb.wms.data.local.entity.StockEntity
import com.utb.wms.data.local.entity.StockMovementEntity
import com.utb.wms.data.local.mapper.toDomain
import com.utb.wms.domain.model.DocumentStatus
import com.utb.wms.domain.model.GoodsReceipt
import com.utb.wms.domain.model.GoodsReceiptDetail
import com.utb.wms.domain.model.MovementType
import com.utb.wms.domain.model.Supplier
import com.utb.wms.domain.model.User
import com.utb.wms.domain.model.bolehPindahKe
import com.utb.wms.domain.repository.DocumentResult
import com.utb.wms.domain.repository.InboundRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InboundRepositoryImpl(
    private val transactionRunner: TransactionRunner,
    private val goodsReceiptDao: GoodsReceiptDao,
    private val stockDao: StockDao,
    private val movementDao: MovementDao,
    private val waktu: () -> Long = { System.currentTimeMillis() },
) : InboundRepository {

    override suspend fun createGoodsReceipt(
        supplier: Supplier,
        operator: User,
        details: List<GoodsReceiptDetail>,
        tanggal: Long,
    ): GoodsReceipt {
        require(details.isNotEmpty()) { "Detail penerimaan tidak boleh kosong" }
        require(details.all { it.qty > 0 }) { "Kuantitas harus lebih dari nol" }

        return transactionRunner.transaction {
            val urut = goodsReceiptDao.count() + 1
            val id = "GR-$urut"
            val noReceipt = "GR-%04d".format(urut)

            goodsReceiptDao.insertHeader(
                GoodsReceiptEntity(
                    id = id,
                    noReceipt = noReceipt,
                    tanggal = tanggal,
                    supplierId = supplier.id,
                    operatorId = operator.id,
                    status = DocumentStatus.DRAFT,
                ),
            )
            goodsReceiptDao.insertDetails(
                details.map { detail ->
                    GoodsReceiptDetailEntity(
                        receiptId = id,
                        sku = detail.item.sku,
                        locationKode = detail.location.kode,
                        qty = detail.qty,
                    )
                },
            )

            GoodsReceipt(
                id = id,
                noReceipt = noReceipt,
                tanggal = tanggal,
                supplier = supplier,
                operator = operator,
                status = DocumentStatus.DRAFT,
                details = details,
            )
        }
    }

    override fun observeGoodsReceipts(): Flow<List<GoodsReceipt>> =
        goodsReceiptDao.observeReceipts().map { daftar -> daftar.map { it.toDomain() } }

    override fun observeGoodsReceiptsByStatus(status: DocumentStatus): Flow<List<GoodsReceipt>> =
        goodsReceiptDao.observeReceiptsByStatus(status).map { daftar -> daftar.map { it.toDomain() } }

    override suspend fun findGoodsReceipt(id: String): GoodsReceipt? =
        goodsReceiptDao.findById(id)?.toDomain()

    override suspend fun validateGoodsReceipt(
        id: String,
        approver: User,
        catatan: String?,
    ): DocumentResult = transactionRunner.transaction {
        val lama = goodsReceiptDao.findById(id) ?: return@transaction DocumentResult.NotFound
        val sekarang = lama.receipt.status
        if (!sekarang.bolehPindahKe(DocumentStatus.VALIDATED)) {
            return@transaction DocumentResult.InvalidTransition(sekarang, DocumentStatus.VALIDATED)
        }

        goodsReceiptDao.updateStatus(
            id = id,
            status = DocumentStatus.VALIDATED,
            approvedBy = approver.id,
            approvedAt = waktu(),
            catatan = catatan ?: lama.receipt.catatan,
        )
        DocumentResult.Success(id, DocumentStatus.VALIDATED)
    }

    override suspend fun postGoodsReceipt(id: String, tanggal: Long): DocumentResult =
        transactionRunner.transaction {
            val lama = goodsReceiptDao.findById(id) ?: return@transaction DocumentResult.NotFound
            val sekarang = lama.receipt.status
            if (!sekarang.bolehPindahKe(DocumentStatus.POSTED)) {
                return@transaction DocumentResult.InvalidTransition(sekarang, DocumentStatus.POSTED)
            }

            lama.details.forEach { baris ->
                val sku = baris.detail.sku
                val kodeLokasi = baris.detail.locationKode
                val stokLama = stockDao.find(sku, kodeLokasi)

                stockDao.upsert(
                    StockEntity(
                        id = stokLama?.id ?: "STK-${stockDao.count() + 1}",
                        sku = sku,
                        locationKode = kodeLokasi,
                        jumlahStok = (stokLama?.jumlahStok ?: 0) + baris.detail.qty,
                    ),
                )
                movementDao.insert(
                    StockMovementEntity(
                        id = "MOV-${movementDao.count() + 1}",
                        sku = sku,
                        locationKode = kodeLokasi,
                        tipe = MovementType.INBOUND,
                        qty = baris.detail.qty,
                        tanggal = tanggal,
                        referensi = lama.receipt.noReceipt,
                        operatorId = lama.receipt.operatorId,
                    ),
                )
            }

            goodsReceiptDao.updateStatus(
                id = id,
                status = DocumentStatus.POSTED,
                approvedBy = lama.receipt.approvedBy,
                approvedAt = lama.receipt.approvedAt,
                catatan = lama.receipt.catatan,
            )
            DocumentResult.Success(id, DocumentStatus.POSTED)
        }

    override suspend fun cancelGoodsReceipt(id: String, catatan: String?): DocumentResult =
        transactionRunner.transaction {
            val lama = goodsReceiptDao.findById(id) ?: return@transaction DocumentResult.NotFound
            val sekarang = lama.receipt.status
            if (!sekarang.bolehPindahKe(DocumentStatus.CANCELLED)) {
                return@transaction DocumentResult.InvalidTransition(sekarang, DocumentStatus.CANCELLED)
            }

            goodsReceiptDao.updateStatus(
                id = id,
                status = DocumentStatus.CANCELLED,
                approvedBy = lama.receipt.approvedBy,
                approvedAt = lama.receipt.approvedAt,
                catatan = catatan ?: lama.receipt.catatan,
            )
            DocumentResult.Success(id, DocumentStatus.CANCELLED)
        }
}
