package com.dicoding.picodiploma.loginwithanimation.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import com.dicoding.picodiploma.loginwithanimation.data.remote.responses.LoginResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    fun register(
        name: String,
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val message = userRepository.register(name, email, password)
                onResult(true, message.toString())
            } catch (e: Exception) {
                onResult(false, e.message ?: "Registration failed")
            }
        }
    }

    fun login(email: String, password: String): LiveData<Result<LoginResult>> {
        return userRepository.login(email, password).asLiveData()
    }

    fun saveToken(token: String) {
        viewModelScope.launch {
            try {
                userRepository.saveToken(token)
            } catch (e: Exception) {
                // To Do
            }
        }
    }

    fun getUserSession(onResult: (UserModel?) -> Unit) {
        viewModelScope.launch {
            val userSession = userRepository.getUserSession().first()
            onResult(userSession)
        }
    }

    fun saveSession(userModel: UserModel) = viewModelScope.launch {
        userRepository.saveSession(userModel)
    }

}