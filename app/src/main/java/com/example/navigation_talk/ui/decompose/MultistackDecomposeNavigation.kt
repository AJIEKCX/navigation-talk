package com.example.navigation_talk.ui.decompose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.example.navigation_talk.ui.decompose.utils.rememberComponentContext
import kotlinx.parcelize.Parcelize

@Composable
fun MultistackDecomposeNavigation() {
    val componentContext = rememberComponentContext()
    val component = remember(componentContext) {
        TabNavigationComponent(componentContext)
    }
    val childStack by component.childStack.subscribeAsState()
    val activeInstance = childStack.active.instance
    val tabs = MainNavTab.tabs
    val activeTab: MainNavTab = remember(activeInstance) {
        when (activeInstance) {
            is TabNavigation.Child.HomeChild -> MainNavTab.Home
            is TabNavigation.Child.PaymentsChild -> MainNavTab.Payments
            is TabNavigation.Child.CatalogChild -> MainNavTab.Catalog
            is TabNavigation.Child.ProfileChild -> MainNavTab.Profile
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigation {
                tabs.forEach { tab ->
                    BottomNavigationItem(
                        selected = activeTab == tab,
                        onClick = { component.onTabClicked(tab) },
                        icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                        label = { Text(text = tab.title) },
                    )
                }
            }
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        Children(childStack) {
            when (it.instance) {
                is TabNavigation.Child.HomeChild -> Screen("Home", modifier)
                is TabNavigation.Child.PaymentsChild -> Screen("Payments", modifier)
                is TabNavigation.Child.CatalogChild -> Screen("Catalog", modifier)
                is TabNavigation.Child.ProfileChild -> Screen("Profile", modifier)
            }
        }
    }
}

@Composable
private fun Screen(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text)
    }
}

private interface TabNavigation {
    val childStack: Value<ChildStack<*, Child>>

    fun onTabClicked(tab: MainNavTab)

    sealed class Child {
        class HomeChild(val component: HomeComponent) : Child()
        class PaymentsChild(val component: PaymentsComponent) : Child()
        class CatalogChild(val component: CatalogComponent) : Child()
        class ProfileChild(val component: ProfileComponent) : Child()
    }
}

private class HomeComponent(componentContext: ComponentContext) : ComponentContext by componentContext
private class PaymentsComponent(componentContext: ComponentContext) : ComponentContext by componentContext
private class CatalogComponent(componentContext: ComponentContext) : ComponentContext by componentContext
private class ProfileComponent(componentContext: ComponentContext) : ComponentContext by componentContext

private class TabNavigationComponent(
    componentContext: ComponentContext
) : TabNavigation, ComponentContext by componentContext {
    private val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, TabNavigation.Child>> = childStack(
        source = navigation,
        initialStack = { listOf(Config.Home) },
        childFactory = ::child,
    )

    override fun onTabClicked(tab: MainNavTab) {
        when (tab) {
            MainNavTab.Home -> navigation.bringToFront(Config.Home)
            MainNavTab.Payments -> navigation.bringToFront(Config.Payments)
            MainNavTab.Catalog -> navigation.bringToFront(Config.Catalog)
            MainNavTab.Profile -> navigation.bringToFront(Config.Profile)
        }
    }

    private fun child(config: Config, componentContext: ComponentContext): TabNavigation.Child {
        return when (config) {
            is Config.Home -> {
                TabNavigation.Child.HomeChild(HomeComponent(componentContext))
            }
            Config.Catalog -> {
                TabNavigation.Child.CatalogChild(CatalogComponent(componentContext))
            }
            Config.Payments -> {
                TabNavigation.Child.PaymentsChild(PaymentsComponent(componentContext))
            }
            Config.Profile -> {
                TabNavigation.Child.ProfileChild(ProfileComponent(componentContext))
            }
        }
    }

    private sealed interface Config : Parcelable {
        @Parcelize
        object Home : Config

        @Parcelize
        object Payments : Config

        @Parcelize
        object Catalog : Config

        @Parcelize
        object Profile : Config
    }
}

private sealed class MainNavTab(
    val title: String,
    val icon: ImageVector
) {
    object Home : MainNavTab("home", Icons.Default.Home)
    object Payments : MainNavTab("payments", Icons.Default.List)
    object Catalog : MainNavTab("catalog", Icons.Default.ShoppingCart)
    object Profile : MainNavTab("profile", Icons.Default.AccountCircle)

    companion object {
        val tabs = listOf(Home, Payments, Catalog, Profile)
    }
}