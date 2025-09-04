package com.rivvr.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val Colors = lightColorScheme()

@Composable
fun RivvrTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}