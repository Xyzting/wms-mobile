package com.utb.wms.domain.repository

import com.utb.wms.domain.model.Role
import com.utb.wms.domain.model.User
import kotlinx.coroutines.flow.Flow

sealed interface RoleResult {

    data object Success : RoleResult

    data class MasihDipakai(val jumlahPengguna: Int) : RoleResult
}

interface UserRepository {

    fun observeUsers(): Flow<List<User>>

    suspend fun findUser(id: String): User?

    suspend fun simpanUser(user: User, password: String?)

    suspend fun ubahStatusAktif(id: String, aktif: Boolean)

    fun observeRoles(): Flow<List<Role>>

    suspend fun simpanRole(role: Role)

    suspend fun hapusRole(id: String): RoleResult
}
