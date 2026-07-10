package com.utb.wms.ui.document

import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import com.utb.wms.R
import com.utb.wms.domain.model.DocumentStatus
import com.utb.wms.domain.repository.DocumentResult

enum class AksiDokumen { SETUJUI, POSTING, BATALKAN }

sealed interface PesanDokumen {

    data class Berhasil(val status: DocumentStatus) : PesanDokumen

    data class StokKurang(val sku: String, val tersedia: Int, val diminta: Int) : PesanDokumen

    data class TransisiTidakValid(val dari: DocumentStatus, val ke: DocumentStatus) : PesanDokumen

    data object TidakDitemukan : PesanDokumen

    data object TidakBerwenang : PesanDokumen

    data object SesiBerakhir : PesanDokumen

    data object Gagal : PesanDokumen
}

data class BarisDokumen(
    val id: String,
    val nomor: String,
    val pihak: String,
    val tanggal: Long,
    val status: DocumentStatus,
    val jumlahBaris: Int,
    val totalQty: Int,
    val operator: String,
    val penyetuju: String?,
    val catatan: String?,
    val bolehMenyetujui: Boolean,
) {
    val bolehSetujui: Boolean
        get() = bolehMenyetujui && status == DocumentStatus.DRAFT

    val bolehPosting: Boolean
        get() = bolehMenyetujui && status == DocumentStatus.VALIDATED

    val bolehBatalkan: Boolean
        get() = bolehMenyetujui &&
            (status == DocumentStatus.DRAFT || status == DocumentStatus.VALIDATED)

    val adaAksi: Boolean
        get() = bolehSetujui || bolehPosting || bolehBatalkan
}

fun DocumentResult.kePesan(): PesanDokumen = when (this) {
    is DocumentResult.Success -> PesanDokumen.Berhasil(status)
    is DocumentResult.InsufficientStock -> PesanDokumen.StokKurang(sku, tersedia, diminta)
    is DocumentResult.InvalidTransition -> PesanDokumen.TransisiTidakValid(dari, ke)
    DocumentResult.NotFound -> PesanDokumen.TidakDitemukan
}

@StringRes
fun DocumentStatus.labelRes(): Int = when (this) {
    DocumentStatus.DRAFT -> R.string.status_draft
    DocumentStatus.VALIDATED -> R.string.status_validated
    DocumentStatus.POSTED -> R.string.status_posted
    DocumentStatus.CANCELLED -> R.string.status_cancelled
}

@AttrRes
fun DocumentStatus.warnaLatarAttr(): Int = when (this) {
    DocumentStatus.DRAFT -> com.google.android.material.R.attr.colorSurfaceVariant
    DocumentStatus.VALIDATED -> com.google.android.material.R.attr.colorSecondaryContainer
    DocumentStatus.POSTED -> com.google.android.material.R.attr.colorPrimaryContainer
    DocumentStatus.CANCELLED -> com.google.android.material.R.attr.colorErrorContainer
}

@AttrRes
fun DocumentStatus.warnaTeksAttr(): Int = when (this) {
    DocumentStatus.DRAFT -> com.google.android.material.R.attr.colorOnSurfaceVariant
    DocumentStatus.VALIDATED -> com.google.android.material.R.attr.colorOnSecondaryContainer
    DocumentStatus.POSTED -> com.google.android.material.R.attr.colorOnPrimaryContainer
    DocumentStatus.CANCELLED -> com.google.android.material.R.attr.colorOnErrorContainer
}
