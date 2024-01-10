package com.example.gemini.summarize

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gemini.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MenuViewModel:ViewModel() {

    private var _uiState = MutableStateFlow<MenuUiState>(MenuUiState.Initial)
    val uiState = _uiState.asStateFlow()

    private val config = generationConfig {
        temperature = 0.7f
    }

    private val generativeModel = GenerativeModel(
        modelName = MODEL_NAME,
        apiKey = BuildConfig.apikey,
        generationConfig = config
    )

    fun prompt(userInput:String){
        _uiState.value = MenuUiState.Loading

        val prompt = "Summarize the following text for me: ${userInput.trim()}"

        viewModelScope.launch(Dispatchers.IO){
            try {

// this can be used to generate out stream of text
//                generativeModel.generateContentStream(prompt).collect{
//                    it.text?.let { output->
//                        _uiState.value = MenuUiState.Success(output)
//                    }
//                }
                generativeModel.generateContent(prompt)
                    .text?.let {
                        _uiState.value = MenuUiState.Success(it)
                    }
            }
            catch (e:Exception){
                _uiState.value = MenuUiState.Error(
                    e.message?:"Unknown error has occurred"
                )
            }
        }
    }


    companion object{
        const val MODEL_NAME = "gemini-pro"
    }
}