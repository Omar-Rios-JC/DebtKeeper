package com.example.debtkeeper.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val deudaId: Int,          // FK lógica
    val monto: Double,
    val fecha: String
)
