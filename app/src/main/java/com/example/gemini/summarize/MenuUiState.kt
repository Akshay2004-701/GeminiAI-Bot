package com.example.gemini.summarize

sealed class MenuUiState {
    data object Initial:MenuUiState()
    data object Loading:MenuUiState()
    data class Success(val output:String):MenuUiState()
    data class Error(val error:String):MenuUiState()
}