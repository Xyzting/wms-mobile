package com.utb.wms.data.repository

import com.utb.wms.data.local.TransactionRunner
import com.utb.wms.data.local.dao.GoodsIssueDao
import com.utb.wms.data.local.dao.MovementDao
import com.utb.wms.data.local.dao.StockDao
import com.utb.wms.domain.model.GoodsIssueDetail
import com.utb.wms.domain.model.User
import com.utb.wms.domain.repository.OutboundRepository
import com.utb.wms.domain.repository.OutboundResult

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
        TODO("BE-2: periksa stok semua baris dulu, baru menulis; kurang stok = batal tanpa tulisan")
    }
}
