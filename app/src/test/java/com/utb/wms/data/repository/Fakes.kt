package com.utb.wms.data.repository

import com.utb.wms.data.local.TransactionRunner
import com.utb.wms.data.local.dao.GoodsIssueDao
import com.utb.wms.data.local.dao.GoodsReceiptDao
import com.utb.wms.data.local.dao.MovementDao
import com.utb.wms.data.local.dao.StockDao
import com.utb.wms.data.local.dao.UserDao
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
import com.utb.wms.data.local.mapper.toDomain
import com.utb.wms.data.local.relation.GoodsIssueDetailWithRefs
import com.utb.wms.data.local.relation.GoodsIssueWithRefs
import com.utb.wms.data.local.relation.GoodsReceiptDetailWithRefs
import com.utb.wms.data.local.relation.GoodsReceiptWithRefs
import com.utb.wms.data.local.relation.StockMovementWithRefs
import com.utb.wms.data.local.relation.StockWithRefs
import com.utb.wms.data.local.relation.UserWithRole
import com.utb.wms.domain.model.DocumentStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

object Contoh {

    const val TANGGAL = 1_720_000_000_000L
    const val WAKTU_SETUJU = 1_720_000_500_000L

    val jam: () -> Long = { WAKTU_SETUJU }

    val roleAdmin = RoleEntity("R-01", "Admin")
    val roleOperator = RoleEntity("R-02", "Operator")
    val roleSupervisor = RoleEntity("R-03", "Supervisor")

    val userOperator = UserWithRole(
        UserEntity("U-02", "operator", "operator123", "Budi Santoso", "R-02", true),
        roleOperator,
    )
    val userSupervisor = UserWithRole(
        UserEntity("U-03", "supervisor", "supervisor123", "Siti Rahma", "R-03", true),
        roleSupervisor,
    )

    val supplierEntity = SupplierEntity("SUP-001", "PT Sumber Makmur", "0215550001", true)
    val itemEntity = ItemEntity("BOLT-M8-30", "Bolt M8x30mm", "pcs", 50, "8991010000011", true)
    val itemKeduaEntity = ItemEntity("NUT-M8", "Mur M8", "pcs", 50, "8991010000028", true)
    val lokasiEntity = LocationEntity("A-01", "Rak A-01", 500, true)
    val lokasiKeduaEntity = LocationEntity("A-02", "Rak A-02", 500, true)

    val roles = listOf(roleAdmin, roleOperator, roleSupervisor)
    val users = listOf(userOperator, userSupervisor)
    val suppliers = listOf(supplierEntity)
    val items = listOf(itemEntity, itemKeduaEntity)
    val lokasiSemua = listOf(lokasiEntity, lokasiKeduaEntity)

    val supplier = supplierEntity.toDomain()
    val item = itemEntity.toDomain()
    val itemKedua = itemKeduaEntity.toDomain()
    val lokasi = lokasiEntity.toDomain()
    val lokasiKedua = lokasiKeduaEntity.toDomain()
    val operator = userOperator.toDomain()
    val supervisor = userSupervisor.toDomain()

    fun item(sku: String): ItemEntity = items.first { it.sku == sku }

    fun lokasi(kode: String): LocationEntity = lokasiSemua.first { it.kode == kode }

    fun user(id: String): UserWithRole = users.first { it.user.id == id }

    fun supplier(id: String): SupplierEntity = suppliers.first { it.id == id }
}

class FakeTransactionRunner : TransactionRunner {

    override suspend fun <R> transaction(block: suspend () -> R): R = block()
}

class FakeStockDao(stokAwal: List<StockEntity> = emptyList()) : StockDao {

    val baris = stokAwal.toMutableList()

    override fun observeStocks(): Flow<List<StockWithRefs>> = flowOf(semuaWithRefs())

    override suspend fun semuaStok(): List<StockWithRefs> = semuaWithRefs()

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

    private fun semuaWithRefs(): List<StockWithRefs> = baris.map { stok ->
        StockWithRefs(stok, Contoh.item(stok.sku), Contoh.lokasi(stok.locationKode))
    }
}

class FakeMovementDao : MovementDao {

    val baris = mutableListOf<StockMovementEntity>()

    override fun observeMovements(): Flow<List<StockMovementWithRefs>> =
        flowOf(baris.map { it.withRefs() })

    override fun observeMovementsBySku(sku: String): Flow<List<StockMovementWithRefs>> =
        flowOf(baris.filter { it.sku == sku }.map { it.withRefs() })

    override suspend fun mutasiRentang(dari: Long, sampai: Long): List<StockMovementWithRefs> =
        baris.filter { it.tanggal in dari..sampai }.map { it.withRefs() }

    override suspend fun count(): Int = baris.size

    override suspend fun insert(movement: StockMovementEntity) {
        baris.add(movement)
    }

    private fun StockMovementEntity.withRefs(): StockMovementWithRefs = StockMovementWithRefs(
        movement = this,
        item = Contoh.item(sku),
        location = Contoh.lokasi(locationKode),
        operator = operatorId?.let { Contoh.user(it) },
    )
}

class FakeGoodsReceiptDao : GoodsReceiptDao {

    val header = mutableListOf<GoodsReceiptEntity>()
    val detail = mutableListOf<GoodsReceiptDetailEntity>()

    override fun observeReceipts(): Flow<List<GoodsReceiptWithRefs>> =
        flowOf(header.map { it.withRefs() })

