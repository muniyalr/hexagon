package com.hexagonkt.settings

import com.hexagonkt.helpers.get
import com.hexagonkt.settings.SettingsManager.loadDefaultSettings
import com.hexagonkt.settings.SettingsManager.settings
import com.hexagonkt.settings.SettingsManager.setting
import com.hexagonkt.settings.SettingsManager.requireSetting
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

/**
 * Check `gradle.build` to see the related files creation.
 */
@Test class SettingsManagerTest {
    @BeforeMethod fun setUpEnvironment() {
        SettingsManager.setDefaultSettings("SERVICE_ENVIRONMENT=DEVELOPMENT")
    }

    fun `setting works as expected`() {
        assert(setting<String>("property") == "changed")
        assert(setting<Int>("intProperty") == 42)
        assert(setting<String>("foo") == "bar")
    }

    fun `get configuration properties`() {
        assert(settings["property"] as String == "changed")
        assert(settings["intProperty"] as Int == 42)
        assert(settings["foo"] as String == "bar")
        assert(settings["parent", "key"] as String == "val")
    }

    fun `require configuration properties`() {
        assert(requireSetting<String>("property") == "changed")
        assert(requireSetting<Int>("intProperty") == 42)
        assert(requireSetting<String>("foo") == "bar")
        assert(requireSetting<String>("parent", "key") == "val")
    }

    @Test(expectedExceptions = arrayOf(IllegalStateException::class))
    fun `require not found setting`() {
        requireSetting<String>("not_found")
    }

    fun `set default settings add command line arguments`() {
        SettingsManager.setDefaultSettings("key=val", "param=data")
        assert(settings.size == 12)
        assert(requireSetting<String>("key") == "val")
        assert(requireSetting<String>("param") == "data")
        assert(requireSetting<String>("property") == "value")
    }

    fun `settings remains the same if the environment is not changed`() {
        val size = SettingsManager.settings.size
        SettingsManager.settings += "SERVICE_ENVIRONMENT" to "DEVELOPMENT"
        assert(settings.size == size)
    }

    fun `Load default settings`() {
        // TODO !!!
        SettingsManager.settings = loadDefaultSettings("--arg1=val1", "--arg2=val2")
    }
}
