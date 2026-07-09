package com.utb.wms.domain.repository

import com.utb.wms.domain.model.Item
import com.utb.wms.domain.model.Location
import com.utb.wms.domain.model.Stock
import com.utb.wms.domain.model.StockMovement
import com.utb.wms.domain.model.User
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {

    fun observeStocks(): Flow<List<Stock>>

    fun observeMovements(): Flow<List<StockMovement>>

    fun observeMovementsBySku(sku: String): Flow<List<StockMovement>>

    suspend fun totalStock(sku: String): Int

    suspend fun stockAt(sku: String, locationCode: String): Int

    suspend fun adjustStock(
        item: Item,
        location: Location,
        jumlahBaru: Int,
        alasan: String,
        operator: User,
    )
}
