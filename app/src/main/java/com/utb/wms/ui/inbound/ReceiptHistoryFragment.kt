package com.utb.wms.ui.inbound

import androidx.fragment.app.viewModels
import com.utb.wms.R
import com.utb.wms.ui.common.appContainer
import com.utb.wms.ui.document.DocumentHistoryFragment

class ReceiptHistoryFragment : DocumentHistoryFragment() {

    override val viewModel: ReceiptHistoryViewModel by viewModels {
        ReceiptHistoryViewModel.factory(
            inboundRepository = appContainer.inboundRepository,
            authRepository = appContainer.authRepository,
        )
    }

    override val judul: Int = R.string.judul_riwayat_penerimaan

    override val teksKosong: Int = R.string.riwayat_kosong_penerimaan

    override val labelPihak: Int = R.string.riwayat_pemasok
}
