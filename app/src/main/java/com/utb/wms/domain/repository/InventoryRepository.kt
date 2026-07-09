package com.utb.wms.domain.repository

import com.utb.wms.domain.model.Stock
import com.utb.wms.domain.model.StockMovement
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {

    fun observeStocks(): Flow<List<Stock>>

    fun observeMovements(): Flow<List<StockMovement>>

    suspend fun totalStock(sku: String): Int

    suspend fun stockAt(sku: String, locationCode: String): Int
}
