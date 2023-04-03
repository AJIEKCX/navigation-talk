package com.example.navigation_talk.ui.decompose.utils

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.arkivanov.decompose.router.overlay.ChildOverlay
import com.arkivanov.decompose.value.Value

private val emptyContent: @Composable ColumnScope.() -> Unit = {
    Spacer(Modifier.height(1.dp))
}

@ExperimentalMaterialApi
class OverlayModalBottomSheetState(
    val sheetContent: State<@Composable ColumnScope.() -> Unit>,
    val sheetState: ModalBottomSheetState,
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <C : Any, T : Any> rememberOverlayModalBottomSheetState(
    overlay: Value<ChildOverlay<C, T>>,
    onDismiss: () -> Unit,
    skipHalfExpanded: Boolean = false,
    sheetContent: @Composable (child: Child.Created<C, T>) -> Unit,
): OverlayModalBottomSheetState {
    val overlayState by overlay.subscribeAsState()
    val child: Child.Created<C, T>? = overlayState.overlay
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = skipHalfExpanded,
        confirmValueChange = { state ->
            if (state == ModalBottomSheetValue.Hidden) {
                onDismiss()
            }
            true
        }
    )
    val childContent = remember { mutableStateOf(emptyContent) }

    LaunchedEffect(child == null) {
        if (child == null) {
            sheetState.hide()
            childContent.value = emptyContent
        } else {
            sheetState.show()
        }
    }

    DisposableEffect(child) {
        if (child != null) {
            childContent.value = { sheetContent(child) }
        }
        onDispose {}
    }

    return remember {
        OverlayModalBottomSheetState(
            sheetContent = childContent,
            sheetState = sheetState
        )
    }
}
