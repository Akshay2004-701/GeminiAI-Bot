package com.example.gemini.summarize

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gemini.ui.theme.GeminiTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun QuestionScreen(
    modifier: Modifier = Modifier,
    viewModel: SummarizeViewModel = viewModel(),

) {
val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var prompt by remember {
        mutableStateOf("")
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .background(color = Color.Black)
            .fillMaxSize()
    ) {

        //Text field for entering the prompt

        ElevatedCard(
            modifier = Modifier.padding(16.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = Color(58, 23, 89)
            )
        ) {
            OutlinedTextField(
                value = prompt,
                onValueChange = {prompt = it},
                label = {
                    Text(text = "prompt")
                },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    focusedLabelColor = Color.White
                )
                )
            TextButton(onClick = {
                if (prompt.isNotBlank()){
                    viewModel.prompt(prompt)
                    keyboardController?.hide()
                    prompt = ""
                }
                else{
                    Toast.makeText(
                        context,
                        "Please enter some text to generate",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.End)
            ) {
                Text(text = "Generate" , color = Color.White)
            }
        }

        when(uiState){
            is SummarizeUiState.Initial->{
                // Nothing is shown when Initial
            }

            is SummarizeUiState.Loading->{
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    CircularProgressIndicator()
                }
            }

            is SummarizeUiState.Success->{

                Card(
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(187, 137, 232)
                    ),
                    shape = MaterialTheme.shapes.large
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle ,
                            contentDescription = "Model",
                            )
                        Text(
                            text = (uiState as SummarizeUiState.Success).output,
                            modifier = Modifier.padding(start = 16.dp)
                                .fillMaxWidth())
                    }

                }

            }

            is SummarizeUiState.Error->{
                Card(
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = MaterialTheme.shapes.large
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                                               Text(
                            text = (uiState as SummarizeUiState.Error).error,
                            modifier = Modifier.padding(start = 16.dp)
                                .fillMaxWidth())
                    }

                }
            }
        }
    }
}

@Preview(showSystemUi = true , showBackground = true)
@Composable
fun Qs() {
    GeminiTheme {
        QuestionScreen()
    }
}