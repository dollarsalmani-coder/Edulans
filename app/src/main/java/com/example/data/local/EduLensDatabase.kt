package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SavedStudyEntity::class], version = 1, exportSchema = false)
abstract class EduLensDatabase : RoomDatabase() {
    abstract fun savedStudyDao(): SavedStudyDao

    companion object {
        @Volatile
        private var instance: EduLensDatabase? = null

        fun getInstance(context: Context): EduLensDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    EduLensDatabase::class.java,
                    "edulens_offline_studies.db"
                ).build().also { instance = it }
            }
        }
    }
}
