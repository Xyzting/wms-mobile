package com.utb.wms.ui.inbound

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.utb.wms.R
import com.utb.wms.ui.common.appContainer
import com.utb.wms.ui.theme.WMSMobileTheme

class GoodsReceiptFormFragment : Fragment() {

    private val viewModel: GoodsReceiptViewModel by viewModels {
        GoodsReceiptViewModel.factory(
            masterDataRepository = appContainer.masterDataRepository,
            inboundRepository = appContainer.inboundRepository,
            authRepository = appContainer.authRepository,
        )
    }

    private val pemindai = registerForActivityResult(ScanContract()) { hasil ->
        viewModel.terapkanBarcode(hasil.contents)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            WMSMobileTheme {
                GoodsReceiptRoute(
                    viewModel = viewModel,
                    onBack = { findNavController().popBackStack() },
                    onPindai = { idBaris ->
                        viewModel.mulaiPindai(idBaris)
                        pemindai.launch(opsiPindai())
                    },
                )
            }
        }
    }

    private fun opsiPindai(): ScanOptions = ScanOptions()
        .setPrompt(getString(R.string.pindai_prompt))
        .setBeepEnabled(true)
}
