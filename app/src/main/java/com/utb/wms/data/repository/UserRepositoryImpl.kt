package com.utb.wms.data.repository

import com.utb.wms.data.local.dao.UserDao
import com.utb.wms.domain.model.Role
import com.utb.wms.domain.model.User
import com.utb.wms.domain.repository.RoleResult
import com.utb.wms.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl(
    private val userDao: UserDao,
) : UserRepository {

    override fun observeUsers(): Flow<List<User>> =
        TODO("BE-2: pakai userDao.observeUsers()")

    override suspend fun findUser(id: String): User? =
        TODO("BE-2: pakai userDao.findById(id)")

    override suspend fun simpanUser(user: User, password: String?) {
        TODO("BE-2: password null berarti pengguna lama, pertahankan kata sandi tersimpan")
    }

    override suspend fun ubahStatusAktif(id: String, aktif: Boolean) {
        TODO("BE-2: pakai userDao.updateAktif(id, aktif)")
    }

    override fun observeRoles(): Flow<List<Role>> =
        TODO("BE-2: pakai userDao.observeRoles()")

    override suspend fun simpanRole(role: Role) {
        TODO("BE-2: pakai userDao.upsertRole()")
    }

    override suspend fun hapusRole(id: String): RoleResult =
        TODO("BE-2: tolak dengan MasihDipakai bila countUsersWithRole(id) > 0")
}
