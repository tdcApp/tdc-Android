package com.bagadbille.tdc.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.local.DataStoreManager
import com.bagadbille.tdc.data.model.isMentorRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    dataStoreManager: DataStoreManager
) : ViewModel() {
    /** null until the stored role is read, so the student UI never flashes for mentors. */
    val isMentor: StateFlow<Boolean?> = dataStoreManager.userRole
        .map { isMentorRole(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
