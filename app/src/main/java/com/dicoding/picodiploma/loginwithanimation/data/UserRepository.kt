package com.dicoding.picodiploma.loginwithanimation.data

import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.remote.AuthApiService
import com.dicoding.picodiploma.loginwithanimation.data.remote.responses.ErrorResponse
import com.dicoding.picodiploma.loginwithanimation.data.remote.responses.LoginResult
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val authApiService: AuthApiService
) {

    suspend fun register(name: String, email: String, password: String): Result<String> {
        return try {
            val response = authApiService.register(name, email, password)
            val message = response.message ?: "Pendaftaran gagal, coba lagi nanti."
            Result.success(message)

        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            val errorMessage = errorResponse?.message ?: "Gagal mendaftar, coba lagi nanti."
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(Exception("Terjadi kesalahan: ${e.message}"))
        }
    }


    fun login(email: String, password: String): Flow<Result<LoginResult>> = flow {
        emit(Result.Loading)
        try {
            val response = authApiService.login(email, password)
            val result = response.loginResult
            saveToken(result.token)
            emit(Result.Success(result))
        } catch (e: HttpException) {
            emit(Result.Error(e.message()))
        }
    }

    suspend fun saveToken(token: String) {
        userPreference.saveToken(token)
    }

    fun getUserSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            authApiService: AuthApiService
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference, authApiService)
            }.also { instance = it }
    }

    suspend fun logout() {
        userPreference.logout()
    }
}