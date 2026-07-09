package com.utb.wms.domain.repository

import com.utb.wms.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {

    val currentUser: StateFlow<User?>

    suspend fun login(username: String, password: String): User?

    fun logout()
}
