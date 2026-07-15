package com.example.freshcontroll.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcontroll.domain.model.User
import com.example.freshcontroll.domain.repository.AuthRepository
import com.example.freshcontroll.domain.usecase.profile.GetEmployeesUseCase
import com.example.freshcontroll.domain.usecase.profile.ToggleEmployeeAccessUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel utilizado por el Dueño para listar a sus empleados y
 * activar o desactivar su acceso al sistema en tiempo real.
 */
@HiltViewModel
class EmployeeManagementViewModel @Inject constructor(
    private val getEmployeesUseCase: GetEmployeesUseCase,
    private val toggleEmployeeAccessUseCase: ToggleEmployeeAccessUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _employeeList = MutableStateFlow<List<User>>(emptyList())
    val employeeList: StateFlow<List<User>> = _employeeList.asStateFlow()

    init {
        loadEmployees()
    }

    fun loadEmployees() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser() ?: return@launch

            getEmployeesUseCase(currentUser.storeId).collect { employees ->
                _employeeList.value = employees
            }
        }
    }

    fun onAccessToggleChanged(userId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            // No es necesario actualizar la lista manualmente. Room detectará el cambio
            // y emitirá un nuevo flujo hacia GetEmployeesUseCase automáticamente.
            toggleEmployeeAccessUseCase(userId, isEnabled)
        }
    }
}