package com.utb.wms.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.utb.wms.domain.model.Role
import com.utb.wms.domain.model.User
import com.utb.wms.domain.repository.RoleResult
import com.utb.wms.domain.repository.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface PesanAdmin {

    data class Galat(val pesan: String) : PesanAdmin

    data class RoleMasihDipakai(val jumlahPengguna: Int) : PesanAdmin

    data object RoleTerhapus : PesanAdmin

    data object Tersimpan : PesanAdmin
}

class AdminViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _pesan = Channel<PesanAdmin>(Channel.BUFFERED)

    val pesan: Flow<PesanAdmin> = _pesan.receiveAsFlow()

    val users: StateFlow<List<User>> = userRepository.observeUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val roles: StateFlow<List<Role>> = userRepository.observeRoles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun simpanUser(user: User, password: String?) {
        viewModelScope.launch {
            runCatching { userRepository.simpanUser(user, password) }
                .onSuccess { _pesan.send(PesanAdmin.Tersimpan) }
                .onFailure { kirimGalat(it) }
        }
    }

    fun ubahStatusAktif(id: String, aktif: Boolean) {
        viewModelScope.launch {
            runCatching { userRepository.ubahStatusAktif(id, aktif) }
                .onFailure { kirimGalat(it) }
        }
    }

    fun simpanRole(role: Role) {
        viewModelScope.launch {
            runCatching { userRepository.simpanRole(role) }
                .onSuccess { _pesan.send(PesanAdmin.Tersimpan) }
                .onFailure { kirimGalat(it) }
        }
    }

    fun hapusRole(id: String) {
        viewModelScope.launch {
            runCatching { userRepository.hapusRole(id) }
                .onSuccess { hasil ->
                    when (hasil) {
                        is RoleResult.MasihDipakai ->
                            _pesan.send(PesanAdmin.RoleMasihDipakai(hasil.jumlahPengguna))

                        RoleResult.Success -> _pesan.send(PesanAdmin.RoleTerhapus)
                    }
                }
                .onFailure { kirimGalat(it) }
        }
    }

    fun jumlahPenggunaDenganRole(roleId: String): Int =
        users.value.count { it.role.id == roleId }

    private suspend fun kirimGalat(penyebab: Throwable) {
        _pesan.send(PesanAdmin.Galat(penyebab.message ?: "Perubahan gagal disimpan"))
    }

    companion object {

        fun factory(userRepository: UserRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { AdminViewModel(userRepository) }
        }
    }
}
