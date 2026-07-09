package com.utb.wms.data.local.mapper

import com.utb.wms.data.local.entity.ItemEntity
import com.utb.wms.data.local.entity.LocationEntity
import com.utb.wms.data.local.entity.RoleEntity
import com.utb.wms.data.local.entity.SupplierEntity
import com.utb.wms.data.local.entity.UserEntity
import com.utb.wms.data.local.relation.GoodsIssueDetailWithRefs
import com.utb.wms.data.local.relation.GoodsIssueWithRefs
import com.utb.wms.data.local.relation.GoodsReceiptDetailWithRefs
import com.utb.wms.data.local.relation.GoodsReceiptWithRefs
import com.utb.wms.data.local.relation.StockMovementWithRefs
import com.utb.wms.data.local.relation.StockWithRefs
import com.utb.wms.data.local.relation.UserWithRole
import com.utb.wms.domain.model.GoodsIssue
import com.utb.wms.domain.model.GoodsIssueDetail
import com.utb.wms.domain.model.GoodsReceipt
import com.utb.wms.domain.model.GoodsReceiptDetail
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

fun Role.toEntity(): RoleEntity = RoleEntity(
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

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    username = username,
    password = password,
    nama = nama,
    roleId = role.id,
    aktif = aktif,
)

fun SupplierEntity.toDomain(): Supplier = Supplier(
    id = id,
    nama = nama,
    kontak = kontak,
    isActive = isActive,
)

fun Supplier.toEntity(): SupplierEntity = SupplierEntity(
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
    barcode = barcode,
    isActive = isActive,
)

fun Item.toEntity(): ItemEntity = ItemEntity(
    sku = sku,
    nama = nama,
    satuan = satuan,
    stokMinimum = stokMinimum,
    barcode = barcode,
    isActive = isActive,
)

fun LocationEntity.toDomain(): Location = Location(
    kode = kode,
    nama = nama,
    kapasitas = kapasitas,
    isActive = isActive,
)

fun Location.toEntity(): LocationEntity = LocationEntity(
    kode = kode,
    nama = nama,
    kapasitas = kapasitas,
    isActive = isActive,
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
    keterangan = movement.keterangan,
    operator = operator?.toDomain(),
)

fun GoodsReceiptDetailWithRefs.toDomain(): GoodsReceiptDetail = GoodsReceiptDetail(
    item = item.toDomain(),
    location = location.toDomain(),
    qty = detail.qty,
)

fun GoodsReceiptWithRefs.toDomain(): GoodsReceipt = GoodsReceipt(
    id = receipt.id,
    noReceipt = receipt.noReceipt,
    tanggal = receipt.tanggal,
    supplier = supplier.toDomain(),
    operator = operator.toDomain(),
    status = receipt.status,
    details = details.map { it.toDomain() },
    approvedBy = approver?.toDomain(),
    approvedAt = receipt.approvedAt,
    catatan = receipt.catatan,
)

fun GoodsIssueDetailWithRefs.toDomain(): GoodsIssueDetail = GoodsIssueDetail(
    item = item.toDomain(),
    location = location.toDomain(),
    qty = detail.qty,
)

fun GoodsIssueWithRefs.toDomain(): GoodsIssue = GoodsIssue(
    id = issue.id,
    noIssue = issue.noIssue,
    tanggal = issue.tanggal,
    tujuan = issue.tujuan,
    operator = operator.toDomain(),
    status = issue.status,
    details = details.map { it.toDomain() },
    approvedBy = approver?.toDomain(),
    approvedAt = issue.approvedAt,
    catatan = issue.catatan,
)
