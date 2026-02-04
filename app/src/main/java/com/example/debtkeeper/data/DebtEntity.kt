package com.example.debtkeeper.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deudas")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nombre: String,
    val totalDeuda: Double,
    val restante: Double,
    val saldada: Boolean = false
)
