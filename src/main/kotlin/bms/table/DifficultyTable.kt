package bms.table

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.collections.firstOrNull
import kotlin.collections.flatMap

/**
 * Difficult table
 */
@Serializable
data class DifficultyTable(
    val headerURL: String,
    val dataURL: String,
    val name: String,
    val symbol: String?,
    val originalURL: String?,
    val levelOrder: List<String>?,
    private val data: List<DifficultyTableElement>,
    val course: List<Course>,
    val lastUpdate: Long = 0
) {
    constructor(headerMeta: DifficultyTableHeader, data: List<DifficultyTableElement>) : this(
        headerURL = headerMeta.headerURL,
        dataURL = headerMeta.dataURL,
        name = headerMeta.name,
        symbol = headerMeta.symbol,
        originalURL = headerMeta.originalURL,
        levelOrder = headerMeta.levelOrder,
        data = data,
        course = headerMeta.courses,
    )

    // Follow the upstream behavior
    val elements: List<DifficultyTableElement> by lazy {
        data.filter { chart ->
            (chart.md5 != null && chart.md5.length > 24) || chart.sha256 != null && chart.sha256.length > 24
        }
    }

    val levelDescription: Array<String> by lazy {
        if (levelOrder != null && levelOrder.isNotEmpty()) {
            levelOrder.toTypedArray()
        } else {
            data.filter { it.level != "" }.map { it.level as String }.distinct().toTypedArray()
        }
    }
}

/**
 * Only presented for serialization
 */
@Serializable
data class DifficultyTableHeader(
    @SerialName("data_url") val _dataURL: String,
    @SerialName("name") val name: String,
    @SerialName("original_url") val originalURL: String? = null,
    @SerialName("symbol") val _symbol: String? = null,
    @SerialName("tag") val _tag: String? = null,
    @SerialName("level_order") val levelOrder: List<String>? = null,
    @SerialName("course") val _courses: JsonElement? = null,
    @SerialName("grade") val _grade: JsonElement? = null,
    @SerialName("last_update") val lastUpdate: String? = null,
    @Transient var headerURL: String = "",
) {
    val courses: List<Course> by lazy {
        when {
            _courses != null -> {
                when (_courses) {
                    is JsonArray -> {
                        if (_courses.firstOrNull() is JsonArray) {
                            _courses.flatMap { innerArray ->
                                Json.decodeFromJsonElement<List<Course>>(innerArray as JsonArray)
                            }
                        } else {
                            Json.decodeFromJsonElement<List<Course>>(_courses)
                        }
                    }
                    else -> emptyList()
                }
            }
            _grade != null -> {
                when (_grade) {
                    is JsonArray -> {
                        val courses = Json.decodeFromJsonElement<List<Course>>(_grade)
                        courses.map { course ->
                            course.constraint.clear()
                            course.constraint.addAll(listOf("grade_mirror", "gauge_lr2"))
                        }
                        courses
                    }
                    else -> emptyList()
                }
            }
            else -> emptyList()
        }
    }

    val dataURL: String
        get() = headerURL.getRelativeResourceURL(_dataURL)

    val symbol: String?
        get() = _symbol ?: _tag
}

@Serializable
data class DifficultyTableElement(
    @SerialName("artist") val artist: String? = null,
    @SerialName("comment") val _comment: String? = null,
    @SerialName("mode") val mode: String? = null,
    @SerialName("level") val level: String = "",
    @SerialName("lr2_bmsid") val lr2BMSID: String? = null,
    @SerialName("md5") val md5: String? = null,
    @SerialName("sha256") val sha256: String? = null,
    @SerialName("name_diff") val _nameDiff: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("url_diff") val urlDiff: String? = null,
    // TODO: Unused?
    @SerialName("ipfs") val ipfs: String? = null,
    @SerialName("ipfs_diff") val ipfsDiff: String? = null,
) {
    constructor(chart: Chart) : this(
        md5 = chart.md5,
        sha256 = chart.sha256,
        title = chart.title,
        artist = chart.artist,
    )

    val appendURL: String? = urlDiff
    val appendIPFS: String? = ipfsDiff
    val comment: String
        get() = _comment ?: ""
    val nameDiff: String
        get() = _nameDiff ?: ""
}

@Serializable
data class Course(
    @SerialName("name") val name: String,
    @SerialName("md5") val md5: List<String> = emptyList(),
    @SerialName("sha256") val sha256: List<String> = emptyList(),
    @SerialName("constraint") val constraint: MutableList<String> = mutableListOf(),
    @SerialName("trophy") val trophy: List<Trophy> = emptyList(),
    @SerialName("style") val style: String? = null,
    @SerialName("charts") val _charts: List<Chart> = emptyList(),
) {
    val charts: List<DifficultyTableElement> by lazy {
        if (!_charts.isEmpty()) {
            _charts.map { DifficultyTableElement(it) }.toList()
        } else {
            md5.map { DifficultyTableElement(md5 = it) }.toList()
        }
    }
}

@Serializable
data class Trophy(
    @SerialName("name") val name: String,
    @SerialName("missrate") val missRate: Double,
    @SerialName("scorerate") val scoreRate: Double,
    @SerialName("style") val style: String? = null
)

// Only used during parsing course
@Serializable
data class Chart(
    @SerialName("md5") val md5: String? = null,
    @SerialName("sha256") val sha256: String? = null,
    @SerialName("title") val title: String,
    // Unused field
    @SerialName("subtitle") val subTitle: String? = null,
    @SerialName("artist") val artist: String? = null,
)