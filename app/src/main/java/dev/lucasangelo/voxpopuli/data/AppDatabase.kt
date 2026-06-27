package dev.lucasangelo.voxpopuli.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SourceEntity::class,
        PostEntity::class,
    ],
    version = 1,
    autoMigrations = [],
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract val dao: AppDao
}