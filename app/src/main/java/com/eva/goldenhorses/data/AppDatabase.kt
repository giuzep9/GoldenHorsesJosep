package com.eva.goldenhorses.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.eva.goldenhorses.model.Jugador

@Database(entities = [Jugador::class], version = 2, exportSchema = false) // Cambia la versión
abstract class AppDatabase : RoomDatabase() {

    abstract fun jugadorDAO(): JugadorDAO

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "golden_horses_db"
                )

                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}