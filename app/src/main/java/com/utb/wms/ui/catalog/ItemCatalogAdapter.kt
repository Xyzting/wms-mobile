package com.utb.wms.ui.catalog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.utb.wms.R
import com.utb.wms.databinding.ItemKatalogBinding
import com.utb.wms.domain.model.Item

class ItemCatalogAdapter :
    ListAdapter<Item, ItemCatalogAdapter.ItemViewHolder>(Pembanding) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemKatalogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ItemViewHolder(
        private val binding: ItemKatalogBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item) {
            val konteks = binding.root.context
            binding.textNama.text = item.nama
            binding.textSku.text = item.sku
            binding.textDetail.text = konteks.getString(R.string.katalog_satuan, item.satuan) +
                " · " + konteks.getString(R.string.katalog_stok_minimum, item.stokMinimum)
            binding.textBarcode.text = item.barcode
                ?.let { konteks.getString(R.string.katalog_barcode, it) }
                ?: konteks.getString(R.string.katalog_tanpa_barcode)
        }
    }

    private object Pembanding : DiffUtil.ItemCallback<Item>() {

        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean =
            oldItem.sku == newItem.sku

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean =
            oldItem == newItem
    }
}
