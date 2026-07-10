package com.utb.wms.ui.common

import com.utb.wms.domain.model.MovementType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FORMAT_TANGGAL: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale("id", "ID"))
        .withZone(ZoneId.systemDefault())

fun tanggalRingkas(epochMillis: Long): String =
    FORMAT_TANGGAL.format(Instant.ofEpochMilli(epochMillis))

fun qtyBertanda(tipe: MovementType, qty: Int): String = when (tipe) {
    MovementType.INBOUND -> "+$qty"
    MovementType.OUTBOUND -> "-$qty"
    MovementType.ADJUSTMENT -> if (qty >= 0) "+$qty" else "$qty"
}
