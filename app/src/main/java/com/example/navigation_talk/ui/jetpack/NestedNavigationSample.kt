package com.example.navigation_talk.ui.jetpack

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun NestedNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen { username ->
                navController.navigate("market/$username")
            }
        }
        composable("market/{username}") {
            val username = it.arguments?.getString("username")!!
            MarketNavGraph(username)
        }
    }
}

@Composable
private fun LoginScreen(
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit
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
            label = { Text("Username") }
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { onClick(text) },
            enabled = text.isNotBlank()
        ) {
            Text("Sign in")
        }
    }
}

@Composable
private fun MarketNavGraph(
    username: String,
    viewModel: MarketViewModel = viewModel()
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "shopping_list"
    ) {
        composable("shopping_list") {
            ShoppingListScreen(
                username = username,
                viewModel = viewModel,
                onCartClick = { navController.navigate("cart") }
            )
        }
        composable("cart") {
            CartScreen(viewModel)
        }
    }
}


@Composable
private fun ShoppingListScreen(
    username: String,
    viewModel: MarketViewModel,
    onCartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    val cart by viewModel.cart.collectAsState()
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "Hello, $username!",
                style = MaterialTheme.typography.h5
            )
        }
        items(products) { product ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(product)
                IconButton(onClick = { viewModel.addProductToCart(product) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add to cart")
                }
            }
        }
        item {
            Button(
                enabled = cart.isNotEmpty(),
                onClick = onCartClick
            ) {
                Text("Go to cart")
            }
        }
    }
}

@Composable
private fun CartScreen(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val cart by viewModel.cart.collectAsState()

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Cart products:", style = MaterialTheme.typography.h5)
        cart.forEach { product ->
            Text(product)
        }
    }
}

class MarketViewModel : ViewModel() {
    val products = MutableStateFlow(
        listOf(
            "\uD83C\uDF4F Apple",
            "\uD83E\uDD5D Kiwi",
            "\uD83E\uDD6D Mango",
            "\uD83C\uDF49 Watermelon",
            "\uD83C\uDF4C Banana",
            "\uD83C\uDF4D Pineapple"
        )
    )
    val cart = MutableStateFlow(emptySet<String>())

    fun addProductToCart(product: String) {
        cart.value = cart.value + product
    }
}