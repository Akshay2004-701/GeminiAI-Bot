package com.example.gemini.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gemini.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel :ViewModel(){

    private val config = generationConfig {
        temperature = 0.7f
    }
    private val generativeModel = GenerativeModel(
        modelName = MODEL_NAME,
        apiKey = BuildConfig.apikey,
        generationConfig = config
    )

    private val chat = generativeModel.startChat(
        history = listOf(
//            content(role = "model"){
//                text("Hello, this is Gemini, How may i help you?")// this is the first part
//            }
        )
    )

    private val _uiState:MutableStateFlow<ChatUiState> = MutableStateFlow(
        ChatUiState(
            messages = listOf(
                ChatMessage(
                    text = "Hi, I am gemini, how may I help you?",
                    participant = Participant.MODEL,
                    isPending = false
                )
            )
        )
    )

    val uiState:StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun sendMessage(userMsg:String){
        // add pending message
        _uiState.value.addMessage(
            ChatMessage(
                text = userMsg,
                participant = Participant.USER,
                isPending = true
            )
        )

        viewModelScope.launch {
            try {
                val response = chat.sendMessage(userMsg)
                _uiState.value.replaceLastPendingMessage()

                response.text?.let {msg->
                    _uiState.value.addMessage(
                        ChatMessage(
                            text = msg,
                            participant = Participant.MODEL,
                            isPending = false
                        )
                    )
                }

            }
            catch (e:Exception){
                _uiState.value.replaceLastPendingMessage()
                _uiState.value.addMessage(
                    ChatMessage(
                        text = e.message?:"Unknown error has occurred",
                        participant = Participant.ERROR,
                        isPending = false
                    )
                )
            }
        }
    }
    companion object{
        const val MODEL_NAME = "gemini-pro"
    }
}