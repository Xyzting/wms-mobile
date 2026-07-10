package com.utb.wms.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.utb.wms.databinding.ItemMenuDashboardBinding

class MenuAdapter(
    private val onKlik: (MenuUtama) -> Unit,
) : ListAdapter<MenuUtama, MenuAdapter.MenuViewHolder>(Pembanding) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = ItemMenuDashboardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return MenuViewHolder(binding, onKlik)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MenuViewHolder(
        private val binding: ItemMenuDashboardBinding,
        private val onKlik: (MenuUtama) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(menu: MenuUtama) {
            binding.ikonMenu.setImageResource(menu.ikon)
            binding.textJudulMenu.setText(menu.judul)
            binding.textKeteranganMenu.setText(menu.keterangan)
            binding.root.setOnClickListener { onKlik(menu) }
        }
    }

    private object Pembanding : DiffUtil.ItemCallback<MenuUtama>() {

        override fun areItemsTheSame(oldItem: MenuUtama, newItem: MenuUtama): Boolean =
            oldItem.actionId == newItem.actionId

        override fun areContentsTheSame(oldItem: MenuUtama, newItem: MenuUtama): Boolean =
            oldItem == newItem
    }
}
