package com.example.navigation_talk.ui.decompose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.parcelize.Parcelize

@Composable
fun MultipleArgsDecomposeNavigation() {
    val componentContext = rememberComponentContext()
    val root = remember(componentContext) { UserRootComponent(componentContext) }
    val childStack by root.childStack.subscribeAsState()

    Children(childStack) {
        when (val child = it.instance) {
            is UserRoot.Child.InputChild ->
                InputScreen(component = child.component)
            is UserRoot.Child.DetailsChild ->
                DetailsScreen(component = child.component)
        }
    }
}

@Composable
private fun InputScreen(
    component: UserInput,
    modifier: Modifier = Modifier
) {
    val state by component.viewModel.state.collectAsState()

    Column(
        modifier = modifier
            .padding(32.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        TextField(
            value = state.name,
            onValueChange = component.viewModel::onNameChanged,
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.age.toString(),
            onValueChange = component.viewModel::onAgeChanged,
            label = { Text("Age") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Row(Modifier.fillMaxWidth()) {
            RadioButtonWithLabel(
                label = "Male",
                selected = state.sex == Sex.Male,
                onClick = { component.viewModel.onSexChanged(Sex.Male) }
            )
            RadioButtonWithLabel(
                label = "Female",
                selected = state.sex == Sex.Female,
                onClick = { component.viewModel.onSexChanged(Sex.Female) }
            )
        }
        TextField(
            value = state.additionalInfo.orEmpty(),
            onValueChange = component.viewModel::onAdditionalInfoChanged,
            label = { Text("Additional info") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                component.onShowUserInfoClick(state)
            },
            enabled = state.name.isNotBlank()
        ) {
            Text("Show Details")
        }
    }
}

@Composable
private fun RadioButtonWithLabel(
    label: String,
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(label)
    }
}

@Composable
private fun DetailsScreen(
    component: UserDetails,
    modifier: Modifier = Modifier
) {
    val userInfo = component.userInfo
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Name: ${userInfo.name}")
        Text(text = "Age: ${userInfo.age}")
        Text(text = "Sex: ${userInfo.sex}")
        Text(text = "Additional info: ${userInfo.additionalInfo ?: "-"}")
    }
}


private interface UserRoot {
    val childStack: Value<ChildStack<*, Child>>

    sealed class Child {
        class InputChild(val component: UserInput) : Child()
        class DetailsChild(val component: UserDetails) : Child()
    }
}

private interface UserInput {
    val viewModel: InputViewModel

    fun onShowUserInfoClick(userInfo: UserInfo)
}

private interface UserDetails {
    val userInfo: UserInfo
}

private class UserRootComponent(
    componentContext: ComponentContext
) : UserRoot, ComponentContext by componentContext {
    private val navigation = StackNavigation<UserConfig>()

    override val childStack: Value<ChildStack<*, UserRoot.Child>> = childStack(
        source = navigation,
        handleBackButton = true,
        initialStack = { listOf(UserConfig.UserInput) },
        childFactory = ::child,
    )

    private fun child(
        config: UserConfig,
        componentContext: ComponentContext
    ): UserRoot.Child {
        return when (config) {
            is UserConfig.UserInput -> {
                UserRoot.Child.InputChild(
                    UserInputComponent(
                        componentContext = componentContext,
                        onShowUserInfo = {
                            navigation.push(UserConfig.UserDetails(it))
                        },
                    )
                )
            }
            is UserConfig.UserDetails -> {
                UserRoot.Child.DetailsChild(
                    UserDetailsComponent(config.userInfo, componentContext)
                )
            }
        }
    }
}

private class UserInputComponent(
    componentContext: ComponentContext,
    private val onShowUserInfo: (UserInfo) -> Unit
) : UserInput, ComponentContext by componentContext {
    override val viewModel = instanceKeeper.getOrCreate {
        InputViewModel()
    }

    override fun onShowUserInfoClick(userInfo: UserInfo) {
        onShowUserInfo(userInfo)
    }
}

private class InputViewModel : InstanceKeeper.Instance {
    private val _state = MutableStateFlow(
        UserInfo(
            name = "",
            age = 18,
            sex = Sex.Male,
            additionalInfo = null
        )
    )
    val state: StateFlow<UserInfo> = _state.asStateFlow()

    fun onNameChanged(value: String) {
        _state.value = _state.value.copy(name = value)
    }

    fun onAgeChanged(value: String) {
        val age = value.toIntOrNull() ?: return
        _state.value = _state.value.copy(age = age)
    }

    fun onSexChanged(value: Sex) {
        _state.value = _state.value.copy(sex = value)
    }

    fun onAdditionalInfoChanged(value: String) {
        _state.value = _state.value.copy(additionalInfo = value)
    }

    override fun onDestroy() {
        // no-op
    }
}

private class UserDetailsComponent(
    override val userInfo: UserInfo,
    componentContext: ComponentContext
) : UserDetails, ComponentContext by componentContext

private sealed interface UserConfig : Parcelable {
    @Parcelize
    object UserInput : UserConfig

    @Parcelize
    data class UserDetails(
        val userInfo: UserInfo
    ) : UserConfig
}

@Parcelize
private data class UserInfo(
    val name: String,
    val age: Int,
    val sex: Sex,
    val additionalInfo: String?
) : Parcelable

private enum class Sex {
    Male,
    Female
}