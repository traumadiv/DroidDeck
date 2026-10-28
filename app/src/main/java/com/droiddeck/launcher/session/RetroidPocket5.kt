package com.droiddeck.launcher.session

import android.content.Context
import android.os.Build
import android.util.Log
import com.droiddeck.launcher.core.CpuCores

object RetroidPocket5 {
    private const val TAG = "RetroidPocket5"
    const val DEFAULTS_KEY = "rp5DefaultsV1"

    // Snapdragon 865: 8 GB LPDDR4X shared with the GPU, so DXVK would otherwise offer games most of it.
    const val VRAM_CAP_MB = 2048

    private val SD865_LITTLE = listOf(0, 1, 2, 3)
    private val SD865_BIG = listOf(4, 5, 6, 7)

    internal fun matches(model: String): Boolean = model.equals("Retroid Pocket 5", ignoreCase = true)

    fun isDevice(): Boolean = matches(Build.MODEL)

    internal fun split(maxGhz: Map<Int, Double>): Pair<List<Int>, List<Int>> {
        val lowest = maxGhz.values.minOrNull()
        val little = maxGhz.filterValues { it == lowest }.keys.sorted()
        val big = maxGhz.keys.sorted() - little.toSet()
        return if (little.isEmpty() || big.isEmpty()) SD865_LITTLE to SD865_BIG else little to big
    }

    private fun clusters(): Pair<List<Int>, List<Int>> =
        split(CpuCores.all.mapNotNull { core -> CpuCores.maxGhz(core)?.let { core to it } }.toMap())

    fun initialize(context: Context) {
        if (!isDevice()) return
        val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
        if (prefs.getBoolean(DEFAULTS_KEY, false)) return
        val (little, big) = clusters()
        prefs.edit()
            .putBoolean("clientCpusOverride", true)
            .putString("clientCpus", CpuCores.format(little.toSet()))
            .putString("gameCpus", CpuCores.format(big.toSet()))
            .putInt("vramCapMb", VRAM_CAP_MB)
            .putBoolean("gpuClockPin", true)
            .putBoolean(DEFAULTS_KEY, true)
            .apply()
        Log.i(TAG, "Retroid Pocket 5 defaults: client cores $little, game cores $big, VRAM cap $VRAM_CAP_MB MB, GPU clock pinned")
    }
}
