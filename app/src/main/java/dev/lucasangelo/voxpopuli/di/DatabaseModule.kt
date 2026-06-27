package dev.lucasangelo.voxpopuli.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.lucasangelo.voxpopuli.data.AppDao
import dev.lucasangelo.voxpopuli.data.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "vox-populi.db"
        )
        .createFromAsset("vox-populi.sqlite")
        .build()
    }

    @Provides
    @Singleton
    fun provideDao(
        database: AppDatabase
    ): AppDao {
        return database.dao
    }
}