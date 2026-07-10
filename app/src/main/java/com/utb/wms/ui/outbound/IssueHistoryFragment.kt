package com.utb.wms.ui.outbound

import androidx.fragment.app.viewModels
import com.utb.wms.R
import com.utb.wms.ui.common.appContainer
import com.utb.wms.ui.document.DocumentHistoryFragment

class IssueHistoryFragment : DocumentHistoryFragment() {

    override val viewModel: IssueHistoryViewModel by viewModels {
        IssueHistoryViewModel.factory(
            outboundRepository = appContainer.outboundRepository,
            authRepository = appContainer.authRepository,
        )
    }

    override val judul: Int = R.string.judul_riwayat_pengeluaran

    override val teksKosong: Int = R.string.riwayat_kosong_pengeluaran

    override val labelPihak: Int = R.string.riwayat_tujuan
}
