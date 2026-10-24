package com.example.tvlauncher

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LauncherApp(vm: HomeViewModel) {
    var settingsOpen by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize()) {
        Wallpaper()
        if (settingsOpen) {
            BackHandler { settingsOpen = false }
            SettingsScreen(vm)
        } else {
            HomeScreen(vm) { settingsOpen = true }
        }
    }
}

@Composable
fun HomeScreen(vm: HomeViewModel, onSettings: () -> Unit) {
    val ctx = LocalContext.current
    val favApps = vm.apps.filter { it.pkg in vm.favorites }
    fun launch(a: AppItem) { try { ctx.startActivity(a.intent) } catch (_: Exception) {} }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val cols = ((maxWidth - 112.dp) / 170.dp).toInt().coerceAtLeast(2)
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 56.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Header(vm, onSettings) }
            if (favApps.isNotEmpty()) {
                item { SectionTitle("Favoriler") }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(favApps, key = { it.pkg }) { a ->
                            AppCard(a, vm.glass, true, { launch(a) }, { vm.toggleFav(a.pkg) })
                        }
                    }
                }
            }
            item { SectionTitle("Tüm uygulamalar") }
            items(vm.apps.chunked(cols)) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    row.forEach { a ->
                        AppCard(a, vm.glass, a.pkg in vm.favorites, { launch(a) }, { vm.toggleFav(a.pkg) })
                    }
                }
            }
        }
    }
}

@Composable
fun Header(vm: HomeViewModel, onSettings: () -> Unit) {
    val pattern = if (vm.clock24) "HH:mm" else "h:mm a"
    val now by produceState(Date(), pattern) {
        while (true) { value = Date(); delay(15_000) }
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(
                SimpleDateFormat(pattern, Locale.getDefault()).format(now),
                color = Color.White, fontSize = 56.sp, fontWeight = FontWeight.Light
            )
            Text(
                SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(now),
                color = Color.White.copy(0.7f), fontSize = 20.sp
            )
        }
        Spacer(Modifier.weight(1f))
        GlassButton("Ayarlar", vm.glass, onSettings)
    }
}

@Composable
fun SectionTitle(text: String) =
    Text(text, color = Color.White.copy(0.85f), fontSize = 22.sp, fontWeight = FontWeight.SemiBold)

@Composable
fun GlassButton(text: String, glass: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.08f else 1f, label = "btn")
    Box(
        Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .glass(glass, focused, RoundedCornerShape(50))
            .onFocusChanged { focused = it.isFocused }
            .tvClick(onClick)
            .padding(horizontal = 28.dp, vertical = 14.dp)
    ) { Text(text, color = Color.White, fontSize = 20.sp) }
}

@Composable
fun AppCard(item: AppItem, glass: Boolean, fav: Boolean, onClick: () -> Unit, onLong: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.12f else 1f, label = "card")
    Column(Modifier.width(150.dp).zIndex(if (focused) 1f else 0f)) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .glass(glass, focused)
                .onFocusChanged { focused = it.isFocused }
                .tvClick(onClick, onLong)
                .padding(8.dp)
        ) {
            Image(item.icon, item.label, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            if (fav) Text("★", color = Color(0xFFFFD60A), fontSize = 14.sp, modifier = Modifier.align(Alignment.TopEnd))
        }
        Text(
            item.label,
            color = Color.White.copy(if (focused) 1f else 0.65f),
            fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 10.dp, start = 4.dp)
        )
    }
}

@Composable
fun SettingsScreen(vm: HomeViewModel) {
    val ctx = LocalContext.current
    fun open(action: String) {
        try { ctx.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) {}
    }
    Column(
        Modifier.fillMaxSize().padding(horizontal = 120.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Ayarlar", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Light)
        Spacer(Modifier.height(8.dp))
        SettingRow("Cam efekti", "Kartlarda yarı saydam cam görünümü", vm.glass, vm.glass) { vm.toggleGlass() }
        SettingRow("24 saat biçimi", "Saati 24 saatlik göster", vm.glass, vm.clock24) { vm.toggleClock() }
        SettingRow("Ağ ayarları", "Wi-Fi ve ağ", vm.glass, null) { open(Settings.ACTION_WIFI_SETTINGS) }
        SettingRow("Sistem ayarları", "Cihazın ayarlarını aç", vm.glass, null) { open(Settings.ACTION_SETTINGS) }
        SettingRow("Favorileri temizle", "Tüm favori uygulamaları kaldır", vm.glass, null) { vm.clearFavs() }
        Spacer(Modifier.weight(1f))
        Text("Geri tuşu ile ana ekrana dön. Uygulamada OK'a basılı tutarak favori ekle/çıkar.",
            color = Color.White.copy(0.5f), fontSize = 15.sp)
    }
}

@Composable
fun SettingRow(title: String, subtitle: String, glass: Boolean, checked: Boolean?, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .glass(glass, focused)
            .onFocusChanged { focused = it.isFocused }
            .tvClick(onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 20.sp)
            Text(subtitle, color = Color.White.copy(0.6f), fontSize = 14.sp)
        }
        if (checked == null) {
            Text("›", color = Color.White.copy(0.7f), fontSize = 28.sp)
        } else {
            Box(
                Modifier.size(56.dp, 32.dp).clip(RoundedCornerShape(50))
                    .background(if (checked) Color(0xFF30D158) else Color.White.copy(0.25f))
            ) {
                Box(
                    Modifier.padding(4.dp).size(24.dp).clip(CircleShape).background(Color.White)
                        .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
                )
            }
        }
    }
}
