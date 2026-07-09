package com.utb.wms.data.repository

import com.utb.wms.domain.model.Role
import com.utb.wms.domain.model.User
import com.utb.wms.domain.repository.RoleResult
import com.utb.wms.domain.repository.UserRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserRepositoryTest {

    private val userDao = FakeUserDao(
        rolesAwal = Contoh.roles,
        penggunaAwal = Contoh.users,
    )

    private val repository: UserRepository = UserRepositoryImpl(userDao)

    @Test
    fun `role yang masih dipakai tidak boleh dihapus`() = runTest {
        val hasil = repository.hapusRole("R-02")

        assertEquals(RoleResult.MasihDipakai(1), hasil)
        assertEquals(3, userDao.roles.size)
    }

    @Test
    fun `role yang tidak dipakai boleh dihapus`() = runTest {
        val hasil = repository.hapusRole("R-01")

        assertEquals(RoleResult.Success, hasil)
        assertEquals(2, userDao.roles.size)
        assertNull(userDao.roles.firstOrNull { it.id == "R-01" })
    }

    @Test
    fun `menyimpan pengguna lama tanpa kata sandi mempertahankan sandi tersimpan`() = runTest {
        val lama = repository.findUser("U-02")!!

        repository.simpanUser(lama.copy(nama = "Budi Santosa"), password = null)

        val sesudah = repository.findUser("U-02")!!
        assertEquals("Budi Santosa", sesudah.nama)
        assertEquals("operator123", sesudah.password)
    }

    @Test
    fun `menyimpan pengguna lama dengan kata sandi baru menimpa sandi lama`() = runTest {
        val lama = repository.findUser("U-02")!!

        repository.simpanUser(lama, password = "rahasia456")

        assertEquals("rahasia456", repository.findUser("U-02")!!.password)
    }

    @Test
    fun `pengguna baru tanpa kata sandi ditolak`() = runTest {
        val baru = User(
            id = "U-09",
            username = "gudang",
            password = "",
            nama = "Petugas Gudang",
            role = Role("R-02", "Operator"),
        )

        val gagal = runCatching { repository.simpanUser(baru, password = null) }

        assertTrue(gagal.exceptionOrNull() is IllegalArgumentException)
        assertNull(repository.findUser("U-09"))
    }

    @Test
    fun `menonaktifkan pengguna mengubah status aktif`() = runTest {
        repository.ubahStatusAktif("U-02", aktif = false)

        assertEquals(false, repository.findUser("U-02")!!.aktif)
    }
}
