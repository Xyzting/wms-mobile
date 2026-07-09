package com.utb.wms.domain.model

object NamaRole {
    const val ADMIN = "Admin"
    const val OPERATOR = "Operator"
    const val SUPERVISOR = "Supervisor"
}

val User.bolehMenyetujui: Boolean
    get() = role.namaRole == NamaRole.ADMIN || role.namaRole == NamaRole.SUPERVISOR

val User.bolehMengelolaPengguna: Boolean
    get() = role.namaRole == NamaRole.ADMIN

val User.bolehMengelolaMasterData: Boolean
    get() = role.namaRole == NamaRole.ADMIN

val User.bolehMenyesuaikanStok: Boolean
    get() = role.namaRole == NamaRole.ADMIN || role.namaRole == NamaRole.SUPERVISOR

val User.bolehMembuatDokumen: Boolean
    get() = role.namaRole == NamaRole.ADMIN || role.namaRole == NamaRole.OPERATOR
