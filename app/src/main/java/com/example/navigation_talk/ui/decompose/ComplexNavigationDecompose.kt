package com.example.navigation_talk.ui.decompose

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.example.navigation_talk.ui.decompose.utils.rememberComponentContext
import kotlinx.parcelize.Parcelize

@Composable
fun ComplexNavigationDecompose(
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
                onClick = { navController.navigate("initial_stack_decompose") }
            ) {
                Text("Initial stack")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("screen_replacing_decompose") }
            ) {
                Text("Screen replacing")
            }
        }
    }
}

fun NavGraphBuilder.complexDecomposeNavGraph(navController: NavController) {
    navigation(
        startDestination = "start_decompose",
        route = "complex_decompose"
    ) {
        composable("start_decompose") {
            ComplexNavigationDecompose(navController)
        }
        composable("initial_stack_decompose") {
            InitialStackGraph()
        }
        composable("screen_replacing_decompose") {
            ReplaceStackGraph()
        }
    }
}

@Composable
fun InitialStackGraph() {
    val componentContext = rememberComponentContext()
    val root = remember(componentContext) {
        ComplexRootComponent(
            componentContext,
            initialStack = listOf(
                ComplexRootComponent.Config.ScreenA,
                ComplexRootComponent.Config.ScreenB("B"),
                ComplexRootComponent.Config.ScreenC,
                ComplexRootComponent.Config.ScreenD
            )
        )
    }
    val childStack by root.childStack.subscribeAsState()

    Children(childStack) {
        when (val child = it.instance) {
            is ComplexRoot.Child.ScreenChild -> {
                when (child.config) {
                    is ComplexRootComponent.Config.ScreenA -> Screen(
                        title = "A",
                        buttonTitle = "Go to B",
                        onClick = { root.navigateTo(ComplexRootComponent.Config.ScreenB("B")) }
                    )
                    is ComplexRootComponent.Config.ScreenB -> Screen(
                        title = "B",
                        buttonTitle = "Go to C",
                        onClick = { root.navigateTo(ComplexRootComponent.Config.ScreenC) }
                    )
                    ComplexRootComponent.Config.ScreenC -> Screen(
                        title = "C",
                        buttonTitle = "Go to D",
                        onClick = { root.navigateTo(ComplexRootComponent.Config.ScreenD) }
                    )
                    ComplexRootComponent.Config.ScreenD -> Screen(
                        title = "D",
                        buttonTitle = "Back",
                        onClick = { root.navigateBack() }
                    )
                }
            }
        }
    }
}

@Composable
fun ReplaceStackGraph() {
    val componentContext = rememberComponentContext()
    val root = remember(componentContext) {
        ComplexRootComponent(componentContext)
    }
    val childStack by root.childStack.subscribeAsState()

    Children(childStack) {
        when (val child = it.instance) {
            is ComplexRoot.Child.ScreenChild -> {
                when (child.config) {
                    is ComplexRootComponent.Config.ScreenA -> Screen(
                        title = "A",
                        buttonTitle = "Go to B",
                        onClick = { root.navigateTo(ComplexRootComponent.Config.ScreenB("initial B")) }
                    )
                    is ComplexRootComponent.Config.ScreenB -> Screen(
                        title = child.config.title,
                        buttonTitle = "Go to C",
                        onClick = { root.navigateTo(ComplexRootComponent.Config.ScreenC) }
                    )
                    ComplexRootComponent.Config.ScreenC -> Screen(
                        title = "C",
                        buttonTitle = "Go to D",
                        onClick = { root.navigateTo(ComplexRootComponent.Config.ScreenD) }
                    )
                    ComplexRootComponent.Config.ScreenD -> Screen(
                        title = "D",
                        buttonTitle = "Replace B",
                        onClick = { root.replaceScreenB(ComplexRootComponent.Config.ScreenB("replaced B")) }
                    )
                }
            }
        }
    }
}

@Composable
private fun Screen(
    title: String,
    buttonTitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title)
        Button(onClick = onClick) {
            Text(buttonTitle)
        }
    }
}

private interface ComplexRoot {
    val childStack: Value<ChildStack<*, Child>>

    fun navigateTo(config: ComplexRootComponent.Config)

    fun navigateBack()

    fun replaceScreenB(newConfig: ComplexRootComponent.Config.ScreenB)

    sealed class Child {
        class ScreenChild(val config: ComplexRootComponent.Config) : Child()
    }
}

private class ComplexRootComponent(
    componentContext: ComponentContext,
    private val initialStack: List<Config>? = null
) : ComplexRoot, ComponentContext by componentContext {
    private val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, ComplexRoot.Child>> = childStack(
        source = navigation,
        handleBackButton = true,
        initialStack = { initialStack ?: listOf(Config.ScreenA) },
        childFactory = { config, _ -> ComplexRoot.Child.ScreenChild(config) },
    )

    override fun navigateTo(config: Config) {
        navigation.push(config)
    }

    override fun navigateBack() {
        navigation.pop()
    }

    override fun replaceScreenB(newConfig: Config.ScreenB) {
        navigation.navigate { stack ->
            return@navigate stack.map { config ->
                if (config::class == newConfig::class) {
                    newConfig
                } else {
                    config
                }
            }
        }
    }

    sealed interface Config : Parcelable {
        @Parcelize
        object ScreenA : Config

        @Parcelize
        data class ScreenB(val title: String) : Config

        @Parcelize
        object ScreenC : Config

        @Parcelize
        object ScreenD : Config
    }
}