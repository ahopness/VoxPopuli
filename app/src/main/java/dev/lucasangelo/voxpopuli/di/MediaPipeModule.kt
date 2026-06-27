package dev.lucasangelo.voxpopuli.di

import android.content.Context
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaPipeModule {
    @Provides
    @Singleton
    fun provideTextEmbedder(
        @ApplicationContext context: Context
    ) : TextEmbedder {
        return TextEmbedder.createFromOptions(
            context,
            TextEmbedder.TextEmbedderOptions.builder()
                .setBaseOptions(
                    BaseOptions.builder()
                        .setDelegate(Delegate.CPU)
                        .setModelAssetPath("universal_sentence_encoder.tflite")
                        .build()
                )
                .build()
        )
    }
}