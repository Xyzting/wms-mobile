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
import com.utb.wms.data.local.relation.GoodsIssueWithRefs
import com.utb.wms.data.local.relation.GoodsReceiptWithRefs
import com.utb.wms.data.local.relation.StockMovementWithRefs
import com.utb.wms.data.local.relation.StockWithRefs
import com.utb.wms.data.local.relation.UserWithRole
import com.utb.wms.domain.model.DocumentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Transaction
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): UserWithRole?

    @Transaction
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): UserWithRole?

    @Transaction
    @Query("SELECT * FROM users ORDER BY nama")
    fun observeUsers(): Flow<List<UserWithRole>>

    @Query("SELECT * FROM roles ORDER BY namaRole")
    fun observeRoles(): Flow<List<RoleEntity>>

    @Query("SELECT COUNT(*) FROM users WHERE roleId = :roleId")
    suspend fun countUsersWithRole(roleId: String): Int

    @Query("UPDATE users SET aktif = :aktif WHERE id = :id")
    suspend fun updateAktif(id: String, aktif: Boolean)

    @Upsert
    suspend fun upsertUser(user: UserEntity)

    @Upsert
    suspend fun upsertRole(role: RoleEntity)

    @Query("DELETE FROM roles WHERE id = :id")
    suspend fun deleteRole(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoles(roles: List<RoleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)
}

@Dao
interface MasterDataDao {

    @Query("SELECT * FROM items WHERE isActive = 1 ORDER BY nama")
    fun observeItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items ORDER BY nama")
    fun observeAllItems(): Flow<List<ItemEntity>>

    @Query(
        "SELECT * FROM items WHERE isActive = 1 AND " +
            "(sku LIKE '%' || :kataKunci || '%' OR nama LIKE '%' || :kataKunci || '%') " +
            "ORDER BY nama",
    )
    fun searchItems(kataKunci: String): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE sku = :sku LIMIT 1")
    suspend fun findItem(sku: String): ItemEntity?

    @Query("SELECT * FROM items WHERE barcode = :barcode LIMIT 1")
    suspend fun findItemByBarcode(barcode: String): ItemEntity?

    @Upsert
    suspend fun upsertItem(item: ItemEntity)

    @Query("UPDATE items SET isActive = 0 WHERE sku = :sku")
    suspend fun nonaktifkanItem(sku: String)

    @Query("SELECT * FROM locations WHERE isActive = 1 ORDER BY kode")
    fun observeLocations(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locations ORDER BY kode")
    fun observeAllLocations(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locations WHERE kode = :kode LIMIT 1")
    suspend fun findLocation(kode: String): LocationEntity?

    @Upsert
    suspend fun upsertLocation(location: LocationEntity)

    @Query("UPDATE locations SET isActive = 0 WHERE kode = :kode")
    suspend fun nonaktifkanLocation(kode: String)

    @Query("SELECT * FROM suppliers WHERE isActive = 1 ORDER BY nama")
    fun observeSuppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers ORDER BY nama")
    fun observeAllSuppliers(): Flow<List<SupplierEntity>>

    @Upsert
    suspend fun upsertSupplier(supplier: SupplierEntity)

    @Query("UPDATE suppliers SET isActive = 0 WHERE id = :id")
    suspend fun nonaktifkanSupplier(id: String)

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

    @Transaction
    @Query("SELECT * FROM stocks ORDER BY sku, locationKode")
    suspend fun semuaStok(): List<StockWithRefs>

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

    @Transaction
    @Query("SELECT * FROM stock_movements WHERE sku = :sku ORDER BY tanggal DESC")
    fun observeMovementsBySku(sku: String): Flow<List<StockMovementWithRefs>>

    @Transaction
    @Query("SELECT * FROM stock_movements WHERE tanggal BETWEEN :dari AND :sampai ORDER BY tanggal")
    suspend fun mutasiRentang(dari: Long, sampai: Long): List<StockMovementWithRefs>

    @Query("SELECT COUNT(*) FROM stock_movements")
    suspend fun count(): Int

    @Insert
    suspend fun insert(movement: StockMovementEntity)
}

@Dao
interface GoodsReceiptDao {

    @Transaction
    @Query("SELECT * FROM goods_receipts ORDER BY tanggal DESC")
    fun observeReceipts(): Flow<List<GoodsReceiptWithRefs>>

    @Transaction
    @Query("SELECT * FROM goods_receipts WHERE status = :status ORDER BY tanggal DESC")
    fun observeReceiptsByStatus(status: DocumentStatus): Flow<List<GoodsReceiptWithRefs>>

    @Transaction
    @Query("SELECT * FROM goods_receipts WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): GoodsReceiptWithRefs?

    @Query("SELECT COUNT(*) FROM goods_receipts")
    suspend fun count(): Int

    @Query(
        "UPDATE goods_receipts SET status = :status, approvedBy = :approvedBy, " +
            "approvedAt = :approvedAt, catatan = :catatan WHERE id = :id",
    )
    suspend fun updateStatus(
        id: String,
        status: DocumentStatus,
        approvedBy: String?,
        approvedAt: Long?,
        catatan: String?,
    )

    @Insert
    suspend fun insertHeader(receipt: GoodsReceiptEntity)

    @Insert
    suspend fun insertDetails(details: List<GoodsReceiptDetailEntity>)
}

@Dao
interface GoodsIssueDao {

    @Transaction
    @Query("SELECT * FROM goods_issues ORDER BY tanggal DESC")
    fun observeIssues(): Flow<List<GoodsIssueWithRefs>>

    @Transaction
    @Query("SELECT * FROM goods_issues WHERE status = :status ORDER BY tanggal DESC")
    fun observeIssuesByStatus(status: DocumentStatus): Flow<List<GoodsIssueWithRefs>>

    @Transaction
    @Query("SELECT * FROM goods_issues WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): GoodsIssueWithRefs?

    @Query("SELECT COUNT(*) FROM goods_issues")
    suspend fun count(): Int

    @Query(
        "UPDATE goods_issues SET status = :status, approvedBy = :approvedBy, " +
            "approvedAt = :approvedAt, catatan = :catatan WHERE id = :id",
    )
    suspend fun updateStatus(
        id: String,
        status: DocumentStatus,
        approvedBy: String?,
        approvedAt: Long?,
        catatan: String?,
    )

    @Insert
    suspend fun insertHeader(issue: GoodsIssueEntity)

    @Insert
    suspend fun insertDetails(details: List<GoodsIssueDetailEntity>)
}
