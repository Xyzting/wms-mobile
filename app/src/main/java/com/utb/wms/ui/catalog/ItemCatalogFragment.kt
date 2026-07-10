package com.utb.wms.ui.catalog

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
import com.utb.wms.databinding.FragmentItemCatalogBinding
import com.utb.wms.ui.common.appContainer
import kotlinx.coroutines.launch

class ItemCatalogFragment : Fragment(R.layout.fragment_item_catalog) {

    private var binding: FragmentItemCatalogBinding? = null

    private val viewModel: ItemCatalogViewModel by viewModels {
        ItemCatalogViewModel.factory(appContainer.masterDataRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tampilan = FragmentItemCatalogBinding.bind(view)
        binding = tampilan

        val adapter = ItemCatalogAdapter()
        tampilan.daftarBarang.adapter = adapter

        tampilan.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        tampilan.inputCari.doAfterTextChanged { teks ->
            viewModel.cari(teks?.toString().orEmpty())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.hasil.collect { daftar ->
                    adapter.submitList(daftar)
                    tampilan.textJumlah.text = getString(R.string.katalog_hasil, daftar.size)
                    tampilan.textKosong.visibility =
                        if (daftar.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        binding?.daftarBarang?.adapter = null
        binding = null
        super.onDestroyView()
    }
}
