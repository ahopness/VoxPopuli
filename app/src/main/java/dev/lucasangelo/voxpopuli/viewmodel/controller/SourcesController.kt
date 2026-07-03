package dev.lucasangelo.voxpopuli.viewmodel.controller

import dev.lucasangelo.voxpopuli.data.AppRepository
import dev.lucasangelo.voxpopuli.data.room.SourceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SourcesController(
    private val repository: AppRepository,
    private val scope: CoroutineScope,
) {
    val sources: StateFlow<List<SourceEntity>> = repository.getAllSources()
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertSource(source: SourceEntity) = scope.launch { repository.insertSource(source) }
    fun updateSource(source: SourceEntity) = scope.launch { repository.updateSource(source) }
    fun deleteSource(source: SourceEntity) = scope.launch { repository.deleteSource(source) }
}