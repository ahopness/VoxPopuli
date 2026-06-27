package dev.lucasangelo.voxpopuli.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.lucasangelo.voxpopuli.data.datastore.Profile
import dev.lucasangelo.voxpopuli.data.datastore.ProfileSerializer
import dev.lucasangelo.voxpopuli.data.datastore.Settings
import dev.lucasangelo.voxpopuli.data.datastore.SettingsSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun provideSettingDataStore(
        @ApplicationContext context: Context
    ): DataStore<Settings> {
        return DataStoreFactory.create(
            serializer = SettingsSerializer,
            produceFile = { context.dataStoreFile("settings.json") },
            corruptionHandler = null,
            migrations = emptyList(),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        )
    }

    @Provides
    @Singleton
    fun provideProfileDataStore(
        @ApplicationContext context: Context
    ): DataStore<Profile> {
        return DataStoreFactory.create(
            serializer = ProfileSerializer,
            produceFile = { context.dataStoreFile("profile.json") },
            corruptionHandler = null,
            migrations = emptyList(),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        )
    }
}