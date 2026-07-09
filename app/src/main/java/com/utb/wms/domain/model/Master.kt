package com.utb.wms.domain.model

data class Role(
    val id: String,
    val namaRole: String,
)

data class User(
    val id: String,
    val username: String,
    val password: String,
    val nama: String,
    val role: Role,
    val aktif: Boolean = true,
)

data class Supplier(
    val id: String,
    val nama: String,
    val kontak: String = "",
    val isActive: Boolean = true,
)

data class Item(
    val sku: String,
    val nama: String,
    val satuan: String,
    val stokMinimum: Int = 0,
    val barcode: String? = null,
    val isActive: Boolean = true,
)

data class Location(
    val kode: String,
    val nama: String,
    val kapasitas: Int,
    val isActive: Boolean = true,
)
