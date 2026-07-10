package com.utb.wms.ui.inventory

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.utb.wms.R
import com.utb.wms.databinding.FragmentStockListBinding
import com.utb.wms.ui.common.appContainer
import kotlinx.coroutines.launch

class StockListFragment : Fragment(R.layout.fragment_stock_list) {

    private var binding: FragmentStockListBinding? = null

    private val viewModel: InventoryViewModel by viewModels {
        InventoryViewModel.factory(appContainer.inventoryRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tampilan = FragmentStockListBinding.bind(view)
        binding = tampilan

        val adapter = StockAdapter()
        tampilan.daftarStok.adapter = adapter

        tampilan.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        tampilan.grupSaring.setOnCheckedStateChangeListener { _, terpilih ->
            viewModel.saringMenipis(terpilih.firstOrNull() == R.id.chip_menipis)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { keadaan ->
                    adapter.submitList(keadaan.baris)
                    tampilan.textJumlah.text =
                        getString(R.string.stok_jumlah, keadaan.baris.size)
                    tampilan.chipMenipis.text =
                        getString(R.string.stok_saring_menipis, keadaan.jumlahMenipis)
                    tampilan.textKosong.setText(
                        if (keadaan.hanyaMenipis) {
                            R.string.stok_kosong_saring
                        } else {
                            R.string.stok_kosong
                        },
                    )
                    tampilan.textKosong.visibility =
                        if (keadaan.baris.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        binding?.daftarStok?.adapter = null
        binding = null
        super.onDestroyView()
    }
}
