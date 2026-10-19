package com.bagadbille.tdc.ui.home.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.model.ClassInfo
import com.bagadbille.tdc.data.repository.ClassRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ClassFilterTab {
    ENROLLED,
    ALL
}

data class ClassesData(
    val enrolledClasses: List<ClassInfo>,
    val allClasses: List<ClassInfo>,
    val enrolledClassIds: Set<String>,
    val selectedFilter: ClassFilterTab = ClassFilterTab.ENROLLED
)

sealed class ClassesUiState {
    data object Loading : ClassesUiState()
    data class Success(val data: ClassesData) : ClassesUiState()
    data class Error(val message: String) : ClassesUiState()
}

@HiltViewModel
class ClassesViewModel @Inject constructor(
    private val repo: ClassRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ClassesUiState>(ClassesUiState.Loading)
    val uiState: StateFlow<ClassesUiState> = _uiState.asStateFlow()

    init {
        loadClasses()
    }

    fun loadClasses() {
        viewModelScope.launch {
            _uiState.value = ClassesUiState.Loading
            val myClassesResult = repo.getMyClasses()
            val allClassesResult = repo.getClasses()

            val myClasses = myClassesResult.getOrDefault(emptyList())
            val allClasses = allClassesResult.getOrDefault(emptyList())

            if (myClasses.isEmpty() && allClasses.isEmpty()) {
                val err = myClassesResult.exceptionOrNull()?.message
                    ?: allClassesResult.exceptionOrNull()?.message
                    ?: "Failed to load classes"
                _uiState.value = ClassesUiState.Error(err)
            } else {
                val enrolledIds = myClasses.map { it.id }.toSet()
                _uiState.value = ClassesUiState.Success(
                    ClassesData(
                        enrolledClasses = myClasses,
                        allClasses = allClasses,
                        enrolledClassIds = enrolledIds,
                        selectedFilter = ClassFilterTab.ENROLLED
                    )
                )
            }
        }
    }

    fun setFilterTab(tab: ClassFilterTab) {
        val current = _uiState.value
        if (current is ClassesUiState.Success) {
            _uiState.value = ClassesUiState.Success(current.data.copy(selectedFilter = tab))
        }
    }
}
