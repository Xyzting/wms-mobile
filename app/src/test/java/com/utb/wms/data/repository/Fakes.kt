package com.utb.wms.data.repository

import com.utb.wms.data.local.TransactionRunner
import com.utb.wms.data.local.dao.GoodsIssueDao
import com.utb.wms.data.local.dao.GoodsReceiptDao
import com.utb.wms.data.local.dao.MovementDao
import com.utb.wms.data.local.dao.StockDao
import com.utb.wms.data.local.entity.GoodsIssueDetailEntity
import com.utb.wms.data.local.entity.GoodsIssueEntity
import com.utb.wms.data.local.entity.GoodsReceiptDetailEntity
import com.utb.wms.data.local.entity.GoodsReceiptEntity
import com.utb.wms.data.local.entity.StockEntity
import com.utb.wms.data.local.entity.StockMovementEntity
import com.utb.wms.data.local.relation.StockMovementWithRefs
import com.utb.wms.data.local.relation.StockWithRefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeTransactionRunner : TransactionRunner {

    override suspend fun <R> transaction(block: suspend () -> R): R = block()
}

class FakeStockDao(stokAwal: List<StockEntity> = emptyList()) : StockDao {

    val baris = stokAwal.toMutableList()

    override fun observeStocks(): Flow<List<StockWithRefs>> = flowOf(emptyList())

    override suspend fun find(sku: String, locationKode: String): StockEntity? =
        baris.firstOrNull { it.sku == sku && it.locationKode == locationKode }

    override suspend fun totalStock(sku: String): Int =
        baris.filter { it.sku == sku }.sumOf { it.jumlahStok }

    override suspend fun count(): Int = baris.size

    override suspend fun upsert(stock: StockEntity) {
        val posisi = baris.indexOfFirst { it.id == stock.id }
        if (posisi >= 0) baris[posisi] = stock else baris.add(stock)
    }

    override suspend fun insertAll(stocks: List<StockEntity>) {
        stocks.forEach { upsert(it) }
    }
}

class FakeMovementDao : MovementDao {

    val baris = mutableListOf<StockMovementEntity>()

    override fun observeMovements(): Flow<List<StockMovementWithRefs>> = flowOf(emptyList())

    override suspend fun count(): Int = baris.size

    override suspend fun insert(movement: StockMovementEntity) {
        baris.add(movement)
    }
}

class FakeGoodsReceiptDao : GoodsReceiptDao {

    val header = mutableListOf<GoodsReceiptEntity>()
    val detail = mutableListOf<GoodsReceiptDetailEntity>()

    override suspend fun count(): Int = header.size

    override suspend fun insertHeader(receipt: GoodsReceiptEntity) {
        header.add(receipt)
    }

    override suspend fun insertDetails(details: List<GoodsReceiptDetailEntity>) {
        detail.addAll(details)
    }
}

class FakeGoodsIssueDao : GoodsIssueDao {

    val header = mutableListOf<GoodsIssueEntity>()
    val detail = mutableListOf<GoodsIssueDetailEntity>()

    override suspend fun count(): Int = header.size

    override suspend fun insertHeader(issue: GoodsIssueEntity) {
        header.add(issue)
    }

    override suspend fun insertDetails(details: List<GoodsIssueDetailEntity>) {
        detail.addAll(details)
    }
}
