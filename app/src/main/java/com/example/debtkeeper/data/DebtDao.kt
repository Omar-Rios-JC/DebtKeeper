package com.example.debtkeeper.data

import androidx.room.*

@Dao
interface DebtDao {

    @Query("SELECT * FROM deudas")
    suspend fun getAll(): List<DebtEntity>

    @Insert
    suspend fun insert(deuda: DebtEntity)

    @Update
    suspend fun update(deuda: DebtEntity)

    @Delete
    suspend fun delete(deuda: DebtEntity)
}
