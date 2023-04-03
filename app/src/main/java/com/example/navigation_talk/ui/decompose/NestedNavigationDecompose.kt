package com.example.navigation_talk.ui.decompose

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.parcelable.Parcelable
import com.example.navigation_talk.ui.decompose.utils.rememberComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.parcelize.Parcelize

@Composable
fun NestedNavigationDecompose() {
    val componentContext = rememberComponentContext()
    val root = remember(componentContext) { MarketRootComponent(componentContext) }
    val childStack by root.childStack.subscribeAsState()

    Children(childStack) {
        when (val child = it.instance) {
            is MarketRoot.Child.LoginChild ->
                LoginScreen(component = child.component)
            is MarketRoot.Child.MarketChild ->
                MarketNavGraph(component = child.component)
        }
    }
}

@Composable
private fun LoginScreen(component: Login) {
    var text by rememberSaveable { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize(),
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
            onClick = { component.onSignInClicked(text) },
            enabled = text.isNotBlank()
        ) {
            Text("Sign in")
        }
    }
}

@Composable
private fun MarketNavGraph(
    component: Market
) {
    val childStack by component.childStack.subscribeAsState()

    Children(childStack) {
        when (val child = it.instance) {
            is Market.Child.ProductsChild ->
                ShoppingListScreen(child.component)
            is Market.Child.CartChild ->
                CartScreen(child.component)
        }
    }
}


@Composable
private fun ShoppingListScreen(
    component: Products,
    modifier: Modifier = Modifier
) {
    val products by component.viewModel.products.collectAsState()
    val cart by component.viewModel.cart.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "Hello, ${component.username}!",
                style = MaterialTheme.typography.h5
            )
        }
        items(products) { product ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(product)
                IconButton(onClick = { component.viewModel.addProductToCart(product) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add to cart")
                }
            }
        }
        item {
            Button(
                enabled = cart.isNotEmpty(),
                onClick = component::onCartClicked
            ) {
                Text("Go to cart")
            }
        }
    }
}

@Composable
private fun CartScreen(
    component: Cart,
    modifier: Modifier = Modifier
) {
    val cart by component.viewModel.cart.collectAsState()

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

private interface MarketRoot {
    val childStack: Value<ChildStack<*, Child>>

    sealed class Child {
        class LoginChild(val component: Login) : Child()
        class MarketChild(val component: Market) : Child()
    }
}

private interface Login {
    fun onSignInClicked(username: String)
}

private interface Market {
    val viewModel: MarketViewModel
    val username: String
    val childStack: Value<ChildStack<*, Child>>

    sealed class Child {
        class ProductsChild(val component: Products) : Child()
        class CartChild(val component: Cart) : Child()
    }
}

private interface Products {
    val username: String
    val viewModel: MarketViewModel

    fun onCartClicked()
}

private interface Cart {
    val viewModel: MarketViewModel
}

private class MarketRootComponent(
    componentContext: ComponentContext
) : MarketRoot, ComponentContext by componentContext {
    private val navigation = StackNavigation<MarketConfig>()

    override val childStack: Value<ChildStack<*, MarketRoot.Child>> = childStack(
        source = navigation,
        handleBackButton = true,
        initialStack = { listOf(MarketConfig.Login) },
        childFactory = ::child,
    )

    private fun child(
        config: MarketConfig,
        componentContext: ComponentContext
    ): MarketRoot.Child {
        return when (config) {
            is MarketConfig.Login -> {
                MarketRoot.Child.LoginChild(
                    LoginComponent(
                        componentContext = componentContext,
                        onSignInClick = {
                            navigation.push(MarketConfig.Market(it))
                        }
                    )
                )
            }
            is MarketConfig.Market -> {
                MarketRoot.Child.MarketChild(
                    MarketComponent(config.username, componentContext)
                )
            }
        }
    }
}

private class LoginComponent(
    componentContext: ComponentContext,
    private val onSignInClick: (String) -> Unit
) : Login, ComponentContext by componentContext {
    override fun onSignInClicked(username: String) {
        onSignInClick(username)
    }
}

private class MarketComponent(
    override val username: String,
    componentContext: ComponentContext
) : Market, ComponentContext by componentContext {
    override val viewModel = instanceKeeper.getOrCreate {
        MarketViewModel()
    }

    private val navigation = StackNavigation<InternalConfig>()

    override val childStack: Value<ChildStack<*, Market.Child>> = childStack(
        source = navigation,
        handleBackButton = true,
        initialStack = { listOf(InternalConfig.Products) },
        childFactory = ::child,
    )

    private fun child(
        config: InternalConfig,
        componentContext: ComponentContext
    ): Market.Child {
        return when (config) {
            InternalConfig.Products -> {
                Market.Child.ProductsChild(
                    ProductsComponent(
                        componentContext,
                        username = username,
                        viewModel = viewModel,
                        onCartClick = { navigation.push(InternalConfig.Cart) }
                    )
                )
            }
            InternalConfig.Cart -> {
                Market.Child.CartChild(
                    CartComponent(componentContext, viewModel)
                )
            }
        }
    }

    private sealed interface InternalConfig : Parcelable {
        @Parcelize
        object Products : InternalConfig

        @Parcelize
        object Cart : InternalConfig
    }
}

private class ProductsComponent(
    componentContext: ComponentContext,
    override val username: String,
    override val viewModel: MarketViewModel,
    private val onCartClick: () -> Unit
) : Products, ComponentContext by componentContext {
    override fun onCartClicked() {
        onCartClick()
    }
}

private class CartComponent(
    componentContext: ComponentContext,
    override val viewModel: MarketViewModel
) : Cart, ComponentContext by componentContext

private sealed interface MarketConfig : Parcelable {
    @Parcelize
    object Login : MarketConfig

    @Parcelize
    data class Market(
        val username: String
    ) : MarketConfig
}

class MarketViewModel : InstanceKeeper.Instance {
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

    override fun onDestroy() {
        // no-op
    }
}