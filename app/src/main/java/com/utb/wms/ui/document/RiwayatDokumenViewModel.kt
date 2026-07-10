package com.utb.wms.ui.document

import com.utb.wms.domain.model.DocumentStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface RiwayatDokumenViewModel {

    val baris: StateFlow<List<BarisDokumen>>

    val status: StateFlow<DocumentStatus?>

    val pesan: Flow<PesanDokumen>

    fun saringStatus(status: DocumentStatus?)

    fun jalankan(id: String, aksi: AksiDokumen)
}
