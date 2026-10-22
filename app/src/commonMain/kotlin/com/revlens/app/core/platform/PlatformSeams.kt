package com.revlens.app.core.platform

data class PickedFile(
    val name: String,
    val sizeBytes: Long,
    val content: ByteArray
) {
    fun readText(): String = content.decodeToString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PickedFile) return false
        return name == other.name && sizeBytes == other.sizeBytes && content.contentEquals(
            other.content
        )
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + sizeBytes.hashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }
}

interface SecureStorage {
    suspend fun put(key: String, value: String)
    suspend fun get(key: String): String?
    suspend fun remove(key: String)
}

class InMemorySecureStorage : SecureStorage {
    private val map = mutableMapOf<String, String>()
    override suspend fun put(key: String, value: String) {
        map[key] = value
    }

    override suspend fun get(key: String): String? = map[key]
    override suspend fun remove(key: String) {
        map.remove(key)
    }
}
