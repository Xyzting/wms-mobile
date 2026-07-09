package com.utb.wms.data.repository

import com.utb.wms.data.local.dao.UserDao
import com.utb.wms.data.local.mapper.toDomain
import com.utb.wms.domain.model.User
import com.utb.wms.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepositoryImpl(
    private val userDao: UserDao,
) : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)

    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    override suspend fun login(username: String, password: String): User? {
        val baris = userDao.findByUsername(username) ?: return null
        if (!baris.user.aktif) return null
        if (baris.user.password != password) return null
        return baris.toDomain().also { _currentUser.value = it }
    }

    override fun logout() {
        _currentUser.value = null
    }
}
