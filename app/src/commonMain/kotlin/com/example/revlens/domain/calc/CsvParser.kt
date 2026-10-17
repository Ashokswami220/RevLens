package com.example.revlens.domain.calc

data class ParsedCsv(
    val delimiter: Char,
    val headers: List<String>,
    val rows: List<List<String>>
)

object CsvParser {

    fun parse(csvContent: String): ParsedCsv {
        val sanitized = csvContent.removePrefix("\uFEFF")
        val delimiter = detectDelimiter(sanitized)

        val allRows = mutableListOf<List<String>>()
        val currentRow = mutableListOf<String>()
        val currentField = StringBuilder()
        var inQuotes = false
        var i = 0
        val len = sanitized.length

        while (i < len) {
            val c = sanitized[i]

            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < len && sanitized[i + 1] == '"') {
                        currentField.append('"')
                        i += 2
                        continue
                    } else {
                        inQuotes = false
                    }
                } else {
                    currentField.append(c)
                }
            } else {
                when (c) {
                    '"' -> {
                        inQuotes = true
                    }
                    delimiter -> {
                        currentRow.add(currentField.toString().trim())
                        currentField.clear()
                    }
                    '\r' -> {
                        if (i + 1 < len && sanitized[i + 1] == '\n') {
                            i++
                        }
                        currentRow.add(currentField.toString().trim())
                        currentField.clear()
                        if (currentRow.any { it.isNotBlank() }) {
                            allRows.add(currentRow.toList())
                        }
                        currentRow.clear()
                    }
                    '\n' -> {
                        currentRow.add(currentField.toString().trim())
                        currentField.clear()
                        if (currentRow.any { it.isNotBlank() }) {
                            allRows.add(currentRow.toList())
                        }
                        currentRow.clear()
                    }
                    else -> {
                        currentField.append(c)
                    }
                }
            }
            i++
        }

        if (currentField.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentField.toString().trim())
            if (currentRow.any { it.isNotBlank() }) {
                allRows.add(currentRow.toList())
            }
        }

        if (allRows.isEmpty()) {
            return ParsedCsv(delimiter, emptyList(), emptyList())
        }

        val headers = allRows.first()
        val dataRows = allRows.drop(1)
        return ParsedCsv(delimiter, headers, dataRows)
    }

    private fun detectDelimiter(content: String): Char {
        val firstLine = content.lines().firstOrNull { it.isNotBlank() } ?: return ','
        var commaCount = 0
        var semicolonCount = 0
        var tabCount = 0
        var inQuotes = false

        for (c in firstLine) {
            if (c == '"') inQuotes = !inQuotes
            if (!inQuotes) {
                when (c) {
                    ',' -> commaCount++
                    ';' -> semicolonCount++
                    '\t' -> tabCount++
                }
            }
        }

        return when {
            tabCount > commaCount && tabCount > semicolonCount -> '\t'
            semicolonCount > commaCount && semicolonCount > tabCount -> ';'
            else -> ','
        }
    }
}
