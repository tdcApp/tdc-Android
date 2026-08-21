package com.bagadbille.tdc.ui.home.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.model.ClassDetail
import com.bagadbille.tdc.data.model.ClassInfo
import com.bagadbille.tdc.data.repository.ClassRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ClassesUiState {
    data object Loading : ClassesUiState()
    data class Success(val classes: List<ClassInfo>) : ClassesUiState()
    data class Error(val message: String) : ClassesUiState()
}

@HiltViewModel
class ClassesViewModel @Inject constructor(private val repo: ClassRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<ClassesUiState>(ClassesUiState.Loading)
    val uiState: StateFlow<ClassesUiState> = _uiState.asStateFlow()
    init { loadClasses() }
    fun loadClasses() {
        viewModelScope.launch {
            _uiState.value = ClassesUiState.Loading
            repo.getClasses()
                .onSuccess { _uiState.value = ClassesUiState.Success(it) }
                .onFailure { _uiState.value = ClassesUiState.Error(it.message ?: "Failed to load") }
        }
    }
}

sealed class ClassDetailUiState {
    data object Loading : ClassDetailUiState()
    data class Success(val detail: ClassDetail) : ClassDetailUiState()
    data class Error(val message: String) : ClassDetailUiState()
}

@HiltViewModel
class ClassDetailViewModel @Inject constructor(private val repo: ClassRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<ClassDetailUiState>(ClassDetailUiState.Loading)
    val uiState: StateFlow<ClassDetailUiState> = _uiState.asStateFlow()
    fun loadClassDetail(classId: String) {
        viewModelScope.launch {
            _uiState.value = ClassDetailUiState.Loading
            repo.getClassDetail(classId)
                .onSuccess { _uiState.value = ClassDetailUiState.Success(it) }
                .onFailure { _uiState.value = ClassDetailUiState.Error(it.message ?: "Failed to load") }
        }
    }
}
