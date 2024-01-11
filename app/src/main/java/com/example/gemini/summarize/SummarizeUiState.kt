package com.example.gemini.summarize

sealed class SummarizeUiState {
    data object Initial:SummarizeUiState()
    data object Loading:SummarizeUiState()
    data class Success(val output:String):SummarizeUiState()
    data class Error(val error:String):SummarizeUiState()
}