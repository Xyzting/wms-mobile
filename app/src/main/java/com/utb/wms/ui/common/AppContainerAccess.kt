package com.utb.wms.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.Fragment
import com.utb.wms.WmsApplication
import com.utb.wms.di.AppContainer

@Composable
fun appContainer(): AppContainer {
    val context = LocalContext.current
    return remember(context) {
        (context.applicationContext as WmsApplication).container
    }
}

val Fragment.appContainer: AppContainer
    get() = (requireContext().applicationContext as WmsApplication).container
