package com.utb.wms.domain.model

enum class MovementType { INBOUND, OUTBOUND, ADJUSTMENT }

data class Stock(
    val id: String,
    val item: Item,
    val location: Location,
    val jumlahStok: Int,
) {
    val dibawahMinimum: Boolean get() = jumlahStok < item.stokMinimum
}

data class StockMovement(
    val id: String,
    val item: Item,
    val location: Location,
    val tipe: MovementType,
    val qty: Int,
    val tanggal: Long,
    val referensi: String,
    val keterangan: String? = null,
    val operator: User? = null,
)
