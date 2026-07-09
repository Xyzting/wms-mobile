package com.utb.wms.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.utb.wms.data.local.entity.GoodsIssueDetailEntity
import com.utb.wms.data.local.entity.GoodsIssueEntity
import com.utb.wms.data.local.entity.GoodsReceiptDetailEntity
import com.utb.wms.data.local.entity.GoodsReceiptEntity
import com.utb.wms.data.local.entity.ItemEntity
import com.utb.wms.data.local.entity.LocationEntity
import com.utb.wms.data.local.entity.RoleEntity
import com.utb.wms.data.local.entity.StockEntity
import com.utb.wms.data.local.entity.StockMovementEntity
import com.utb.wms.data.local.entity.SupplierEntity
import com.utb.wms.data.local.entity.UserEntity
import com.utb.wms.data.local.relation.StockMovementWithRefs
import com.utb.wms.data.local.relation.StockWithRefs
import com.utb.wms.data.local.relation.UserWithRole
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Transaction
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): UserWithRole?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoles(roles: List<RoleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)
}

@Dao
interface MasterDataDao {

    @Query("SELECT * FROM items ORDER BY nama")
    fun observeItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM locations ORDER BY kode")
    fun observeLocations(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM suppliers WHERE isActive = 1 ORDER BY nama")
    fun observeSuppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT COUNT(*) FROM items")
    suspend fun countItems(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<LocationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuppliers(suppliers: List<SupplierEntity>)
}

@Dao
interface StockDao {

    @Transaction
    @Query("SELECT * FROM stocks ORDER BY sku, locationKode")
    fun observeStocks(): Flow<List<StockWithRefs>>

    @Query("SELECT * FROM stocks WHERE sku = :sku AND locationKode = :locationKode LIMIT 1")
    suspend fun find(sku: String, locationKode: String): StockEntity?

    @Query("SELECT COALESCE(SUM(jumlahStok), 0) FROM stocks WHERE sku = :sku")
    suspend fun totalStock(sku: String): Int

    @Query("SELECT COUNT(*) FROM stocks")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(stock: StockEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stocks: List<StockEntity>)
}

@Dao
interface MovementDao {

    @Transaction
    @Query("SELECT * FROM stock_movements ORDER BY tanggal DESC")
    fun observeMovements(): Flow<List<StockMovementWithRefs>>

    @Query("SELECT COUNT(*) FROM stock_movements")
    suspend fun count(): Int

    @Insert
    suspend fun insert(movement: StockMovementEntity)
}

@Dao
interface GoodsReceiptDao {

    @Query("SELECT COUNT(*) FROM goods_receipts")
    suspend fun count(): Int

    @Insert
    suspend fun insertHeader(receipt: GoodsReceiptEntity)

    @Insert
    suspend fun insertDetails(details: List<GoodsReceiptDetailEntity>)
}

@Dao
interface GoodsIssueDao {

    @Query("SELECT COUNT(*) FROM goods_issues")
    suspend fun count(): Int

    @Insert
    suspend fun insertHeader(issue: GoodsIssueEntity)

    @Insert
    suspend fun insertDetails(details: List<GoodsIssueDetailEntity>)
}
