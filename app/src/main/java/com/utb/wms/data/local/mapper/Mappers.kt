package com.utb.wms.data.local.mapper

import com.utb.wms.data.local.entity.ItemEntity
import com.utb.wms.data.local.entity.LocationEntity
import com.utb.wms.data.local.entity.RoleEntity
import com.utb.wms.data.local.entity.SupplierEntity
import com.utb.wms.data.local.relation.StockMovementWithRefs
import com.utb.wms.data.local.relation.StockWithRefs
import com.utb.wms.data.local.relation.UserWithRole
import com.utb.wms.domain.model.Item
import com.utb.wms.domain.model.Location
import com.utb.wms.domain.model.Role
import com.utb.wms.domain.model.Stock
import com.utb.wms.domain.model.StockMovement
import com.utb.wms.domain.model.Supplier
import com.utb.wms.domain.model.User

fun RoleEntity.toDomain(): Role = Role(
    id = id,
    namaRole = namaRole,
)

fun UserWithRole.toDomain(): User = User(
    id = user.id,
    username = user.username,
    password = user.password,
    nama = user.nama,
    role = role.toDomain(),
    aktif = user.aktif,
)

fun SupplierEntity.toDomain(): Supplier = Supplier(
    id = id,
    nama = nama,
    kontak = kontak,
    isActive = isActive,
)

fun ItemEntity.toDomain(): Item = Item(
    sku = sku,
    nama = nama,
    satuan = satuan,
    stokMinimum = stokMinimum,
)

fun LocationEntity.toDomain(): Location = Location(
    kode = kode,
    nama = nama,
    kapasitas = kapasitas,
)

fun StockWithRefs.toDomain(): Stock = Stock(
    id = stock.id,
    item = item.toDomain(),
    location = location.toDomain(),
    jumlahStok = stock.jumlahStok,
)

fun StockMovementWithRefs.toDomain(): StockMovement = StockMovement(
    id = movement.id,
    item = item.toDomain(),
    location = location.toDomain(),
    tipe = movement.tipe,
    qty = movement.qty,
    tanggal = movement.tanggal,
    referensi = movement.referensi,
)
