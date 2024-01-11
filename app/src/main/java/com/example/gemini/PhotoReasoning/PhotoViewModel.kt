package com.example.gemini.PhotoReasoning

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gemini.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PhotoViewModel: ViewModel() {
    // model name is different for photo reasoning
    private val config = generationConfig {
        temperature = 0.7f
    }
    private val generativeModel = GenerativeModel(
        modelName = MODEL_NAME,
        apiKey = BuildConfig.apikey,
        generationConfig = config
    )

    private val _uiState : MutableStateFlow<PhotoUiState> =
        MutableStateFlow(PhotoUiState.Initial)
    val uiState = _uiState.asStateFlow()

    fun reason(
        userInput:String,
        images:List<Bitmap>
    ){
        _uiState.value = PhotoUiState.Loading
        val prompt = "Look at the following image(s) and answer the following question:$userInput"

        viewModelScope.launch(Dispatchers.IO){
            try{
                val inputContent = content {
                    for (bitmap in images) {
                        image(bitmap)
                    }
                    text(prompt)
                }

                var output = ""
                generativeModel.generateContentStream(inputContent).collect { response ->
                    response.text?.let { responseText ->
                        output += responseText
                        _uiState.value = PhotoUiState.Success(output)
                    }
                }
            }catch (e:Exception){
                _uiState.value = PhotoUiState.Error(
                    e.message?:"Unknown error"
                )
            }
        }

    }
    companion object{
        const val MODEL_NAME = "gemini-pro-vision"
    }
}