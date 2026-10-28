package com.droiddeck.launcher.session

import android.content.Context
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBuild

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], manifest = Config.NONE)
class RetroidPocket5Test {
    private val context: Context get() = RuntimeEnvironment.getApplication()

    @Before fun reset() {
        context.getSharedPreferences("session", Context.MODE_PRIVATE).edit().clear().commit()
        ShadowBuild.setModel("Phone")
    }

    @Test fun otherDevicesKeepTheirSettings() {
        for (model in listOf("Phone", "Retroid Pocket 4 Pro", "Retroid Pocket Mini", "Retroid Pocket Nova")) {
            ShadowBuild.setModel(model)
            RetroidPocket5.initialize(context)
            assertFalse(SessionPrefs.clientCpusOverride(context))
            assertEquals("", SessionPrefs.gameCpus(context))
            assertEquals(0, SessionPrefs.vramCapMb(context))
            assertFalse(SessionPrefs.gpuClockPin(context))
        }
    }

    @Test fun rp5DefaultsAreAppliedOnlyOnce() {
        ShadowBuild.setModel("Retroid Pocket 5")
        RetroidPocket5.initialize(context)
        assertTrue(SessionPrefs.clientCpusOverride(context))
        assertEquals("0,1,2,3", SessionPrefs.clientCpus(context))
        assertEquals("4,5,6,7", SessionPrefs.gameCpus(context))
        assertEquals(RetroidPocket5.VRAM_CAP_MB, SessionPrefs.vramCapMb(context))
        assertTrue(SessionPrefs.gpuClockPin(context))
        SessionPrefs.setGameCpus(context, "")
        SessionPrefs.setGpuClockPin(context, false)
        RetroidPocket5.initialize(context)
        assertEquals("", SessionPrefs.gameCpus(context))
        assertFalse(SessionPrefs.gpuClockPin(context))
    }

    @Test fun settingsResetBringsTheDefaultsBack() {
        assertTrue(SettingsReset.isSessionSetting(RetroidPocket5.DEFAULTS_KEY))
    }

    @Test fun clustersComeFromCpufreq() {
        val sd865 = mapOf(0 to 1.8, 1 to 1.8, 2 to 1.8, 3 to 1.8, 4 to 2.42, 5 to 2.42, 6 to 2.42, 7 to 2.84)
        assertEquals(listOf(0, 1, 2, 3) to listOf(4, 5, 6, 7), RetroidPocket5.split(sd865))
        assertEquals(listOf(0, 1, 2, 3) to listOf(4, 5, 6, 7), RetroidPocket5.split(emptyMap()))
        assertEquals(listOf(0, 1, 2, 3) to listOf(4, 5, 6, 7), RetroidPocket5.split((0..7).associateWith { 2.0 }))
    }

    @Test fun modelMatchIsExact() {
        assertTrue(RetroidPocket5.matches("Retroid Pocket 5"))
        assertTrue(RetroidPocket5.matches("retroid pocket 5"))
        assertFalse(RetroidPocket5.matches("Retroid Pocket 5 Pro"))
    }
}
