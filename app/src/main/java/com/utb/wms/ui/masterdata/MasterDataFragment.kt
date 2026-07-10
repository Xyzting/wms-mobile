package com.utb.wms.ui.masterdata

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
import com.google.android.material.tabs.TabLayout
import com.utb.wms.R
import com.utb.wms.databinding.DialogFormBarangBinding
import com.utb.wms.databinding.DialogFormLokasiBinding
import com.utb.wms.databinding.DialogFormPemasokBinding
import com.utb.wms.databinding.FragmentMasterDataBinding
import com.utb.wms.domain.model.Item
import com.utb.wms.domain.model.Location
import com.utb.wms.domain.model.Supplier
import com.utb.wms.ui.common.appContainer
import kotlinx.coroutines.launch

class MasterDataFragment : Fragment(R.layout.fragment_master_data) {

    private var binding: FragmentMasterDataBinding? = null

    private val viewModel: MasterDataViewModel by viewModels {
        MasterDataViewModel.factory(appContainer.masterDataRepository)
    }

    private var tabTerpilih = TipeMaster.BARANG

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tampilan = FragmentMasterDataBinding.bind(view)
        binding = tampilan

        val adapter = MasterDataAdapter(
            onUbah = ::bukaFormUbah,
            onNonaktifkan = ::konfirmasiNonaktifkan,
            onTelepon = ::telepon,
        )
        tampilan.daftarMaster.adapter = adapter

        tampilan.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        tampilan.tombolTambah.setOnClickListener { bukaFormTambah() }

