package com.example.revlens.utils

private fun jsNow(): Double = js("Date.now()")

actual fun getCurrentTimeMillis(): Long = jsNow().toLong()
