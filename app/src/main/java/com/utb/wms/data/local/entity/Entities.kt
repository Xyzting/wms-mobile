package com.utb.wms.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.utb.wms.domain.model.DocumentStatus
import com.utb.wms.domain.model.MovementType

@Entity(tableName = "roles")
data class RoleEntity(
    @PrimaryKey val id: String,
    val namaRole: String,
)

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["roleId"]),
    ],
)
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val password: String,
    val nama: String,
    val roleId: String,
    val aktif: Boolean,
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey val id: String,
    val nama: String,
    val kontak: String,
    val isActive: Boolean,
)

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val sku: String,
    val nama: String,
    val satuan: String,
    val stokMinimum: Int,
)

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey val kode: String,
    val nama: String,
    val kapasitas: Int,
)

@Entity(
    tableName = "stocks",
    indices = [
        Index(value = ["sku", "locationKode"], unique = true),
        Index(value = ["locationKode"]),
    ],
)
data class StockEntity(
    @PrimaryKey val id: String,
    val sku: String,
    val locationKode: String,
    val jumlahStok: Int,
)

@Entity(
    tableName = "stock_movements",
    indices = [
        Index(value = ["sku"]),
        Index(value = ["locationKode"]),
    ],
)
data class StockMovementEntity(
    @PrimaryKey val id: String,
    val sku: String,
    val locationKode: String,
    val tipe: MovementType,
    val qty: Int,
    val tanggal: Long,
    val referensi: String,
)

@Entity(
    tableName = "goods_receipts",
    indices = [
        Index(value = ["noReceipt"], unique = true),
        Index(value = ["supplierId"]),
        Index(value = ["operatorId"]),
    ],
)
data class GoodsReceiptEntity(
    @PrimaryKey val id: String,
    val noReceipt: String,
    val tanggal: Long,
    val supplierId: String,
    val operatorId: String,
    val status: DocumentStatus,
)

@Entity(
    tableName = "goods_receipt_details",
    indices = [Index(value = ["receiptId"])],
)
data class GoodsReceiptDetailEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val receiptId: String,
    val sku: String,
    val locationKode: String,
    val qty: Int,
)

@Entity(
    tableName = "goods_issues",
    indices = [
        Index(value = ["noIssue"], unique = true),
        Index(value = ["operatorId"]),
    ],
)
data class GoodsIssueEntity(
    @PrimaryKey val id: String,
    val noIssue: String,
    val tanggal: Long,
    val tujuan: String,
    val operatorId: String,
    val status: DocumentStatus,
)

@Entity(
    tableName = "goods_issue_details",
    indices = [Index(value = ["issueId"])],
)
data class GoodsIssueDetailEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val issueId: String,
    val sku: String,
    val locationKode: String,
    val qty: Int,
)
