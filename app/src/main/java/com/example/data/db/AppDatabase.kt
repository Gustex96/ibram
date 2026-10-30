package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.HorseInspection

@Database(entities = [HorseInspection::class], version = 15, exportSchema = false)
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

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE horse_inspections ADD COLUMN species TEXT NOT NULL DEFAULT 'Cavalo'")
                db.execSQL("ALTER TABLE horse_inspections ADD COLUMN auditorName TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE horse_inspections ADD COLUMN requiresOtherAgencySupport INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE horse_inspections ADD COLUMN otherAgencyName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE horse_inspections ADD COLUMN otherAgencyNotes TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE horse_inspections ADD COLUMN ppbeaAnswersData TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE horse_inspections ADD COLUMN individualEvaluationsData TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE horse_inspections ADD COLUMN sampledAnimalCount INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "horse_inspection_database",
                )
                    .addMigrations(MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
