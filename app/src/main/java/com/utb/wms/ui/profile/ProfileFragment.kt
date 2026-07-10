package com.utb.wms.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.utb.wms.R
import com.utb.wms.databinding.FragmentProfileBinding
import com.utb.wms.domain.model.User
import com.utb.wms.domain.model.bolehMembuatDokumen
import com.utb.wms.domain.model.bolehMengelolaMasterData
import com.utb.wms.domain.model.bolehMengelolaPengguna
import com.utb.wms.domain.model.bolehMenyesuaikanStok
import com.utb.wms.domain.model.bolehMenyetujui
import com.utb.wms.ui.common.appContainer
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var binding: FragmentProfileBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tampilan = FragmentProfileBinding.bind(view)
        binding = tampilan

        val authRepository = appContainer.authRepository

        tampilan.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        tampilan.tombolKeluar.setOnClickListener {
            authRepository.logout()
            findNavController().navigate(R.id.action_profile_to_login)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authRepository.currentUser.collect { pengguna ->
                    if (pengguna == null) return@collect
                    tampilkan(tampilan, pengguna)
                }
            }
        }
    }

    private fun tampilkan(tampilan: FragmentProfileBinding, pengguna: User) {
        tampilan.textNama.text = pengguna.nama
        tampilan.textUsername.text = pengguna.username
        tampilan.textRole.text = pengguna.role.namaRole
        tampilan.textStatus.setText(
            if (pengguna.aktif) R.string.profil_aktif else R.string.profil_nonaktif,
        )

        val kewenangan = buildList {
            if (pengguna.bolehMembuatDokumen) add(R.string.profil_boleh_dokumen)
            if (pengguna.bolehMenyetujui) add(R.string.profil_boleh_menyetujui)
            if (pengguna.bolehMenyesuaikanStok) add(R.string.profil_boleh_penyesuaian)
            if (pengguna.bolehMengelolaMasterData) add(R.string.profil_boleh_master_data)
            if (pengguna.bolehMengelolaPengguna) add(R.string.profil_boleh_pengguna)
        }

        tampilan.grupKewenangan.removeAllViews()
        kewenangan.forEach { teks ->
            val chip = Chip(requireContext())
            chip.setText(teks)
            chip.isClickable = false
            chip.isCheckable = false
            tampilan.grupKewenangan.addView(chip)
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
