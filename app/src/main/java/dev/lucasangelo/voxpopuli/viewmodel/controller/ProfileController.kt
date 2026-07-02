package dev.lucasangelo.voxpopuli.viewmodel.controller

import dev.lucasangelo.voxpopuli.data.AppRepository
import dev.lucasangelo.voxpopuli.data.datastore.Profile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileController(
    private val repository: AppRepository,
    private val scope: CoroutineScope
) {
    val profile: StateFlow<Profile?> = repository.profile
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    fun updateProfile(profile: Profile) = scope.launch {
        repository.updateProfile(profile)
    }
}