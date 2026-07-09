package com.utb.wms.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
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

data class UserWithRole(
    @Embedded val user: UserEntity,
    @Relation(parentColumn = "roleId", entityColumn = "id")
    val role: RoleEntity,
)

data class StockWithRefs(
    @Embedded val stock: StockEntity,
    @Relation(parentColumn = "sku", entityColumn = "sku")
    val item: ItemEntity,
    @Relation(parentColumn = "locationKode", entityColumn = "kode")
    val location: LocationEntity,
)

data class StockMovementWithRefs(
    @Embedded val movement: StockMovementEntity,
    @Relation(parentColumn = "sku", entityColumn = "sku")
    val item: ItemEntity,
    @Relation(parentColumn = "locationKode", entityColumn = "kode")
    val location: LocationEntity,
    @Relation(entity = UserEntity::class, parentColumn = "operatorId", entityColumn = "id")
    val operator: UserWithRole?,
)

data class GoodsReceiptDetailWithRefs(
    @Embedded val detail: GoodsReceiptDetailEntity,
    @Relation(parentColumn = "sku", entityColumn = "sku")
    val item: ItemEntity,
    @Relation(parentColumn = "locationKode", entityColumn = "kode")
    val location: LocationEntity,
)

data class GoodsReceiptWithRefs(
    @Embedded val receipt: GoodsReceiptEntity,
    @Relation(parentColumn = "supplierId", entityColumn = "id")
    val supplier: SupplierEntity,
    @Relation(entity = UserEntity::class, parentColumn = "operatorId", entityColumn = "id")
    val operator: UserWithRole,
    @Relation(entity = UserEntity::class, parentColumn = "approvedBy", entityColumn = "id")
    val approver: UserWithRole?,
    @Relation(
        entity = GoodsReceiptDetailEntity::class,
        parentColumn = "id",
        entityColumn = "receiptId",
    )
    val details: List<GoodsReceiptDetailWithRefs>,
)

data class GoodsIssueDetailWithRefs(
    @Embedded val detail: GoodsIssueDetailEntity,
    @Relation(parentColumn = "sku", entityColumn = "sku")
    val item: ItemEntity,
    @Relation(parentColumn = "locationKode", entityColumn = "kode")
    val location: LocationEntity,
)

data class GoodsIssueWithRefs(
    @Embedded val issue: GoodsIssueEntity,
    @Relation(entity = UserEntity::class, parentColumn = "operatorId", entityColumn = "id")
    val operator: UserWithRole,
    @Relation(entity = UserEntity::class, parentColumn = "approvedBy", entityColumn = "id")
    val approver: UserWithRole?,
    @Relation(
        entity = GoodsIssueDetailEntity::class,
        parentColumn = "id",
        entityColumn = "issueId",
    )
    val details: List<GoodsIssueDetailWithRefs>,
)
