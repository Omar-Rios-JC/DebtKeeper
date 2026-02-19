package com.example.debtkeeper.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        DebtEntity::class,
        PaymentEntity::class   // 👈 SE AGREGA
    ],
    version = 3               // 👈 SE SUBE LA VERSIÓN
)
//@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun debtDao(): DebtDao
    abstract fun paymentDao(): PaymentDao
}
