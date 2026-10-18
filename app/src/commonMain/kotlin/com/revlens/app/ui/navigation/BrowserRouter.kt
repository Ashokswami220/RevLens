package com.revlens.app.ui.navigation

expect object BrowserRouter {
    fun updateUrl(route: String)
    fun getInitialRoute(): String?
    fun onUrlChange(callback: (String) -> Unit)
}
