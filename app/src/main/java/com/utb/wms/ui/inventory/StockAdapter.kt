package com.utb.wms.ui.inventory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.utb.wms.R
import com.utb.wms.databinding.ItemStokBinding
import com.utb.wms.domain.model.Stock

class StockAdapter : ListAdapter<Stock, StockAdapter.StokViewHolder>(Pembanding) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StokViewHolder {
        val binding = ItemStokBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return StokViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StokViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StokViewHolder(
        private val binding: ItemStokBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(stok: Stock) {
            val konteks = binding.root.context
            binding.textNama.text = stok.item.nama
            binding.textSku.text = stok.item.sku
            binding.textLokasi.text = konteks.getString(
                R.string.stok_lokasi,
                stok.location.nama,
                stok.location.kode,
            )
            binding.textJumlah.text = konteks.getString(
                R.string.stok_nilai,
                stok.jumlahStok,
                stok.item.satuan,
            )
            binding.textMinimum.text = konteks.getString(R.string.stok_minimum, stok.item.stokMinimum)

            val atribut = if (stok.dibawahMinimum) {
                com.google.android.material.R.attr.colorError
            } else {
                com.google.android.material.R.attr.colorPrimary
            }
            binding.textJumlah.setTextColor(MaterialColors.getColor(binding.root, atribut))
            binding.textPeringatan.visibility =
                if (stok.dibawahMinimum) View.VISIBLE else View.GONE
        }
    }

    private object Pembanding : DiffUtil.ItemCallback<Stock>() {

        override fun areItemsTheSame(oldItem: Stock, newItem: Stock): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Stock, newItem: Stock): Boolean =
            oldItem == newItem
    }
}
