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

interface FilePicker {
    suspend fun pickCsv(): PickedFile?
}

interface FileSharer {
    suspend fun shareFile(filename: String, mimeType: String, content: ByteArray)
}

interface SecureStorage {
    suspend fun put(key: String, value: String)
    suspend fun get(key: String): String?
    suspend fun remove(key: String)
}

interface LocalNotifier {
    fun notify(id: Int, title: String, message: String)
}

class DefaultFilePicker : FilePicker {
    override suspend fun pickCsv(): PickedFile? = null
}

class DefaultFileSharer : FileSharer {
    override suspend fun shareFile(filename: String, mimeType: String, content: ByteArray) {}
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

class DefaultLocalNotifier : LocalNotifier {
    override fun notify(id: Int, title: String, message: String) {}
}
