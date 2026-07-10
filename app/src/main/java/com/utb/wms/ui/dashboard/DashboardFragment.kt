package com.utb.wms.ui.dashboard

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.utb.wms.R
import com.utb.wms.databinding.FragmentDashboardBinding
import com.utb.wms.ui.common.appContainer
import kotlinx.coroutines.launch

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private var binding: FragmentDashboardBinding? = null

    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModel.factory(
            authRepository = appContainer.authRepository,
            inventoryRepository = appContainer.inventoryRepository,
            inboundRepository = appContainer.inboundRepository,
            outboundRepository = appContainer.outboundRepository,
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tampilan = FragmentDashboardBinding.bind(view)
        binding = tampilan

        val adapter = DashboardAdapter(::bukaMenu, ::bukaUbin)
        val manajer = GridLayoutManager(requireContext(), 2)
        manajer.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int = adapter.spanUntuk(position)
        }
        tampilan.daftarDashboard.layoutManager = manajer
        tampilan.daftarDashboard.adapter = adapter

        tampilan.toolbar.setOnMenuItemClickListener { butir ->
            when (butir.itemId) {
                R.id.aksi_profil -> {
                    findNavController().navigate(R.id.action_dashboard_to_profile)
                    true
                }

                R.id.aksi_keluar -> {
                    appContainer.authRepository.logout()
                    findNavController().navigate(R.id.action_dashboard_to_login)
                    true
                }

                else -> false
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { keadaan ->
                    adapter.submitList(susunBaris(keadaan))
                }
            }
        }
    }

    private fun susunBaris(keadaan: DashboardState): List<BarisDashboard> {
        val pengguna = keadaan.pengguna ?: return emptyList()
        val menu = menuUntuk(pengguna)

        return buildList {
            add(
                BarisDashboard.Sambutan(
                    nama = pengguna.nama,
                    role = pengguna.role.namaRole,
                    stokMenipis = keadaan.stokMenipis,
                    penerimaanMenunggu = keadaan.penerimaanMenunggu,
                    pengeluaranMenunggu = keadaan.pengeluaranMenunggu,
                ),
            )
            KategoriMenu.entries.forEach { kategori ->
                val isi = menu.filter { it.kategori == kategori }
                if (isi.isNotEmpty()) {
                    add(BarisDashboard.Bagian(kategori.judul))
                    isi.forEach { add(BarisDashboard.Menu(it)) }
                }
            }
        }
    }

    private fun bukaMenu(menu: MenuUtama) {
        findNavController().navigate(menu.actionId)
    }

    private fun bukaUbin(ubin: UbinRingkasan) {
        val aksi = when (ubin) {
            UbinRingkasan.STOK_MENIPIS -> R.id.action_dashboard_to_stockList
            UbinRingkasan.PENERIMAAN_MENUNGGU -> R.id.action_dashboard_to_receiptHistory
            UbinRingkasan.PENGELUARAN_MENUNGGU -> R.id.action_dashboard_to_issueHistory
        }
        findNavController().navigate(aksi)
    }

    override fun onDestroyView() {
        binding?.daftarDashboard?.adapter = null
        binding = null
        super.onDestroyView()
    }
}
