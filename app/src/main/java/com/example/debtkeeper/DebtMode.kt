package com.example.debtkeeper

enum class DebtMode(val storageValue: String) {
    POR_COBRAR("por_cobrar"),
    POR_PAGAR("por_pagar");

    companion object {
        fun fromStorage(value: String?): DebtMode =
            values().firstOrNull { it.storageValue == value } ?: POR_COBRAR
    }
}
