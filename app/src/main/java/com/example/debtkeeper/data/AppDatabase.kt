package com.example.debtkeeper.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        DebtEntity::class,
        PaymentEntity::class   // 👈 SE AGREGA
    ],
    version = 2               // 👈 SE SUBE LA VERSIÓN
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun debtDao(): DebtDao
    abstract fun paymentDao(): PaymentDao
}
