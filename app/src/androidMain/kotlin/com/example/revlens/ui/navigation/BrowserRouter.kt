package com.example.revlens.ui.navigation

actual object BrowserRouter {
    actual fun updateUrl(route: String) {}
    actual fun getInitialRoute(): String? = null
    actual fun onUrlChange(callback: (String) -> Unit) {}
}
