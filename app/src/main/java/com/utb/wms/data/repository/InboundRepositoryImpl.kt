package com.utb.wms.data.repository

import com.utb.wms.data.local.TransactionRunner
import com.utb.wms.data.local.dao.GoodsReceiptDao
import com.utb.wms.data.local.dao.MovementDao
import com.utb.wms.data.local.dao.StockDao
import com.utb.wms.data.local.entity.GoodsReceiptDetailEntity
import com.utb.wms.data.local.entity.GoodsReceiptEntity
import com.utb.wms.data.local.entity.StockEntity
import com.utb.wms.data.local.entity.StockMovementEntity
import com.utb.wms.domain.model.DocumentStatus
import com.utb.wms.domain.model.GoodsReceipt
import com.utb.wms.domain.model.GoodsReceiptDetail
import com.utb.wms.domain.model.MovementType
import com.utb.wms.domain.model.Supplier
import com.utb.wms.domain.model.User
import com.utb.wms.domain.repository.InboundRepository

class InboundRepositoryImpl(
    private val transactionRunner: TransactionRunner,
    private val goodsReceiptDao: GoodsReceiptDao,
    private val stockDao: StockDao,
    private val movementDao: MovementDao,
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
                    status = DocumentStatus.POSTED,
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

            details.forEach { detail ->
                val lama = stockDao.find(detail.item.sku, detail.location.kode)
                stockDao.upsert(
                    StockEntity(
                        id = lama?.id ?: "STK-${stockDao.count() + 1}",
                        sku = detail.item.sku,
                        locationKode = detail.location.kode,
                        jumlahStok = (lama?.jumlahStok ?: 0) + detail.qty,
                    ),
                )
                movementDao.insert(
                    StockMovementEntity(
                        id = "MOV-${movementDao.count() + 1}",
                        sku = detail.item.sku,
                        locationKode = detail.location.kode,
                        tipe = MovementType.INBOUND,
                        qty = detail.qty,
                        tanggal = tanggal,
                        referensi = noReceipt,
                    ),
                )
            }

            GoodsReceipt(
                id = id,
                noReceipt = noReceipt,
                tanggal = tanggal,
                supplier = supplier,
                operator = operator,
                status = DocumentStatus.POSTED,
                details = details,
            )
        }
    }
}
