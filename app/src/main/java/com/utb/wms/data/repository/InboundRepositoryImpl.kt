package com.utb.wms.data.repository

import com.utb.wms.data.local.TransactionRunner
import com.utb.wms.data.local.dao.GoodsReceiptDao
import com.utb.wms.data.local.dao.MovementDao
import com.utb.wms.data.local.dao.StockDao
import com.utb.wms.domain.model.GoodsReceipt
import com.utb.wms.domain.model.GoodsReceiptDetail
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
        TODO("BE-2: validasi detail, lalu tulis header + detail + stok + movement dalam satu transaksi")
    }
}
