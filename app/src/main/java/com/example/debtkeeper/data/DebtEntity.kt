package com.example.debtkeeper.data

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.example.debtkeeper.DebtMode

@Entity(tableName = "deudas")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nombre: String,
    val totalDeuda: Double,
    val restante: Double,
    val tasaInteres: Double = 0.0,
    val tipoInteres: String = "Mensual",
    val plazoPagos: Int,
    val interesAcumulado: Double = 0.0,
    val interesAplicado: Boolean = false,
    val saldada: Boolean = false,
    @ColumnInfo(defaultValue = "'por_cobrar'")
    val modo: String = DebtMode.POR_COBRAR.storageValue

    //var fechaCreacion: LocalDate,
    //var ultimaActualizacionInteres: LocalDate? = null,
)
