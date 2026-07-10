package com.utb.wms.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.utb.wms.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginState(
    val username: String = "",
    val password: String = "",
    val sedangMemuat: Boolean = false,
    val galat: String? = null,
) {
    val dapatMasuk: Boolean
        get() = username.isNotBlank() && password.isNotBlank() && !sedangMemuat
}

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())

    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onUsernameChange(nilai: String) {
        _state.update { it.copy(username = nilai, galat = null) }
    }

    fun onPasswordChange(nilai: String) {
        _state.update { it.copy(password = nilai, galat = null) }
    }

    fun login(onBerhasil: () -> Unit) {
        if (!_state.value.dapatMasuk) return

        viewModelScope.launch {
            _state.update { it.copy(sedangMemuat = true, galat = null) }

            val pengguna = authRepository.login(
                username = _state.value.username.trim(),
                password = _state.value.password,
            )

            if (pengguna == null) {
                _state.update {
                    it.copy(sedangMemuat = false, galat = "Username atau kata sandi salah")
                }
            } else {
                _state.update { it.copy(sedangMemuat = false) }
                onBerhasil()
            }
        }
    }

    companion object {

        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { LoginViewModel(authRepository) }
        }
    }
}
