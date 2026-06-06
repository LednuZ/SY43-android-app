package com.example.whereami.util

import io.ktor.client.plugins.ClientRequestException

sealed class AppError {
    object InvalidCredentials : AppError()
    object PermissionDenied : AppError()
    object NetworkFailure : AppError()
    data class Unknown(val rawMessage: String) : AppError()

    fun toUserMessage(): String {
        return when (this) {
            is InvalidCredentials -> "The username or password you entered is incorrect."
            is PermissionDenied -> "You don't have permission to perform this action."
            is NetworkFailure -> "Please check your internet connection and try again."
            is Unknown -> "An unexpected error occurred: $rawMessage"
        }
    }
}

fun Throwable.toAppError(): AppError {
    val message = this.message ?: ""
    return when {
        message.contains("Invalid login credentials", ignoreCase = true) -> AppError.InvalidCredentials
        message.contains("violates row-level security", ignoreCase = true) -> AppError.PermissionDenied
        this is java.net.UnknownHostException -> AppError.NetworkFailure
        this is ClientRequestException -> {
            if (this.response.status.value == 401 || this.response.status.value == 403) {
                AppError.PermissionDenied
            } else {
                AppError.Unknown(message)
            }
        }
        else -> AppError.Unknown(message)
    }
}
