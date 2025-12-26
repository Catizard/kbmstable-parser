import bms.table.DifficultyTableElement
import bms.table.DifficultyTableParser
import bms.table.HttpException
import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.http.HttpConnectTimeoutException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParserTest {
    companion object {
        private val logger = KotlinLogging.logger {}

        /**
         * A simple wrapper that unifies the invoke between upstream and this project
         */
        fun io.github.catizard.jbmstable_parser.bms.table.DifficultyTableParser.parse(url: String): io.github.catizard.jbmstable_parser.bms.table.DifficultyTable {
            val dt = io.github.catizard.jbmstable_parser.bms.table.DifficultyTable()
            if (url.endsWith(".json")) {
                dt.headURL = url
            } else {
                dt.sourceURL = url
            }
            this.decode(true, dt)
            return dt
        }

        data class TableDefinition(
            val name: String,
            val url: String,
            val symbol: String,
            val hasCourses: Boolean
        )

        val RealTableDefinitions = listOf<TableDefinition>(
            TableDefinition("第三期Overjoy", "http://zris.work/bmstable/overjoy/header.json", "★★", false),
            TableDefinition("通常難易度表", "http://zris.work/bmstable/normal/normal_header.json", "☆", false),
            TableDefinition("発狂BMS難易度表", "http://zris.work/bmstable/insane/insane_header.json", "★", false),
            TableDefinition("NEW GENERATION 通常難易度表", "http://zris.work/bmstable/normal2/header.json", "▽", true),
            TableDefinition("NEW GENERATION 発狂難易度表", "http://zris.work/bmstable/insane2/insane_header.json", "▼", true),
            TableDefinition("Satellite", "http://zris.work/bmstable/satellite/header.json", "sl", true),
            TableDefinition("Stella", "https://stellabms.xyz/st/table.html", "st", true),
            TableDefinition("DP Satellite", "http://zris.work/bmstable/dp_satellite/header.json", "DPsl", true),
            TableDefinition("DP Stella", "http://zris.work/bmstable/dp_stella/header.json", "DPst", false),
            TableDefinition("δ難易度表", "http://zris.work/bmstable/dp_normal/dpn_header.json", "δ", true),
            TableDefinition("発狂DP難易度表", "http://zris.work/bmstable/dp_insane/dpi_header.json", "★", true),
            TableDefinition("DP Overjoy", "http://zris.work/bmstable/dp_overjoy/header.json", "★★", false),
            TableDefinition("DPBMS白難易度表(通常)", "http://zris.work/bmstable/dp_white/header.json", "白", false),
            TableDefinition("DPBMS黒難易度表(発狂)", "http://zris.work/bmstable/dp_black/header.json", "黒", false),
            TableDefinition("発狂DPBMSごった煮難易度表", "http://zris.work/bmstable/dp_zhu/header.json", "★", false),
            TableDefinition("発狂14keyBMS闇鍋難易度表", "http://zris.work/bmstable/dp_anguo/head14.json", "★", false),
            TableDefinition("DPBMSと諸感", "http://zris.work/bmstable/dp_zhugan/header.json", "☆", false),
            TableDefinition("Luminous", "http://zris.work/bmstable/luminous/header.json", "ln", false),
            TableDefinition("LN難易度", "http://zris.work/bmstable/ln/ln_header.json", "◆", true),
            TableDefinition("Scramble難易度表", "http://zris.work/bmstable/scramble/header.json", "SB", true),
            TableDefinition("PMSデータベース(Lv1~45)", "http://zris.work/bmstable/pms_normal/pmsdatabase_header.json", "PLv", false),
//            TableDefinition("発狂PMSデータベース(lv46～)", "https://pmsdifficulty.xxxxxxxx.jp/insane_PMSdifficulty.html", "P●", false),
            TableDefinition("発狂PMS難易度表", "http://zris.work/bmstable/pms_upper/header.json", "●", true),
            TableDefinition("PMS Database コースデータ案内所", "http://zris.work/bmstable/pms_course/course_header.json", "Pcourse", true),
            TableDefinition("Stellalite", "http://zris.work/bmstable/stellalite/Stellalite-header.json", "stl", false),
            TableDefinition("オマージュBMS難易度表", "http://zris.work/bmstable/homage/header.json", "∽", false),
            TableDefinition("16分乱打難易度表(仮)", "https://lets-go-time-hell.github.io/code-stream-table/", " ", false),
            TableDefinition("ウーデオシ小学校難易度表", "https://lets-go-time-hell.github.io/Arm-Shougakkou-table/", "Ude", false),
        )
    }

    @Test
    fun smokeTest() {
        for (tableDefinition in RealTableDefinitions) {
            logger.info { "Processing smoke test of ${tableDefinition.name}" }
            val parser = DifficultyTableParser()
            val table = try {
                parser.parse(tableDefinition.url)
            } catch (e: HttpConnectTimeoutException) {
                logger.info { "Skipping test because connection timed out" }
                continue
            } catch (e: HttpException) {
                logger.info { "Skipping test because server returns unexpected status code" }
                continue
            } catch (e: Exception) {
                throw e
            }
            assertEquals(table.name, tableDefinition.name, "name not equals")
            assertEquals(table.symbol, tableDefinition.symbol, "symbol not equals")
            if (tableDefinition.hasCourses) {
                assertTrue(table.course.isNotEmpty(), "no courses found")
            }
        }
    }

    @Test
    fun clapTest() {
        val upstreamParser = io.github.catizard.jbmstable_parser.bms.table.DifficultyTableParser()
        val parser = DifficultyTableParser()
        for (tableDefinition in RealTableDefinitions) {
            logger.info { "Processing clap test of ${tableDefinition.name}" }
            val upstreamResult = upstreamParser.parse(tableDefinition.url)
            val selfResult = try {
                parser.parse(tableDefinition.url)
            } catch (e: HttpConnectTimeoutException) {
                continue
            } catch (e: HttpException) {
                continue
            } catch (e: Exception) {
                throw e
            }
            assertEquals(upstreamResult.name, selfResult.name, "name not equals")
            assertEquals(upstreamResult.levelDescription.joinToString("#"), selfResult.levelDescription.joinToString("#"), "levelDescription not equals")
            val upstreamCourses = upstreamResult.course.flatMap { innerArray -> innerArray.map { it } }.toList()
            assertEquals(upstreamCourses.size, selfResult.course.size, "courses size not equals")
            selfResult.course.forEachIndexed { i, selfCourse ->
                val upstreamCourse = upstreamCourses[i]
                assertEquals(upstreamCourse.name, selfCourse.name, "course name not equals")
                assertEquals(upstreamCourse.charts.size, selfCourse.charts.size, "course charts size not equals")
                selfCourse.charts.forEachIndexed { j, selfCourseChart ->
                    val upstreamCourseChart = upstreamCourse.charts[j] as io.github.catizard.jbmstable_parser.bms.table.DifficultyTableElement
                    clapDifficultyTableElement(selfCourseChart, upstreamCourseChart)
                }
            }
            selfResult.elements.forEachIndexed { i, selfChart ->
                val upstreamChart = upstreamResult.elements[i]
                clapDifficultyTableElement(selfChart, upstreamChart)
            }
        }
    }

    private fun clapDifficultyTableElement(self: DifficultyTableElement, upstream: io.github.catizard.jbmstable_parser.bms.table.DifficultyTableElement) {
        assertEquals(upstream.level, self.level, "chart level not equals")
        assertEquals(upstream.appendURL, self.appendURL, "chart append url not equals")
        assertEquals(upstream.appendIPFS, self.appendIPFS, "chart append ipfs not equals")
        assertEquals(upstream.comment, self.comment, "comment not equals")
        assertEquals(upstream.title, self.title, "chart title not equals")
        assertEquals(upstream.url, self.url, "chart url not equals")
        assertEquals(upstream.ipfs, self.ipfs, "chart ipfs not equals")
        assertEquals(upstream.artist, self.artist, "chart title not equals")
        assertEquals(upstream.mD5, self.md5, "chart md5 not equals")
        assertEquals(upstream.shA256, self.sha256, "chart sha256 not equals")
        assertEquals(upstream.mode, self.mode, "chart mode not equals")
        // unused fields
        // assertEquals(upstream.evaluation, self.evaluation, "chart evaluation not equals")
        // assertEquals(upstream.packageURL, self.packageURL, "chart package url not equals")
        // assertEquals(upstream.packageName, self.packageName, "chart package name not equals")
        // assertEquals(upstream.parentHash, selfCourse.parentHash, "chart parent hash not equals")
        // assertEquals(upstream.state, self.state, "chart state not equals")
        // assertEquals(upstream.proposer, self.proposer, "chart proposer not equals")
        // assertEquals(upstream.appendArtist, self.appendArtist, "chart append artist not equals")
        // assertEquals(upstream.information, self.information, "chart information not equals")
        // assertEquals(upstream.bmsid, self.lr2BMSID, "chart lr2 BMSID not equals")
    }
}