package com.example.navigation_talk.ui.decompose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.overlay.ChildOverlay
import com.arkivanov.decompose.router.overlay.OverlayNavigation
import com.arkivanov.decompose.router.overlay.activate
import com.arkivanov.decompose.router.overlay.childOverlay
import com.arkivanov.decompose.router.overlay.dismiss
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.example.navigation_talk.ui.decompose.utils.rememberComponentContext
import com.example.navigation_talk.ui.decompose.utils.rememberOverlayModalBottomSheetState
import kotlinx.parcelize.Parcelize

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ModalDecomposeNavigation() {
    val componentContext = rememberComponentContext()
    val root = remember(componentContext) {
        ModalRootComponent(componentContext)
    }
    val sheetState = rememberOverlayModalBottomSheetState(
        overlay = root.childOverlay,
        onDismiss = root::dismissOverlay
    ) { child ->
        when (val instance = child.instance) {
            is ModalRoot.Child.BottomSheetChild -> {
                SimpleBottomSheet(component = instance.component)
            }
            is ModalRoot.Child.DialogChild -> {
                SimpleDialog(component = instance.component)
            }
        }
    }

    ModalBottomSheetLayout(
        sheetContent = sheetState.sheetContent.value,
        sheetState = sheetState.sheetState
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = root::showBottomSheet) {
                Text("Show bottom sheet")
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = root::showDialog) {
                Text("Show dialog")
            }
        }
    }
}


@Composable
private fun SimpleBottomSheet(component: BottomSheetComponent) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(MaterialTheme.colors.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Simple bottom sheet")
        Spacer(Modifier.height(16.dp))
        Button(onClick = component::dismissOverlay) {
            Text("Close")
        }
    }
}

@Composable
private fun SimpleDialog(component: DialogComponent) {
    Dialog(onDismissRequest = component::dismissOverlay) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colors.background)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Simple dialog")
            Spacer(Modifier.height(16.dp))
            Button(onClick = component::dismissOverlay) {
                Text("Close")
            }
        }
    }
}

private interface ModalRoot {
    val childOverlay: Value<ChildOverlay<*, Child>>

    fun dismissOverlay()

    fun showBottomSheet()

    fun showDialog()

    sealed class Child {
        class BottomSheetChild(val component: BottomSheetComponent) : Child()
        class DialogChild(val component: DialogComponent) : Child()
    }
}

private class ModalRootComponent(
    componentContext: ComponentContext
) : ModalRoot, ComponentContext by componentContext {

    private val overlayNavigation = OverlayNavigation<ModalConfig>()

    override val childOverlay: Value<ChildOverlay<*, ModalRoot.Child>> = childOverlay(
        source = overlayNavigation,
        handleBackButton = true,
        childFactory = ::child
    )

    override fun showBottomSheet() {
        overlayNavigation.activate(ModalConfig.BottomSheet)
    }

    override fun showDialog() {
        overlayNavigation.activate(ModalConfig.Dialog)
    }

    override fun dismissOverlay() {
        overlayNavigation.dismiss()
    }

    private fun child(
        config: ModalConfig,
        componentContext: ComponentContext
    ): ModalRoot.Child {
        return when (config) {
            is ModalConfig.BottomSheet ->
                ModalRoot.Child.BottomSheetChild(
                    component = BottomSheetComponent(
                        componentContext,
                        onDismiss = overlayNavigation::dismiss
                    )
                )
            is ModalConfig.Dialog -> ModalRoot.Child.DialogChild(
                component = DialogComponent(
                    componentContext,
                    onDismiss = overlayNavigation::dismiss
                )
            )
        }
    }
}

private class BottomSheetComponent(
    componentContext: ComponentContext,
    private val onDismiss: () -> Unit
) : ComponentContext by componentContext {
    fun dismissOverlay() {
        onDismiss()
    }
}

private class DialogComponent(
    componentContext: ComponentContext,
    private val onDismiss: () -> Unit
) : ComponentContext by componentContext {
    fun dismissOverlay() {
        onDismiss()
    }
}


private sealed interface ModalConfig : Parcelable {
    @Parcelize
    object BottomSheet : ModalConfig

    @Parcelize
    object Dialog : ModalConfig
}