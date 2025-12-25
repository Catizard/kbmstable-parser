package com.github.catizard.bms.table

import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.*
import java.util.regex.Pattern

/**
 * Difficult table parser
 */
class DifficultyTableParser {
    companion object {
        val Json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    val ignoreTableContent: Boolean = false
    val ignoreDuplicateCharts: Boolean = true
    val sortElements: Boolean = false

    /**
     * Read and parse a difficult table from url
     * @param url should be a valid http URL
     */
    fun parse(url: String): DifficultyTable {
        if (!url.startsWith("http")) {
            throw IllegalArgumentException("Invalid URL: $url")
        }
        var headerURL: String = ""
        if (url.endsWith(".json")) {
            headerURL = url
        } else {
            val lines = readAllLines(url)
            val p = Pattern.compile("\"")
            for (line in lines) {
                // 難易度表ヘッダ
                if (line.lowercase(Locale.getDefault()).contains("<meta name=\"bmstable\"")) {
                    headerURL = p.split(line)[3]
                }
            }
        }
        return parseJsonTableFromURL(url.getRelativeResourceURL(headerURL))
    }

    /**
     * Read and parse a difficult table from file
     */
    fun parse(file: File): DifficultyTable {
        TODO()
    }

    fun parseJsonTableFromURL(url: String): DifficultyTable {
        val header = Json.decodeFromString<DifficultyTableHeader>(fetchJSON(url)).apply {
            headerURL = url
        }
        val data = Json.decodeFromString<List<DifficultyTableElement>>(fetchJSON(header.dataURL))
        return DifficultyTable(header, data)
    }

    private fun fetchJSON(url: String): String {
        val httpClient = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(15))
            .GET()
            .build()
        val resp = httpClient.send<String>(request, HttpResponse.BodyHandlers.ofString())
        val body = resp.body()
            .trim()
            .replace("\ufeff", "")
            .replace("\r", "")
            .replace("\n", "")
        if (resp.statusCode() != 200) {
            throw HttpException(resp.statusCode(), body)
        }
        if (!body.startsWith("{") && !body.startsWith("[")) {
            throw NotValidJsonException(body)
        }
        return body
    }

    private fun readAllLines(url: String): List<String> {
        BufferedReader(InputStreamReader(URL(url).openStream())).use { br ->
            val l: MutableList<String?> = mutableListOf()
            var line: String? = null
            while ((br.readLine().also { line = it }) != null) {
                l.add(line)
            }
            return Collections.unmodifiableList<String>(l)
        }
    }
}