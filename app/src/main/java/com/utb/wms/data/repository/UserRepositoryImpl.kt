package com.utb.wms.data.repository

import com.utb.wms.data.local.dao.UserDao
import com.utb.wms.data.local.mapper.toDomain
import com.utb.wms.data.local.mapper.toEntity
import com.utb.wms.domain.model.Role
import com.utb.wms.domain.model.User
import com.utb.wms.domain.repository.RoleResult
import com.utb.wms.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepositoryImpl(
    private val userDao: UserDao,
) : UserRepository {

    override fun observeUsers(): Flow<List<User>> =
        userDao.observeUsers().map { daftar -> daftar.map { it.toDomain() } }

    override suspend fun findUser(id: String): User? =
        userDao.findById(id)?.toDomain()

    override suspend fun simpanUser(user: User, password: String?) {
        require(user.id.isNotBlank()) { "Kode pengguna wajib diisi" }
        require(user.username.isNotBlank()) { "Nama pengguna wajib diisi" }
        require(user.nama.isNotBlank()) { "Nama lengkap wajib diisi" }

        val lama = userDao.findById(user.id)
        val sandi = password?.takeIf { it.isNotBlank() } ?: lama?.user?.password
        requireNotNull(sandi) { "Kata sandi wajib diisi untuk pengguna baru" }

        val bersih = user.copy(
            username = user.username.trim(),
            nama = user.nama.trim(),
            password = sandi,
        )
        userDao.upsertUser(bersih.toEntity())
    }

    override suspend fun ubahStatusAktif(id: String, aktif: Boolean) {
        userDao.updateAktif(id, aktif)
    }

    override fun observeRoles(): Flow<List<Role>> =
        userDao.observeRoles().map { daftar -> daftar.map { it.toDomain() } }

    override suspend fun simpanRole(role: Role) {
        require(role.id.isNotBlank()) { "Kode role wajib diisi" }
        require(role.namaRole.isNotBlank()) { "Nama role wajib diisi" }

        userDao.upsertRole(role.copy(namaRole = role.namaRole.trim()).toEntity())
    }

    override suspend fun hapusRole(id: String): RoleResult {
        val jumlah = userDao.countUsersWithRole(id)
        if (jumlah > 0) return RoleResult.MasihDipakai(jumlah)

        userDao.deleteRole(id)
        return RoleResult.Success
    }
}
