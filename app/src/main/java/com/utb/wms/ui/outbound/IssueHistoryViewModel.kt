package com.utb.wms.ui.outbound

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.utb.wms.domain.model.DocumentStatus
import com.utb.wms.domain.model.GoodsIssue
import com.utb.wms.domain.model.bolehMenyetujui
import com.utb.wms.domain.repository.AuthRepository
import com.utb.wms.domain.repository.OutboundRepository
import com.utb.wms.ui.document.AksiDokumen
import com.utb.wms.ui.document.BarisDokumen
import com.utb.wms.ui.document.PesanDokumen
import com.utb.wms.ui.document.RiwayatDokumenViewModel
import com.utb.wms.ui.document.kePesan
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class IssueHistoryViewModel(
    private val outboundRepository: OutboundRepository,
    private val authRepository: AuthRepository,
) : ViewModel(), RiwayatDokumenViewModel {

    private val _status = MutableStateFlow<DocumentStatus?>(null)

    override val status: StateFlow<DocumentStatus?> = _status.asStateFlow()

    private val _pesan = Channel<PesanDokumen>(Channel.BUFFERED)

    override val pesan: Flow<PesanDokumen> = _pesan.receiveAsFlow()

    override val baris: StateFlow<List<BarisDokumen>> = combine(
        _status.flatMapLatest { status ->
            if (status == null) {
                outboundRepository.observeGoodsIssues()
            } else {
                outboundRepository.observeGoodsIssuesByStatus(status)
            }
        },
        authRepository.currentUser,
    ) { dokumen, pengguna ->
        val boleh = pengguna?.bolehMenyetujui == true
        dokumen.map { it.keBaris(boleh) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    override fun saringStatus(status: DocumentStatus?) {
        _status.update { status }
    }

    override fun jalankan(id: String, aksi: AksiDokumen) {
        val pengguna = authRepository.currentUser.value
        if (pengguna == null) {
            _pesan.trySend(PesanDokumen.SesiBerakhir)
            return
        }
        if (!pengguna.bolehMenyetujui) {
            _pesan.trySend(PesanDokumen.TidakBerwenang)
            return
        }

        viewModelScope.launch {
            val hasil = runCatching {
                when (aksi) {
                    AksiDokumen.SETUJUI -> outboundRepository.validateGoodsIssue(id, pengguna)
                    AksiDokumen.POSTING ->
                        outboundRepository.postGoodsIssue(id, System.currentTimeMillis())

                    AksiDokumen.BATALKAN -> outboundRepository.cancelGoodsIssue(id)
                }
            }
            _pesan.send(
                hasil.fold(
                    onSuccess = { it.kePesan() },
                    onFailure = { PesanDokumen.Gagal },
                ),
            )
        }
    }

    private fun GoodsIssue.keBaris(bolehMenyetujui: Boolean) = BarisDokumen(
        id = id,
        nomor = noIssue,
        pihak = tujuan,
        tanggal = tanggal,
        status = status,
        jumlahBaris = details.size,
        totalQty = totalQty,
        operator = operator.nama,
        penyetuju = approvedBy?.nama,
        catatan = catatan,
        bolehMenyetujui = bolehMenyetujui,
    )

    companion object {
        fun factory(
            outboundRepository: OutboundRepository,
            authRepository: AuthRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { IssueHistoryViewModel(outboundRepository, authRepository) }
        }
    }
}
