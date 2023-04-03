package com.example.navigation_talk.ui.decompose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
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
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.isFront
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.example.navigation_talk.ui.decompose.utils.rememberComponentContext
import kotlinx.parcelize.Parcelize

@Composable
fun SimpleDecomposeNavigation() {
    val componentContext = rememberComponentContext()
    val root = remember(componentContext) {
        RootComponent(componentContext)
    }
    val childStack by root.childStack.subscribeAsState()

    Children(
        stack = childStack,
        animation = stackAnimation { _, _, direction ->
            if (direction.isFront) {
                slide() + fade()
            } else {
                scale(frontFactor = 1F, backFactor = 0.7F) + fade()
            }
        }
    ) {
        when (val child = it.instance) {
            is Root.Child.InputChild ->
                InputScreen(component = child.component)
            is Root.Child.DetailsChild ->
                DetailsScreen(component = child.component)
        }
    }
}

@Composable
private fun InputScreen(component: Input) {
    var text by rememberSaveable { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Message") }
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { component.onSendMessageClick(text) },
            enabled = text.isNotBlank()
        ) {
            Text("Send to Details")
        }
    }
}

@Composable
private fun DetailsScreen(component: Details) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = component.message)
    }
}

private interface Root {
    val childStack: Value<ChildStack<*, Child>>

    sealed class Child {
        class InputChild(val component: Input) : Child()
        class DetailsChild(val component: Details) : Child()
    }
}

private interface Input {
    fun onSendMessageClick(message: String)
}

private interface Details {
    val message: String
}

private class RootComponent(
    componentContext: ComponentContext
) : Root, ComponentContext by componentContext {
    private val navigation = StackNavigation<ScreenConfig>()

    override val childStack: Value<ChildStack<*, Root.Child>> = childStack(
        source = navigation,
        handleBackButton = true,
        initialStack = { listOf(ScreenConfig.Input) },
        childFactory = ::child,
    )

    private fun child(
        config: ScreenConfig,
        componentContext: ComponentContext
    ): Root.Child {
        return when (config) {
            is ScreenConfig.Input -> {
                Root.Child.InputChild(
                    InputComponent(
                        componentContext = componentContext,
                        onSendMessage = { message ->
                            navigation.push(ScreenConfig.Details(message))
                        },
                    )
                )
            }
            is ScreenConfig.Details -> {
                Root.Child.DetailsChild(
                    DetailsComponent(config.message, componentContext)
                )
            }
        }
    }
}

private class InputComponent(
    componentContext: ComponentContext,
    private val onSendMessage: (String) -> Unit
) : Input, ComponentContext by componentContext {
    override fun onSendMessageClick(message: String) {
        onSendMessage(message)
    }
}

private class DetailsComponent(
    override val message: String,
    componentContext: ComponentContext
) : Details, ComponentContext by componentContext

private sealed interface ScreenConfig : Parcelable {
    @Parcelize
    object Input : ScreenConfig

    @Parcelize
    data class Details(
        val message: String
    ) : ScreenConfig
}