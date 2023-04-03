package com.example.navigation_talk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.navigation_talk.ui.decompose.ModalDecomposeNavigation
import com.example.navigation_talk.ui.decompose.MultipleArgsDecomposeNavigation
import com.example.navigation_talk.ui.decompose.MultistackDecomposeNavigation
import com.example.navigation_talk.ui.decompose.NestedNavigationDecompose
import com.example.navigation_talk.ui.decompose.SimpleDecomposeNavigation
import com.example.navigation_talk.ui.decompose.complexDecomposeNavGraph
import com.example.navigation_talk.ui.jetpack.ModalNavigation
import com.example.navigation_talk.ui.jetpack.MultipleArgsNavigation
import com.example.navigation_talk.ui.jetpack.MultistackNavigation
import com.example.navigation_talk.ui.jetpack.NestedNavigation
import com.example.navigation_talk.ui.jetpack.SimpleNavigation
import com.example.navigation_talk.ui.jetpack.complexNavGraph
import com.example.navigation_talk.ui.theme.NavigationtalkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NavigationtalkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainNavGraph()
                }
            }
        }
    }
}

@Composable
fun MainNavGraph() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(navController = navController)
        }
        composable("simple_jetpack") {
            SimpleNavigation()
        }
        composable("simple_decompose") {
            SimpleDecomposeNavigation()
        }
        composable("multiple_args_jetpack") {
            MultipleArgsNavigation()
        }
        composable("multiple_args_decompose") {
            MultipleArgsDecomposeNavigation()
        }
        composable("nested_jetpack") {
            NestedNavigation()
        }
        composable("nested_decompose") {
            NestedNavigationDecompose()
        }
        composable("multistack_jetpack") {
            MultistackNavigation()
        }
        composable("multistack_decompose") {
            MultistackDecomposeNavigation()
        }
        composable("modal_jetpack") {
            ModalNavigation()
        }
        composable("modal_decompose") {
            ModalDecomposeNavigation()
        }
        complexNavGraph(navController)
        complexDecomposeNavGraph(navController)
    }
}

@Composable
fun MainScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var useDecompose by rememberSaveable {
        mutableStateOf(false)
    }
    val navPostfix = remember(useDecompose) {
        if (useDecompose) "decompose" else "jetpack"
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Use decompose")
                Switch(checked = useDecompose, onCheckedChange = { useDecompose = it })
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("simple_$navPostfix") }
            ) {
                Text("Simple with animation")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("multiple_args_$navPostfix") }
            ) {
                Text("Multiple args")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("nested_$navPostfix") }
            ) {
                Text("Nested")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("multistack_$navPostfix") }
            ) {
                Text("Multistack")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("modal_$navPostfix") }
            ) {
                Text("Modal")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("complex_$navPostfix") }
            ) {
                Text("Complex")
            }
        }
    }
}
