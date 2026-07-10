package com.utb.wms.ui.dashboard

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.utb.wms.R
import com.utb.wms.databinding.FragmentDashboardBinding
import com.utb.wms.ui.common.appContainer
import kotlinx.coroutines.launch

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private var binding: FragmentDashboardBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tampilan = FragmentDashboardBinding.bind(view)
        binding = tampilan

        val authRepository = appContainer.authRepository
        val adapter = MenuAdapter { menu -> findNavController().navigate(menu.actionId) }
        tampilan.daftarMenu.adapter = adapter

        tampilan.toolbar.setOnMenuItemClickListener { butir ->
            when (butir.itemId) {
                R.id.aksi_profil -> {
                    findNavController().navigate(R.id.action_dashboard_to_profile)
                    true
                }

                R.id.aksi_keluar -> {
                    authRepository.logout()
                    findNavController().navigate(R.id.action_dashboard_to_login)
                    true
                }

                else -> false
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authRepository.currentUser.collect { pengguna ->
                    if (pengguna == null) return@collect
                    tampilan.textNama.text = pengguna.nama
                    tampilan.textRole.text = pengguna.role.namaRole
                    adapter.submitList(menuUntuk(pengguna))
                }
            }
        }
    }

    override fun onDestroyView() {
        binding?.daftarMenu?.adapter = null
        binding = null
        super.onDestroyView()
    }
}
