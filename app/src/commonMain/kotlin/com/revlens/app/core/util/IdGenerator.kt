package com.revlens.app.core.util

import kotlin.random.Random

interface IdGenerator {
    fun generate(prefix: String = ""): String

    companion object {
        fun newId(prefix: String = ""): String = DefaultIdGenerator().generate(prefix)
    }
}

class DefaultIdGenerator : IdGenerator {
    private val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

    override fun generate(prefix: String): String {
        val randomString = (1..16)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
        return if (prefix.isNotBlank()) "${prefix}_$randomString" else randomString
    }
}
