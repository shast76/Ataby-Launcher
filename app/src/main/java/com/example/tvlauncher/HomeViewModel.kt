package com.example.tvlauncher

import android.app.Application
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AppItem(val pkg: String, val label: String, val icon: ImageBitmap, val intent: Intent)

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = app.getSharedPreferences("launcher", 0)

    var apps by mutableStateOf<List<AppItem>>(emptyList())
        private set
    var favorites by mutableStateOf(prefs.getStringSet("fav", emptySet())!!.toSet())
        private set
    var glass by mutableStateOf(prefs.getBoolean("glass", true))
        private set
    var clock24 by mutableStateOf(prefs.getBoolean("clock24", true))
        private set

    fun refresh() {
        viewModelScope.launch { apps = withContext(Dispatchers.Default) { load() } }
    }

    fun toggleFav(pkg: String) {
        favorites = if (pkg in favorites) favorites - pkg else favorites + pkg
        prefs.edit().putStringSet("fav", favorites).apply()
    }

    fun clearFavs() {
        favorites = emptySet()
        prefs.edit().putStringSet("fav", emptySet()).apply()
    }

    fun toggleGlass() {
        glass = !glass
        prefs.edit().putBoolean("glass", glass).apply()
    }

    fun toggleClock() {
        clock24 = !clock24
        prefs.edit().putBoolean("clock24", clock24).apply()
    }

    private fun load(): List<AppItem> {
        val ctx = getApplication<Application>()
        val pm = ctx.packageManager
        fun query(cat: String) =
            pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(cat), 0)

        return (query(Intent.CATEGORY_LEANBACK_LAUNCHER) + query(Intent.CATEGORY_LAUNCHER))
            .asSequence()
            .filter { it.activityInfo.packageName != ctx.packageName }
            .distinctBy { it.activityInfo.packageName }
            .mapNotNull { ri ->
                val pkg = ri.activityInfo.packageName
                val intent = pm.getLeanbackLaunchIntentForPackage(pkg)
                    ?: pm.getLaunchIntentForPackage(pkg)
                    ?: return@mapNotNull null
                val banner = ri.activityInfo.loadBanner(pm)
                val bmp = if (banner != null) banner.toBitmap(320, 180)
                else ri.loadIcon(pm).toBitmap(256, 256)
                AppItem(
                    pkg,
                    ri.loadLabel(pm).toString(),
                    bmp.asImageBitmap(),
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }
}