        tampilan.tabMaster.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    tabTerpilih = TipeMaster.entries[tab.position]
                    gambarUlang(tampilan, adapter)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) = Unit

                override fun onTabReselected(tab: TabLayout.Tab) = Unit
            },
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.items.collect { gambarUlang(tampilan, adapter) } }
                launch { viewModel.locations.collect { gambarUlang(tampilan, adapter) } }
                launch { viewModel.suppliers.collect { gambarUlang(tampilan, adapter) } }
                launch {
                    viewModel.galat.collect { pesan ->
                        Snackbar.make(tampilan.root, pesan, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun gambarUlang(tampilan: FragmentMasterDataBinding, adapter: MasterDataAdapter) {
        val baris = when (tabTerpilih) {
            TipeMaster.BARANG -> viewModel.items.value.map { it.keBaris() }
            TipeMaster.LOKASI -> viewModel.locations.value.map { it.keBaris() }
            TipeMaster.PEMASOK -> viewModel.suppliers.value.map { it.keBaris() }
        }
        adapter.submitList(baris)
        tampilan.textKosong.visibility = if (baris.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun Item.keBaris() = BarisMaster(
        tipe = TipeMaster.BARANG,
        id = sku,
        judul = nama,
        subjudul = sku,
        info = getString(R.string.katalog_satuan, satuan) + " · " +
            getString(R.string.katalog_stok_minimum, stokMinimum),
        aktif = isActive,
    )

    private fun Location.keBaris() = BarisMaster(
        tipe = TipeMaster.LOKASI,
        id = kode,
        judul = nama,
        subjudul = kode,
        info = getString(R.string.form_kapasitas) + ": " + kapasitas,
        aktif = isActive,
    )

    private fun Supplier.keBaris() = BarisMaster(
        tipe = TipeMaster.PEMASOK,
        id = this.id,
        judul = nama,
        subjudul = this.id,
        info = kontak.ifBlank { "-" },
        aktif = isActive,
        telepon = kontak.ifBlank { null },
    )

    private fun bukaFormTambah() {
        when (tabTerpilih) {
            TipeMaster.BARANG -> formBarang(null)
            TipeMaster.LOKASI -> formLokasi(null)
            TipeMaster.PEMASOK -> formPemasok(null)
        }
    }

    private fun bukaFormUbah(baris: BarisMaster) {
        when (baris.tipe) {
            TipeMaster.BARANG -> formBarang(viewModel.items.value.firstOrNull { it.sku == baris.id })
            TipeMaster.LOKASI ->
                formLokasi(viewModel.locations.value.firstOrNull { it.kode == baris.id })

            TipeMaster.PEMASOK ->
                formPemasok(viewModel.suppliers.value.firstOrNull { it.id == baris.id })
        }
    }

    private fun formBarang(lama: Item?) {
        val form = DialogFormBarangBinding.inflate(layoutInflater)
        lama?.let {
            form.inputSku.setText(it.sku)
            form.inputNama.setText(it.nama)
            form.inputSatuan.setText(it.satuan)
            form.inputStokMinimum.setText(it.stokMinimum.toString())
            form.inputBarcode.setText(it.barcode.orEmpty())
            form.inputSku.isEnabled = false
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (lama == null) R.string.form_barang_judul_tambah else R.string.form_barang_judul_ubah)
            .setView(form.root)
            .setNegativeButton(R.string.master_batal, null)
            .setPositiveButton(R.string.master_simpan) { _, _ ->
                viewModel.simpanItem(
                    Item(
                        sku = form.inputSku.text.toString(),
                        nama = form.inputNama.text.toString(),
                        satuan = form.inputSatuan.text.toString(),
                        stokMinimum = form.inputStokMinimum.text.toString().toIntOrNull() ?: 0,
                        barcode = form.inputBarcode.text.toString(),
                        isActive = lama?.isActive ?: true,
                    ),
                )
            }
            .show()
    }

    private fun formLokasi(lama: Location?) {
        val form = DialogFormLokasiBinding.inflate(layoutInflater)
        lama?.let {
            form.inputKode.setText(it.kode)
            form.inputNama.setText(it.nama)
            form.inputKapasitas.setText(it.kapasitas.toString())
            form.inputKode.isEnabled = false
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (lama == null) R.string.form_lokasi_judul_tambah else R.string.form_lokasi_judul_ubah)
            .setView(form.root)
            .setNegativeButton(R.string.master_batal, null)
            .setPositiveButton(R.string.master_simpan) { _, _ ->
                viewModel.simpanLocation(
                    Location(
                        kode = form.inputKode.text.toString(),
                        nama = form.inputNama.text.toString(),
                        kapasitas = form.inputKapasitas.text.toString().toIntOrNull() ?: 0,
                        isActive = lama?.isActive ?: true,
                    ),
                )
            }
            .show()
    }

    private fun formPemasok(lama: Supplier?) {
        val form = DialogFormPemasokBinding.inflate(layoutInflater)
        lama?.let {
            form.inputKode.setText(it.id)
            form.inputNama.setText(it.nama)
            form.inputKontak.setText(it.kontak)
            form.inputKode.isEnabled = false
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (lama == null) R.string.form_pemasok_judul_tambah else R.string.form_pemasok_judul_ubah)
            .setView(form.root)
            .setNegativeButton(R.string.master_batal, null)
            .setPositiveButton(R.string.master_simpan) { _, _ ->
                viewModel.simpanSupplier(
                    Supplier(
                        id = form.inputKode.text.toString(),
                        nama = form.inputNama.text.toString(),
                        kontak = form.inputKontak.text.toString(),
                        isActive = lama?.isActive ?: true,
                    ),
                )
            }
            .show()
    }

    private fun konfirmasiNonaktifkan(baris: BarisMaster) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.master_konfirmasi_judul, baris.judul))
            .setMessage(R.string.master_konfirmasi_pesan)
            .setNegativeButton(R.string.master_batal, null)
            .setPositiveButton(R.string.master_nonaktifkan) { _, _ ->
                when (baris.tipe) {
                    TipeMaster.BARANG -> viewModel.nonaktifkanItem(baris.id)
                    TipeMaster.LOKASI -> viewModel.nonaktifkanLocation(baris.id)
                    TipeMaster.PEMASOK -> viewModel.nonaktifkanSupplier(baris.id)
                }
                binding?.let { tampilan ->
                    Snackbar.make(
                        tampilan.root,
                        getString(R.string.master_dinonaktifkan, baris.judul),
                        Snackbar.LENGTH_SHORT,
                    ).show()
                }
            }
            .show()
    }

    private fun telepon(nomor: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$nomor"))
        try {
            startActivity(intent)
        } catch (galat: ActivityNotFoundException) {
            binding?.let { tampilan ->
                Snackbar.make(
                    tampilan.root,
                    R.string.master_tidak_ada_aplikasi_telepon,
                    Snackbar.LENGTH_LONG,
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        binding?.daftarMaster?.adapter = null
        binding = null
        super.onDestroyView()
    }
}
