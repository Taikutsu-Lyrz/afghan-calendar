package app.afghancalendar

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp

fun main() = application {
    val state = rememberWindowState(width = 420.dp, height = 860.dp)
    Window(onCloseRequest = ::exitApplication, title = "Afghan Calendar", state = state) {
        App()
    }
}
