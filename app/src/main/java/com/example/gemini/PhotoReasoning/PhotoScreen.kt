package com.example.gemini.PhotoReasoning

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Precision
import kotlinx.coroutines.launch

@Composable
fun PhotoReasoningRoute(
    viewModel: PhotoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val imageRequestBuilder = ImageRequest.Builder(LocalContext.current)
    val imageLoader = ImageLoader.Builder(LocalContext.current).build()
    val coroutineScope = rememberCoroutineScope()

    PhotoReasoningScreen(
        uiState = uiState,
        onSend = {inputText, selectedItems ->
            coroutineScope.launch {
                val bitmaps = selectedItems.mapNotNull {
                    val imageRequest = imageRequestBuilder
                        .data(it)
                        .size(768)
                        .precision(Precision.EXACT)
                        .build()
                    try {
                        val result = imageLoader.execute(imageRequest)
                        if (result is SuccessResult){
                            return@mapNotNull (result.drawable as BitmapDrawable).bitmap
                        }
                        else{
                            return@mapNotNull null
                        }
                    }catch (e:Exception){
                        return@mapNotNull null
                    }
                }
                viewModel.reason(inputText,bitmaps)
            }
        })
}
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PhotoReasoningScreen(
    uiState: PhotoUiState,
    onSend:(String,List<Uri>)->Unit
) {
    var question by remember {
        mutableStateOf("")
    }
    val imageUris = rememberSaveable(saver = UriSaver()) {
        mutableListOf()
    }

    // used to pick images from the device
    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { imageUri ->
        imageUri?.let {
            imageUris.add(it)
        }
    }
    val ctx = LocalContext.current
    val kb = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .padding(all = 16.dp)
            .verticalScroll(rememberScrollState())
            .background(Color.Black)
            .fillMaxSize()
    ){
        Card(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(55, 32, 84))
        ) {
            Row(modifier = Modifier.padding(top = 16.dp)) {
                //Add button
                IconButton(onClick = {
                    pickMedia.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                    modifier= Modifier
                        .padding(4.dp)
                        .align(Alignment.CenterVertically),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                    ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription ="add"
                    )
                }
                // text field
                OutlinedTextField(
                    value = question,
                    onValueChange = {question = it},
                    modifier = Modifier.fillMaxWidth(0.8f),
                    label = { Text(text = "prompt")},
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(235, 146, 240),
                        focusedLabelColor =Color(235, 146, 240)
                    )
                )

                Column(
                    modifier = Modifier.padding(4.dp)
                ){ //Generate Button
                    TextButton(onClick = {
                        if (question.isNotBlank()) {
                            onSend(question, imageUris)
                            kb?.hide()
                        } else {
                            Toast.makeText(ctx, "Invalid Input", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text(
                            text = "Go",
                            color = Color(214, 30, 205),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    //clear button
                    TextButton(onClick = {
                        if (question.isNotBlank()) {
                            question = ""
                            imageUris.clear()
                            kb?.hide()
                        } else {
                            Toast.makeText(ctx, "Already empty", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text(
                            text = "Clear",
                            color = Color(214, 51, 87),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

            }
            // lazy row to display the photos picked
            LazyRow(modifier = Modifier.padding(8.dp)){
                items(imageUris){uri->
                    AsyncImage(
                        model = uri,
                        contentDescription = "picked images",
                        modifier = Modifier
                            .padding(8.dp)
                            .requiredSize(72.dp)
                    )
                }
            }
        }

        // AI output

        when(uiState){
            PhotoUiState.Initial->{/*Nothing is shown*/}
            PhotoUiState.Loading->{
                Box(modifier =
                Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center){
                    CircularProgressIndicator()
                }
            }
            is PhotoUiState.Success->{
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = Color(230, 217, 39)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = "model",
                            modifier = Modifier.requiredSize(36.dp)
                        )

                        Text(
                            text = uiState.output,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }

            is PhotoUiState.Error->{
                Card(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ){
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(all = 16.dp)
                    )
                }
            }
        }

    }

}