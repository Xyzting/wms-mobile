package com.utb.wms.ui.admin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.utb.wms.R
import com.utb.wms.databinding.DialogFormPenggunaBinding
import com.utb.wms.databinding.FragmentUserManagementBinding
import com.utb.wms.domain.model.Role
import com.utb.wms.domain.model.User
import com.utb.wms.ui.common.appContainer
import kotlinx.coroutines.launch

class UserManagementFragment : Fragment(R.layout.fragment_user_management) {

    private var binding: FragmentUserManagementBinding? = null

    private val viewModel: AdminViewModel by viewModels {
        AdminViewModel.factory(appContainer.userRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tampilan = FragmentUserManagementBinding.bind(view)
        binding = tampilan

        val adapter = UserAdapter(
            onUbah = { pengguna -> formPengguna(pengguna) },
            onUbahStatus = { pengguna, aktif -> viewModel.ubahStatusAktif(pengguna.id, aktif) },
        )
        tampilan.daftarPengguna.adapter = adapter

        tampilan.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        tampilan.tombolTambah.setOnClickListener { formPengguna(null) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.users.collect { daftar ->
                        adapter.submitList(daftar)
                        tampilan.textKosong.visibility =
                            if (daftar.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.pesan.collect { pesan -> tampilkanPesan(tampilan.root, pesan) }
                }
            }
        }
    }

    private fun formPengguna(lama: User?) {
        val daftarRole = viewModel.roles.value
        if (daftarRole.isEmpty()) return

        val form = DialogFormPenggunaBinding.inflate(layoutInflater)
        form.inputRole.setSimpleItems(daftarRole.map { it.namaRole }.toTypedArray())

        var rolePilihan: Role = lama?.role ?: daftarRole.first()
        form.inputRole.setText(rolePilihan.namaRole, false)
        form.inputRole.setOnItemClickListener { _, _, posisi, _ ->
            rolePilihan = daftarRole[posisi]
        }

        lama?.let {
            form.inputKode.setText(it.id)
            form.inputKode.isEnabled = false
            form.inputUsername.setText(it.username)
            form.inputNama.setText(it.nama)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (lama == null) R.string.pengguna_judul_tambah else R.string.pengguna_judul_ubah)
            .setView(form.root)
            .setNegativeButton(R.string.master_batal, null)
            .setPositiveButton(R.string.master_simpan) { _, _ ->
                val sandi = form.inputPassword.text.toString().takeIf { it.isNotBlank() }
                viewModel.simpanUser(
                    User(
                        id = form.inputKode.text.toString(),
                        username = form.inputUsername.text.toString(),
                        password = lama?.password.orEmpty(),
                        nama = form.inputNama.text.toString(),
                        role = rolePilihan,
                        aktif = lama?.aktif ?: true,
                    ),
                    password = sandi,
                )
            }
            .show()
    }

    private fun tampilkanPesan(akar: View, pesan: PesanAdmin) {
        val teks = when (pesan) {
            is PesanAdmin.Galat -> pesan.pesan
            is PesanAdmin.RoleMasihDipakai ->
                getString(R.string.role_masih_dipakai, pesan.jumlahPengguna)

            PesanAdmin.RoleTerhapus -> getString(R.string.role_terhapus)
            PesanAdmin.Tersimpan -> getString(R.string.umum_tersimpan)
        }
        Snackbar.make(akar, teks, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        binding?.daftarPengguna?.adapter = null
        binding = null
        super.onDestroyView()
    }
}
