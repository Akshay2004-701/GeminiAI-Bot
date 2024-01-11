package com.example.gemini.menu

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gemini.PhotoReasoning.PhotoReasoningRoute
import com.example.gemini.chat.ChatScreen
import com.example.gemini.summarize.QuestionScreen

@Composable
fun NavGraph() {
 val navController = rememberNavController()
  NavHost(navController = navController, startDestination = "menu" ){
      // Menu Screen composable
      composable("menu"){
          MenuScreen(
              menuItems = listOf(
                  MenuItem("Chat","Chat","Chat"),
                  MenuItem("Summarize","Summarize","Summarize"),
                  MenuItem("PhotoReasoning","PhotoReasoning","PhotoReasoning")
              ),
              onButtonClick = {routeId->
                  navController.navigate(routeId)
              }
          )
      }

      //Chat Screen
      composable("Chat"){
          ChatScreen()
      }
      //Summarize Screen
      composable("Summarize"){
          QuestionScreen()
      }
      //Photo reasoning
      composable("PhotoReasoning"){
          PhotoReasoningRoute()
      }
  }
}