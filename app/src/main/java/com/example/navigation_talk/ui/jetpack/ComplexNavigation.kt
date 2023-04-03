package com.example.navigation_talk.ui.jetpack

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Composable
fun ComplexNavigation(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("initial_stack") }
            ) {
                Text("Initial stack")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("screen_replacing") }
            ) {
                Text("Screen replacing")
            }
        }
    }
}

fun NavGraphBuilder.complexNavGraph(navController: NavController) {
    navigation(
        startDestination = "start",
        route = "complex_jetpack"
    ) {
        composable("start") {
            ComplexNavigation(navController)
        }
        composable("initial_stack") {
            InitialStackGraph()
        }
        composable("screen_replacing") {
            ReplaceStackGraph()
        }
    }
}

@Composable
private fun InitialStackGraph(viewModel: InitialStackViewModel = viewModel()) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "screenA") {
        composable("screenA") {
            Screen(
                title = "A",
                buttonTitle = "Go to B",
                onClick = { navController.navigate("screenB/B") }
            )
        }
        composable("screenB/{title}") {
            val title = it.arguments?.getString("title")!!
            Screen(
                title = title,
                buttonTitle = "Go to C",
                onClick = { navController.navigate("screenC") }
            )
        }
        composable("screenC") {
            Screen(
                title = "C",
                buttonTitle = "Go to D",
                onClick = { navController.navigate("screenD") }
            )
        }
        composable("screenD") {
            Screen(title = "D", buttonTitle = "Back") {
                navController.popBackStack()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.routes.collect { routes ->
            navController.popBackStack(
                destinationId = navController.graph.findStartDestination().id,
                inclusive = true
            )
            routes.forEach {
                navController.navigate(it)
            }
        }
    }
}

class InitialStackViewModel : ViewModel() {
    private val _routes = Channel<List<String>>(
        capacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val routes: Flow<List<String>> = _routes.receiveAsFlow()

    init {
        viewModelScope.launch {
            _routes.send(listOf("screenA", "screenB/B", "screenC", "screenD"))
        }
    }
}

@Composable
private fun ReplaceStackGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "screenA") {
        composable("screenA") {
            Screen(
                title = "A",
                buttonTitle = "Go to B",
                onClick = { navController.navigate("screenB/initial B") }
            )
        }
        composable("screenB/{title}") {
            val title = it.arguments?.getString("title")!!
            Screen(
                title = title,
                buttonTitle = "Go to C",
                onClick = { navController.navigate("screenC") }
            )
        }
        composable("screenC") {
            Screen(
                title = "C",
                buttonTitle = "Go to D",
                onClick = { navController.navigate("screenD") }
            )
        }
        composable("screenD") {
            Screen(title = "D", buttonTitle = "Replace B") {
                navController.navigate("screenB/replaced B") {
                    popUpTo("screenA")
                }
                navController.navigate("screenC")
                navController.navigate("screenD")
            }
        }
    }
}

@Composable
private fun Screen(
    title: String,
    buttonTitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title)
        Button(onClick = onClick) {
            Text(buttonTitle)
        }
    }
}