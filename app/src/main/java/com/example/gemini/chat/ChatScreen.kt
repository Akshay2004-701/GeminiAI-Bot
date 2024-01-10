package com.example.gemini.chat

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gemini.ui.theme.GeminiTheme
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: ChatViewModel  = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

   Scaffold(
       bottomBar = {
           MessageInput(
               onSendMessage = viewModel::sendMessage,
               resetScroll = {
                   scope.launch {
                       listState.scrollToItem(0)
                   }
               }
           )
       },
       containerColor = Color.Black
   ) {
       Column (
           modifier = Modifier
               .padding(it)
               .fillMaxSize()

       ){
           ChatList(messageList = uiState.messages, listState =listState)
       }
   }

}

@Composable
fun ChatList(
    messageList:List<ChatMessage>,
    listState:LazyListState
) {
    LazyColumn(
        state = listState,
        reverseLayout = true
    ){
        items(messageList.reversed()){message->
            ChatItem(chatMessage = message)
        }
    }
}

@Composable
fun ChatItem(
    chatMessage: ChatMessage
) {
    val isModel = chatMessage.participant == Participant.MODEL ||
                  chatMessage.participant == Participant.ERROR

    val itemShape = if (isModel){
        RoundedCornerShape(4.dp,20.dp,20.dp,20.dp)
    }else{
        RoundedCornerShape(20.dp,4.dp,20.dp,20.dp)
    }

    val bgColor = when(chatMessage.participant){
        Participant.USER->{
            Color(147, 34, 153)
        }
        Participant.MODEL->{
            Color(235, 146, 240)
        }
        Participant.ERROR->{
            MaterialTheme.colorScheme.errorContainer
        }
    }

    val horizontalAlignment = if (isModel){
        Alignment.Start
    }else{
        Alignment.End
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = horizontalAlignment
    ) {
        Text(
            text = chatMessage.participant.name,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Row {
            if (chatMessage.isPending){
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterVertically)
                )
            }
            BoxWithConstraints {
                Card(
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    shape = itemShape,
                    modifier = Modifier.widthIn(0.dp,maxWidth*0.9f)
                ) {
                    Text(
                        text = chatMessage.text,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MessageInput(
    onSendMessage: (String) -> Unit,
    resetScroll: () -> Unit = {}
) {
    var userMessage by rememberSaveable { mutableStateOf("") }
    val ctx = LocalContext.current
    val kb = LocalSoftwareKeyboardController.current

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(21, 5, 43)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = userMessage,
                label = { Text(text = "prompt") },
                onValueChange = { userMessage = it },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(235, 146, 240),
                    focusedLabelColor = Color(235, 146, 240)
                ),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()
                    .weight(0.85f)
            )
            IconButton(
                onClick = {
                    if (userMessage.isNotBlank()) {
                        onSendMessage(userMessage)
                        userMessage = ""
                        resetScroll()
                        kb?.hide()
                    }
                    else{
                        Toast.makeText(
                            ctx,
                            "Invalid Input",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .padding(start = 16.dp)
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()
                    .weight(0.15f),
                colors = IconButtonDefaults.filledIconButtonColors(
                    contentColor = Color.White
                )
            ) {
                Icon(
                    Icons.Filled.Send,
                    contentDescription = "send",
                    modifier = Modifier
                )
            }
        }
    }
}


@Preview(showSystemUi = true, showBackground = true)
@Composable
fun Items() {
    GeminiTheme {
ChatScreen()
    }
}