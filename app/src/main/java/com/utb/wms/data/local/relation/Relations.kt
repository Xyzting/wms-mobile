package com.utb.wms.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.utb.wms.data.local.entity.ItemEntity
import com.utb.wms.data.local.entity.LocationEntity
import com.utb.wms.data.local.entity.RoleEntity
import com.utb.wms.data.local.entity.StockEntity
import com.utb.wms.data.local.entity.StockMovementEntity
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
)
