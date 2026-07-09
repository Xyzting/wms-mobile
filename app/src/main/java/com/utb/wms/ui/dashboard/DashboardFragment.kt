package com.utb.wms.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.utb.wms.R
import com.utb.wms.ui.theme.WMSMobileTheme

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            WMSMobileTheme {
                DashboardRoute(
                    onOpenInbound = {
                        findNavController().navigate(R.id.action_dashboard_to_goodsReceiptForm)
                    },
                    onOpenOutbound = {
                        findNavController().navigate(R.id.action_dashboard_to_goodsIssueForm)
                    },
                    onOpenInventory = {
                        findNavController().navigate(R.id.action_dashboard_to_stockList)
                    },
                    onLogout = {
                        findNavController().navigate(R.id.action_dashboard_to_login)
                    },
                )
            }
        }
    }
}
