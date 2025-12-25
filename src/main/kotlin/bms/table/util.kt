package com.github.catizard.bms.table

/**
 * Calculate the relative resource url based on source url
 *
 * Example:
 * ```kotlin
 * val source = "http://example.com/table.json
 * val path = "score.json"
 * source.getRelativeResourceURL(path) // => "http://example.com/score.json"
 * ```
 */
fun String.getRelativeResourceURL(path: String): String {
    val trimmedPath = removeRelativePrefix(path)
    val parent = this.take(this.lastIndexOf('/') + 1)
    if (!path.startsWith("http") && !path.startsWith(parent)) {
        return parent + trimmedPath
    }
    return trimmedPath
}

private fun removeRelativePrefix(path: String): String {
    return if (path.startsWith("./")) {
        removeRelativePrefix(path.substring(2))
    } else {
        path
    }
}