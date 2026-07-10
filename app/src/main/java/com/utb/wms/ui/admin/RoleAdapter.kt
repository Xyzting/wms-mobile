package com.utb.wms.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.utb.wms.R
import com.utb.wms.databinding.ItemRoleBinding
import com.utb.wms.domain.model.Role

data class BarisRole(
    val role: Role,
    val jumlahPengguna: Int,
)

class RoleAdapter(
    private val onUbah: (Role) -> Unit,
    private val onHapus: (Role) -> Unit,
) : ListAdapter<BarisRole, RoleAdapter.RoleViewHolder>(Pembanding) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoleViewHolder {
        val binding = ItemRoleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return RoleViewHolder(binding, onUbah, onHapus)
    }

    override fun onBindViewHolder(holder: RoleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RoleViewHolder(
        private val binding: ItemRoleBinding,
        private val onUbah: (Role) -> Unit,
        private val onHapus: (Role) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(baris: BarisRole) {
            binding.textNama.text = baris.role.namaRole
            binding.textKode.text = baris.role.id
            binding.textJumlah.text = binding.root.context.getString(
                R.string.role_jumlah_pengguna,
                baris.jumlahPengguna,
            )
            binding.tombolHapus.setOnClickListener { onHapus(baris.role) }
            binding.root.setOnClickListener { onUbah(baris.role) }
        }
    }

    private object Pembanding : DiffUtil.ItemCallback<BarisRole>() {

        override fun areItemsTheSame(oldItem: BarisRole, newItem: BarisRole): Boolean =
            oldItem.role.id == newItem.role.id

        override fun areContentsTheSame(oldItem: BarisRole, newItem: BarisRole): Boolean =
            oldItem == newItem
    }
}
