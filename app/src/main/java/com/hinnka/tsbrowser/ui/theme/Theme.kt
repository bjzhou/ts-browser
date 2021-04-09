package com.hinnka.tsbrowser.ui.theme

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.hinnka.tsbrowser.App

private val SecretColorPalette = darkColors(
    primary = Color.Black,
    primaryVariant = Color.Black,
    secondary = Teal200,
    onPrimary = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
)

private val LightColorPalette = lightColors(
    primary = Color.White,
    primaryVariant = Color.White,
    secondary = Teal200,
    onPrimary = Color.Black,

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

val Colors.lightWhite: Color
    get() = if (App.getProcessName().endsWith("secret")) {
        lightBlack
    } else {
        com.hinnka.tsbrowser.ui.theme.lightWhite
    }

@Composable
fun TSBrowserTheme(content: @Composable() () -> Unit) {
    val colors = if (App.getProcessName().endsWith("secret")) {
        SecretColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}