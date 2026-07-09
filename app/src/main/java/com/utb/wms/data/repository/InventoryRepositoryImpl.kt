package com.utb.wms.data.repository

import com.utb.wms.data.local.TransactionRunner
import com.utb.wms.data.local.dao.MovementDao
import com.utb.wms.data.local.dao.StockDao
import com.utb.wms.data.local.mapper.toDomain
import com.utb.wms.domain.model.Item
import com.utb.wms.domain.model.Location
import com.utb.wms.domain.model.Stock
import com.utb.wms.domain.model.StockMovement
import com.utb.wms.domain.model.User
import com.utb.wms.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InventoryRepositoryImpl(
    private val transactionRunner: TransactionRunner,
    private val stockDao: StockDao,
    private val movementDao: MovementDao,
) : InventoryRepository {

    override fun observeStocks(): Flow<List<Stock>> =
        stockDao.observeStocks().map { daftar -> daftar.map { it.toDomain() } }

    override fun observeMovements(): Flow<List<StockMovement>> =
        movementDao.observeMovements().map { daftar -> daftar.map { it.toDomain() } }

    override fun observeMovementsBySku(sku: String): Flow<List<StockMovement>> =
        TODO("BE-2: pakai movementDao.observeMovementsBySku(sku)")

    override suspend fun totalStock(sku: String): Int =
        stockDao.totalStock(sku)

    override suspend fun stockAt(sku: String, locationCode: String): Int =
        stockDao.find(sku, locationCode)?.jumlahStok ?: 0

    override suspend fun adjustStock(
        item: Item,
        location: Location,
        jumlahBaru: Int,
        alasan: String,
        operator: User,
    ) {
        TODO(
            "BE-2: dalam satu transaksi, hitung selisih terhadap stok lama, " +
                "upsert baris stok, lalu catat satu StockMovement bertipe ADJUSTMENT " +
                "dengan qty berupa nilai mutlak selisih, keterangan = alasan, " +
                "operatorId = operator.id, dan referensi berupa nomor penyesuaian. " +
                "Selisih nol tidak menghasilkan pergerakan.",
        )
    }
}
