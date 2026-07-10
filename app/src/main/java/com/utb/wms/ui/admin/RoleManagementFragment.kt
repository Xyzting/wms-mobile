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
import com.utb.wms.databinding.DialogFormRoleBinding
import com.utb.wms.databinding.FragmentRoleManagementBinding
import com.utb.wms.domain.model.Role
import com.utb.wms.ui.common.appContainer
import kotlinx.coroutines.launch

class RoleManagementFragment : Fragment(R.layout.fragment_role_management) {

    private var binding: FragmentRoleManagementBinding? = null

    private val viewModel: AdminViewModel by viewModels {
        AdminViewModel.factory(appContainer.userRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tampilan = FragmentRoleManagementBinding.bind(view)
        binding = tampilan

        val adapter = RoleAdapter(
            onUbah = ::formRole,
            onHapus = ::konfirmasiHapus,
        )
        tampilan.daftarRole.adapter = adapter

        tampilan.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        tampilan.tombolTambah.setOnClickListener { formRole(null) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.roles.collect { gambarUlang(tampilan, adapter) } }
                launch { viewModel.users.collect { gambarUlang(tampilan, adapter) } }
                launch {
                    viewModel.pesan.collect { pesan -> tampilkanPesan(tampilan.root, pesan) }
                }
            }
        }
    }

    private fun gambarUlang(tampilan: FragmentRoleManagementBinding, adapter: RoleAdapter) {
        val baris = viewModel.roles.value.map { role ->
            BarisRole(role, viewModel.jumlahPenggunaDenganRole(role.id))
        }
        adapter.submitList(baris)
        tampilan.textKosong.visibility = if (baris.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun formRole(lama: Role?) {
        val form = DialogFormRoleBinding.inflate(layoutInflater)
        lama?.let {
            form.inputKode.setText(it.id)
            form.inputKode.isEnabled = false
            form.inputNama.setText(it.namaRole)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (lama == null) R.string.role_judul_tambah else R.string.role_judul_ubah)
            .setView(form.root)
            .setNegativeButton(R.string.master_batal, null)
            .setPositiveButton(R.string.master_simpan) { _, _ ->
                viewModel.simpanRole(
                    Role(
                        id = form.inputKode.text.toString(),
                        namaRole = form.inputNama.text.toString(),
                    ),
                )
            }
            .show()
    }

    private fun konfirmasiHapus(role: Role) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.role_konfirmasi_judul, role.namaRole))
            .setMessage(R.string.role_konfirmasi_pesan)
            .setNegativeButton(R.string.master_batal, null)
            .setPositiveButton(R.string.umum_hapus) { _, _ -> viewModel.hapusRole(role.id) }
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
        binding?.daftarRole?.adapter = null
        binding = null
        super.onDestroyView()
    }
}
