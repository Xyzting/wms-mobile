package com.utb.wms.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.utb.wms.databinding.ItemPenggunaBinding
import com.utb.wms.domain.model.User

class UserAdapter(
    private val onUbah: (User) -> Unit,
    private val onUbahStatus: (User, Boolean) -> Unit,
) : ListAdapter<User, UserAdapter.UserViewHolder>(Pembanding) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemPenggunaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return UserViewHolder(binding, onUbah, onUbahStatus)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(
        private val binding: ItemPenggunaBinding,
        private val onUbah: (User) -> Unit,
        private val onUbahStatus: (User, Boolean) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pengguna: User) {
            binding.textNama.text = pengguna.nama
            binding.textUsername.text = pengguna.username
            binding.textRole.text = pengguna.role.namaRole

            binding.saklarAktif.setOnCheckedChangeListener(null)
            binding.saklarAktif.isChecked = pengguna.aktif
            binding.saklarAktif.setOnCheckedChangeListener { _, aktif ->
                onUbahStatus(pengguna, aktif)
            }

            binding.root.setOnClickListener { onUbah(pengguna) }
        }
    }

    private object Pembanding : DiffUtil.ItemCallback<User>() {

        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem == newItem
    }
}
