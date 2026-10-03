package com.nullpointer.nourseCompose.medication

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.nullpointer.nourseCompose.R
import org.junit.Assert.*
import org.junit.Test
import org.xmlpull.v1.XmlPullParser

class PrivacyConfigurationTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val sensitiveFiles = setOf("measure.db", "measure.db-wal", "measure.db-shm")

    private fun excludedFiles(resource: Int): Map<String, Set<String>> {
        val exclusions = mutableMapOf<String, MutableSet<String>>()
        context.resources.getXml(resource).use { xml ->
            var section = "full-backup-content"
            while (xml.eventType != XmlPullParser.END_DOCUMENT) {
                if (xml.eventType == XmlPullParser.START_TAG) {
                    if (xml.name in setOf("cloud-backup", "device-transfer")) section = xml.name
                    if (xml.name == "exclude" && xml.getAttributeValue(null, "domain") == "database") {
                        exclusions.getOrPut(section) { mutableSetOf() }.add(xml.getAttributeValue(null, "path"))
                    }
                }
                xml.next()
            }
        }
        return exclusions
    }

    @Test fun legacyBackupExcludesHealthDatabase() {
        assertEquals(sensitiveFiles, excludedFiles(R.xml.backup_rules)["full-backup-content"])
    }
    @Test fun cloudAndTransferExcludeHealthDatabase() {
        val rules = excludedFiles(R.xml.data_extraction_rules)
        assertEquals(sensitiveFiles, rules["cloud-backup"])
        assertEquals(sensitiveFiles, rules["device-transfer"])
    }
    @Test fun privacyLinkUsesThePublishedHttpsPolicy() {
        val uri = Uri.parse(context.getString(R.string.privacy_policy_url))
        assertEquals("https", uri.scheme)
        assertEquals("ricardopajarocoatl.com", uri.host)
        assertEquals("/terms-and-conditions/C2TnDWRQv5eNTCcMkLUt", uri.path)
    }
}