    override fun observeReceiptsByStatus(
        status: DocumentStatus,
    ): Flow<List<GoodsReceiptWithRefs>> =
        flowOf(header.filter { it.status == status }.map { it.withRefs() })

    override suspend fun findById(id: String): GoodsReceiptWithRefs? =
        header.firstOrNull { it.id == id }?.withRefs()

    override suspend fun count(): Int = header.size

    override suspend fun updateStatus(
        id: String,
        status: DocumentStatus,
        approvedBy: String?,
        approvedAt: Long?,
        catatan: String?,
    ) {
        val posisi = header.indexOfFirst { it.id == id }
        if (posisi < 0) return
        header[posisi] = header[posisi].copy(
            status = status,
            approvedBy = approvedBy,
            approvedAt = approvedAt,
            catatan = catatan,
        )
    }

    override suspend fun insertHeader(receipt: GoodsReceiptEntity) {
        header.add(receipt)
    }

    override suspend fun insertDetails(details: List<GoodsReceiptDetailEntity>) {
        detail.addAll(details)
    }

    private fun GoodsReceiptEntity.withRefs(): GoodsReceiptWithRefs = GoodsReceiptWithRefs(
        receipt = this,
        supplier = Contoh.supplier(supplierId),
        operator = Contoh.user(operatorId),
        approver = approvedBy?.let { Contoh.user(it) },
        details = detail.filter { it.receiptId == id }.map { baris ->
            GoodsReceiptDetailWithRefs(
                detail = baris,
                item = Contoh.item(baris.sku),
                location = Contoh.lokasi(baris.locationKode),
            )
        },
    )
}

class FakeGoodsIssueDao : GoodsIssueDao {

    val header = mutableListOf<GoodsIssueEntity>()
    val detail = mutableListOf<GoodsIssueDetailEntity>()

    override fun observeIssues(): Flow<List<GoodsIssueWithRefs>> =
        flowOf(header.map { it.withRefs() })

    override fun observeIssuesByStatus(status: DocumentStatus): Flow<List<GoodsIssueWithRefs>> =
        flowOf(header.filter { it.status == status }.map { it.withRefs() })

    override suspend fun findById(id: String): GoodsIssueWithRefs? =
        header.firstOrNull { it.id == id }?.withRefs()

    override suspend fun count(): Int = header.size

    override suspend fun updateStatus(
        id: String,
        status: DocumentStatus,
        approvedBy: String?,
        approvedAt: Long?,
        catatan: String?,
    ) {
        val posisi = header.indexOfFirst { it.id == id }
        if (posisi < 0) return
        header[posisi] = header[posisi].copy(
            status = status,
            approvedBy = approvedBy,
            approvedAt = approvedAt,
            catatan = catatan,
        )
    }

    override suspend fun insertHeader(issue: GoodsIssueEntity) {
        header.add(issue)
    }

    override suspend fun insertDetails(details: List<GoodsIssueDetailEntity>) {
        detail.addAll(details)
    }

    private fun GoodsIssueEntity.withRefs(): GoodsIssueWithRefs = GoodsIssueWithRefs(
        issue = this,
        operator = Contoh.user(operatorId),
        approver = approvedBy?.let { Contoh.user(it) },
        details = detail.filter { it.issueId == id }.map { baris ->
            GoodsIssueDetailWithRefs(
                detail = baris,
                item = Contoh.item(baris.sku),
                location = Contoh.lokasi(baris.locationKode),
            )
        },
    )
}

class FakeUserDao(
    rolesAwal: List<RoleEntity> = emptyList(),
    penggunaAwal: List<UserWithRole> = emptyList(),
) : UserDao {

    val roles = rolesAwal.toMutableList()
    val users = penggunaAwal.map { it.user }.toMutableList()

    override suspend fun findByUsername(username: String): UserWithRole? =
        users.firstOrNull { it.username == username }?.withRole()

    override suspend fun findById(id: String): UserWithRole? =
        users.firstOrNull { it.id == id }?.withRole()

    override fun observeUsers(): Flow<List<UserWithRole>> =
        flowOf(users.map { it.withRole() })

    override fun observeRoles(): Flow<List<RoleEntity>> = flowOf(roles.toList())

    override suspend fun countUsersWithRole(roleId: String): Int =
        users.count { it.roleId == roleId }

    override suspend fun updateAktif(id: String, aktif: Boolean) {
        val posisi = users.indexOfFirst { it.id == id }
        if (posisi >= 0) users[posisi] = users[posisi].copy(aktif = aktif)
    }

    override suspend fun upsertUser(user: UserEntity) {
        val posisi = users.indexOfFirst { it.id == user.id }
        if (posisi >= 0) users[posisi] = user else users.add(user)
    }

    override suspend fun upsertRole(role: RoleEntity) {
        val posisi = roles.indexOfFirst { it.id == role.id }
        if (posisi >= 0) roles[posisi] = role else roles.add(role)
    }

    override suspend fun deleteRole(id: String) {
        roles.removeAll { it.id == id }
    }

    override suspend fun insertRoles(daftar: List<RoleEntity>) {
        daftar.forEach { upsertRole(it) }
    }

    override suspend fun insertUsers(daftar: List<UserEntity>) {
        daftar.forEach { upsertUser(it) }
    }

    private fun UserEntity.withRole(): UserWithRole =
        UserWithRole(this, roles.first { it.id == roleId })
}
