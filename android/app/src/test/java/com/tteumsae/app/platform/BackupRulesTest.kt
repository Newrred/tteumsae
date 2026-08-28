package com.tteumsae.app.platform

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupRulesTest {
    @Test
    fun `활성 여행과 인증 세션이 포함된 shared preferences는 백업하지 않는다`() {
        val manifest = source("src/main/AndroidManifest.xml")
        val legacyRules = source("src/main/res/xml/backup_rules.xml")
        val extractionRules = source("src/main/res/xml/data_extraction_rules.xml")

        assertTrue(manifest.contains("android:fullBackupContent=\"@xml/backup_rules\""))
        assertTrue(manifest.contains("android:dataExtractionRules=\"@xml/data_extraction_rules\""))
        assertTrue(legacyRules.contains("<exclude domain=\"sharedpref\" path=\".\""))
        assertEquals(
            2,
            extractionRules.split("<exclude domain=\"sharedpref\" path=\".\"").size - 1,
        )
    }

    private fun source(relativePath: String): String {
        val candidates = listOf(File(relativePath), File("app/$relativePath"))
        return candidates.firstOrNull(File::isFile)?.readText()
            ?: error("소스 파일을 찾지 못했습니다: $relativePath")
    }
}
