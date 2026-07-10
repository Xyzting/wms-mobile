package com.utb.wms.ui.inventory

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.utb.wms.R
import com.utb.wms.databinding.FragmentStockMovementBinding
import com.utb.wms.domain.model.MovementType
import com.utb.wms.ui.common.appContainer
import kotlinx.coroutines.launch

class StockMovementFragment : Fragment(R.layout.fragment_stock_movement) {

    private var binding: FragmentStockMovementBinding? = null

    private val viewModel: StockMovementViewModel by viewModels {
        StockMovementViewModel.factory(appContainer.inventoryRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tampilan = FragmentStockMovementBinding.bind(view)
        binding = tampilan

        val adapter = StockMovementAdapter()
        tampilan.daftarMutasi.adapter = adapter

        tampilan.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        tampilan.inputCari.doAfterTextChanged { teks ->
            viewModel.cari(teks?.toString().orEmpty())
        }
        tampilan.grupTipe.setOnCheckedStateChangeListener { _, terpilih ->
            viewModel.saringTipe(tipeUntuk(terpilih.firstOrNull()))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { keadaan ->
                    adapter.submitList(keadaan.baris)
                    tampilan.textJumlah.text =
                        getString(R.string.mutasi_jumlah, keadaan.baris.size)
                    tampilan.textKosong.setText(
                        if (keadaan.menyaring) {
                            R.string.mutasi_kosong_saring
                        } else {
                            R.string.mutasi_kosong
                        },
                    )
                    tampilan.textKosong.visibility =
                        if (keadaan.baris.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun tipeUntuk(idChip: Int?): MovementType? = when (idChip) {
        R.id.chip_tipe_masuk -> MovementType.INBOUND
        R.id.chip_tipe_keluar -> MovementType.OUTBOUND
        R.id.chip_tipe_penyesuaian -> MovementType.ADJUSTMENT
        else -> null
    }

    override fun onDestroyView() {
        binding?.daftarMutasi?.adapter = null
        binding = null
        super.onDestroyView()
    }
}
