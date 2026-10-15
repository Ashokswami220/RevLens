package com.example.revlens.ui.navigation

import kotlinx.browser.window

actual object BrowserRouter {
    actual fun updateUrl(route: String) {
        try {
            val target = "#/$route"
            if (window.location.hash != target) {
                window.location.hash = target
            }
        } catch (_: Throwable) {
        }
    }

    actual fun getInitialRoute(): String? {
        return try {
            val hash = window.location.hash.removePrefix("#")
                .removePrefix("/")
            hash.ifBlank { null }
        } catch (_: Throwable) {
            null
        }
    }

    actual fun onUrlChange(callback: (String) -> Unit) {
        try {
            window.addEventListener("hashchange") {
                val hash = window.location.hash.removePrefix("#")
                    .removePrefix("/")
                if (hash.isNotBlank()) {
                    callback(hash)
                }
            }
        } catch (_: Throwable) {
        }
    }
}
