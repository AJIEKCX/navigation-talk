package com.example.navigation_talk.ui.jetpack

import android.net.Uri
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SimpleNavigation() {
    val navController = rememberAnimatedNavController()
    AnimatedNavHost(
        navController = navController,
        startDestination = Routes.Input.route,
    ) {
        inputScreen { message ->
            navController.navigateDetails(message)
        }
        detailsScreen()
    }
}

private sealed class Routes {
    object Input : Routes() {
        const val route = "input"
    }

    object Details : Routes() {
        private const val screen = "details"
        const val messageArg = "message"
        const val route = "$screen/{$messageArg}"

        fun destination(message: String): String {
            return "$screen/$message"
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.inputScreen(navigateDetails: (String) -> Unit) {
    composable(
        route = Routes.Input.route,
        enterTransition = {
            slideIntoContainer(AnimatedContentScope.SlideDirection.Right, animationSpec = tween(700))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(700))
        },
    ) {
        InputScreen(onClick = navigateDetails)
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.detailsScreen() {
    with(Routes.Details) {
        composable(
            route = route,
            enterTransition = {
                slideIntoContainer(AnimatedContentScope.SlideDirection.Right, animationSpec = tween(700))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(700))
            },
        ) {
            val message = it.arguments?.getString(messageArg)!!
            DetailsScreen(message = message)
        }
    }
}

private fun NavController.navigateDetails(message: String) {
    val encodedMessage = Uri.encode(message)
    return navigate(Routes.Details.destination(encodedMessage))
}

@Composable
private fun InputScreen(
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Message") },
            singleLine = true,
            modifier = Modifier
                .padding(horizontal = 64.dp)
                .fillMaxWidth()
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { onClick(text) },
            enabled = text.isNotBlank()
        ) {
            Text("Send to Details")
        }
    }
}

@Composable
private fun DetailsScreen(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message)
    }
}