package com.example.debtkeeper.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    @Insert
    suspend fun insert(payment: PaymentEntity)

    @Query("SELECT * FROM payments WHERE deudaId = :deudaId")
    fun getPagosPorDeuda(deudaId: Int): Flow<List<PaymentEntity>>
}
