package com.revlens.app.utils

private fun jsNow(): Double = js("Date.now()")

actual fun getCurrentTimeMillis(): Long = jsNow().toLong()
