package com.utb.wms.data.repository

import com.utb.wms.data.local.TransactionRunner
import com.utb.wms.data.local.dao.MovementDao
import com.utb.wms.data.local.dao.StockDao
import com.utb.wms.data.local.entity.StockEntity
import com.utb.wms.data.local.entity.StockMovementEntity
import com.utb.wms.data.local.mapper.toDomain
import com.utb.wms.domain.model.Item
import com.utb.wms.domain.model.Location
import com.utb.wms.domain.model.MovementType
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
    private val waktu: () -> Long = { System.currentTimeMillis() },
) : InventoryRepository {

    override fun observeStocks(): Flow<List<Stock>> =
        stockDao.observeStocks().map { daftar -> daftar.map { it.toDomain() } }

    override fun observeMovements(): Flow<List<StockMovement>> =
        movementDao.observeMovements().map { daftar -> daftar.map { it.toDomain() } }

    override fun observeMovementsBySku(sku: String): Flow<List<StockMovement>> =
        movementDao.observeMovementsBySku(sku).map { daftar -> daftar.map { it.toDomain() } }

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
        require(jumlahBaru >= 0) { "Jumlah stok baru tidak boleh negatif" }
        require(alasan.isNotBlank()) { "Alasan penyesuaian wajib diisi" }

        transactionRunner.transaction {
            val stokLama = stockDao.find(item.sku, location.kode)
            val jumlahLama = stokLama?.jumlahStok ?: 0
            val selisih = jumlahBaru - jumlahLama
            if (selisih == 0) return@transaction

            stockDao.upsert(
                StockEntity(
                    id = stokLama?.id ?: "STK-${stockDao.count() + 1}",
                    sku = item.sku,
                    locationKode = location.kode,
                    jumlahStok = jumlahBaru,
                ),
            )

            val urut = movementDao.count() + 1
            movementDao.insert(
                StockMovementEntity(
                    id = "MOV-$urut",
                    sku = item.sku,
                    locationKode = location.kode,
                    tipe = MovementType.ADJUSTMENT,
                    qty = selisih,
                    tanggal = waktu(),
                    referensi = "ADJ-%04d".format(urut),
                    keterangan = alasan,
                    operatorId = operator.id,
                ),
            )
        }
    }
}
