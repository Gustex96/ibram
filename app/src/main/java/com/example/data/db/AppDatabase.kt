package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.HorseInspection

@Database(entities = [HorseInspection::class], version = 10, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun horseInspectionDao(): HorseInspectionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE horse_inspections ADD COLUMN inspectionTeam TEXT NOT NULL DEFAULT 'Brasília Ambiental / Fiscalização DF'")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE horse_inspections ADD COLUMN quadra TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE horse_inspections ADD COLUMN conjunto TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE horse_inspections ADD COLUMN numero TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "horse_inspection_database"
                )
                    .addMigrations(MIGRATION_8_9, MIGRATION_9_10)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
