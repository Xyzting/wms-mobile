package com.utb.wms.data.repository

import com.utb.wms.data.local.dao.MovementDao
import com.utb.wms.data.local.dao.StockDao
import com.utb.wms.data.local.mapper.toDomain
import com.utb.wms.domain.model.Stock
import com.utb.wms.domain.model.StockMovement
import com.utb.wms.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InventoryRepositoryImpl(
    private val stockDao: StockDao,
    private val movementDao: MovementDao,
) : InventoryRepository {

    override fun observeStocks(): Flow<List<Stock>> =
        stockDao.observeStocks().map { daftar -> daftar.map { it.toDomain() } }

    override fun observeMovements(): Flow<List<StockMovement>> =
        movementDao.observeMovements().map { daftar -> daftar.map { it.toDomain() } }

    override suspend fun totalStock(sku: String): Int =
        stockDao.totalStock(sku)

    override suspend fun stockAt(sku: String, locationCode: String): Int =
        stockDao.find(sku, locationCode)?.jumlahStok ?: 0
}
