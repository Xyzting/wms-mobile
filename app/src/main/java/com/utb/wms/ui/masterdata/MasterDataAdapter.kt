package com.utb.wms.ui.masterdata

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.utb.wms.databinding.ItemMasterDataBinding

enum class TipeMaster { BARANG, LOKASI, PEMASOK }

data class BarisMaster(
    val tipe: TipeMaster,
    val id: String,
    val judul: String,
    val subjudul: String,
    val info: String,
    val aktif: Boolean,
    val telepon: String? = null,
)

class MasterDataAdapter(
    private val onUbah: (BarisMaster) -> Unit,
    private val onNonaktifkan: (BarisMaster) -> Unit,
    private val onTelepon: (String) -> Unit,
) : ListAdapter<BarisMaster, MasterDataAdapter.BarisViewHolder>(Pembanding) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarisViewHolder {
        val binding = ItemMasterDataBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return BarisViewHolder(binding, onUbah, onNonaktifkan, onTelepon)
    }

    override fun onBindViewHolder(holder: BarisViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BarisViewHolder(
        private val binding: ItemMasterDataBinding,
        private val onUbah: (BarisMaster) -> Unit,
        private val onNonaktifkan: (BarisMaster) -> Unit,
        private val onTelepon: (String) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(baris: BarisMaster) {
            binding.textJudul.text = baris.judul
            binding.textSubjudul.text = baris.subjudul
            binding.textInfo.text = baris.info
            binding.textNonaktif.visibility = if (baris.aktif) View.GONE else View.VISIBLE

            binding.tombolNonaktifkan.visibility = if (baris.aktif) View.VISIBLE else View.GONE
            binding.tombolNonaktifkan.setOnClickListener { onNonaktifkan(baris) }

            val nomor = baris.telepon
            binding.tombolTelepon.visibility =
                if (nomor.isNullOrBlank()) View.GONE else View.VISIBLE
            binding.tombolTelepon.setOnClickListener { nomor?.let(onTelepon) }

            binding.root.setOnClickListener { onUbah(baris) }
        }
    }

    private object Pembanding : DiffUtil.ItemCallback<BarisMaster>() {

        override fun areItemsTheSame(oldItem: BarisMaster, newItem: BarisMaster): Boolean =
            oldItem.tipe == newItem.tipe && oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: BarisMaster, newItem: BarisMaster): Boolean =
            oldItem == newItem
    }
}
