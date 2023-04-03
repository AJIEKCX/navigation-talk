package com.example.navigation_talk.ui.jetpack

import android.net.Uri
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun MultipleArgsNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = UserInfoRoute.Input.route
    ) {
        inputScreen { userInfo ->
            navController.navigateDetails(userInfo)
        }
        detailsScreen()
    }
}

private sealed class UserInfoRoute {
    object Input : UserInfoRoute() {
        const val route = "input"
    }

    object Details : UserInfoRoute() {
        private const val screen = "details"
        const val nameArg = "name"
        const val ageArg = "age"
        const val sexArg = "sex"
        const val additionalInfoArg = "addInfo"
        const val route = "$screen/{$nameArg},{$ageArg},{$sexArg}?$additionalInfoArg={$additionalInfoArg}"

        internal val arguments = listOf(
            navArgument(nameArg) {
                type = NavType.StringType
            },
            navArgument(ageArg) {
                type = NavType.IntType
            },
            navArgument(sexArg) {
                type = NavType.StringType
            },
            navArgument(additionalInfoArg) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
        )

        fun destination(info: UserInfo): String {
            val name = Uri.encode(info.name)
            return buildString {
                append("$screen/$name,${info.age},${info.sex}")
                if (info.additionalInfo != null && info.additionalInfo.isNotEmpty()) {
                    val addInfo = Uri.encode(info.additionalInfo)
                    append("?$additionalInfoArg=${addInfo}")
                }
            }
        }
    }
}

private fun NavGraphBuilder.inputScreen(navigateDetails: (UserInfo) -> Unit) {
    composable(UserInfoRoute.Input.route) {
        InputScreen(onShowUserInfo = navigateDetails)
    }
}

private fun NavGraphBuilder.detailsScreen() {
    with(UserInfoRoute.Details) {
        composable(
            route = route,
            arguments = arguments
        ) {
            DetailsScreen(
                userInfo = UserInfo(
                    name = it.arguments?.getString(nameArg)!!,
                    age = it.arguments?.getInt(ageArg)!!,
                    sex = Sex.valueOf(it.arguments?.getString(sexArg)!!),
                    additionalInfo = it.arguments?.getString(additionalInfoArg)
                )
            )
        }
    }
}

private fun NavController.navigateDetails(userInfo: UserInfo) {
    return navigate(UserInfoRoute.Details.destination(userInfo))
}

@Composable
private fun InputScreen(
    onShowUserInfo: (UserInfo) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InputViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier
            .padding(32.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        TextField(
            value = state.name,
            onValueChange = viewModel::onNameChanged,
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.age.toString(),
            onValueChange = viewModel::onAgeChanged,
            label = { Text("Age") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Row(Modifier.fillMaxWidth()) {
            RadioButtonWithLabel(
                label = "Male",
                selected = state.sex == Sex.Male,
                onClick = { viewModel.onSexChanged(Sex.Male) }
            )
            RadioButtonWithLabel(
                label = "Female",
                selected = state.sex == Sex.Female,
                onClick = { viewModel.onSexChanged(Sex.Female) }
            )
        }
        TextField(
            value = state.additionalInfo.orEmpty(),
            onValueChange = viewModel::onAdditionalInfoChanged,
            label = { Text("Additional info") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                onShowUserInfo(state)
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
    userInfo: UserInfo,
    modifier: Modifier = Modifier
) {
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

class InputViewModel : ViewModel() {
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
}

data class UserInfo(
    val name: String,
    val age: Int,
    val sex: Sex,
    val additionalInfo: String?
)

enum class Sex {
    Male,
    Female
}