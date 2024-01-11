package com.example.gemini.PhotoReasoning

sealed class PhotoUiState {
    data object Initial:PhotoUiState()
    data object Loading:PhotoUiState()
    data class Success(val output:String):PhotoUiState()
    data class Error(val error:String):PhotoUiState()
}