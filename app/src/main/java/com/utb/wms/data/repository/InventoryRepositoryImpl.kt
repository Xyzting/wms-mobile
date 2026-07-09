package com.utb.wms.data.repository

import com.utb.wms.data.local.dao.MovementDao
import com.utb.wms.data.local.dao.StockDao
import com.utb.wms.domain.model.Stock
import com.utb.wms.domain.model.StockMovement
import com.utb.wms.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow

class InventoryRepositoryImpl(
    private val stockDao: StockDao,
    private val movementDao: MovementDao,
) : InventoryRepository {

    override fun observeStocks(): Flow<List<Stock>> {
        TODO("BE-2: petakan StockWithRefs ke Stock")
    }

    override fun observeMovements(): Flow<List<StockMovement>> {
        TODO("BE-2: petakan StockMovementWithRefs ke StockMovement")
    }

    override suspend fun totalStock(sku: String): Int {
        TODO("BE-2: jumlah stok SKU ini di seluruh lokasi")
    }

    override suspend fun stockAt(sku: String, locationCode: String): Int {
        TODO("BE-2: stok SKU ini di satu lokasi, 0 bila baris belum ada")
    }
}
