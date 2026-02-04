package com.example.debtkeeper.data

import android.content.Context
import androidx.room.Room

object DatabaseModule {

    @Volatile
    private var db: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return db ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "debtkeeper_db"
            )
                .fallbackToDestructiveMigration()
                .build()


            db = instance
            instance
        }
    }
}
