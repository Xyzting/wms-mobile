package com.utb.wms.domain.model

data class BarisLaporanStok(
    val sku: String,
    val namaItem: String,
    val satuan: String,
    val kodeLokasi: String,
    val namaLokasi: String,
    val jumlahStok: Int,
    val stokMinimum: Int,
) {
    val dibawahMinimum: Boolean get() = jumlahStok < stokMinimum
}

data class BarisLaporanMutasi(
    val tanggal: Long,
    val sku: String,
    val namaItem: String,
    val kodeLokasi: String,
    val tipe: MovementType,
    val qty: Int,
    val referensi: String,
    val keterangan: String?,
)
