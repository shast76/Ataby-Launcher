# Ataby Launcher

tvOS tarzı, cam efektli Android/Google TV launcher.

## APK üretme
- GitHub'a yükle > Actions > "Build APK" > Artifacts > `TvLauncher-debug`
- veya Android Studio: Build > Build APK(s)

## Kurulum
```
adb connect TV_IP:5555
adb install -r app-debug.apk
```
Google TV'de uygulama "Uygulamalar" listesinden açılır. Varsayılan ana ekran yapmak cihaza göre değişir.

## Kullanım
- Bir uygulamada OK tuşuna basılı tut: favorilere ekle/çıkar
- Sağ üstteki "Ayarlar": cam efekti, saat biçimi, sistem ayarları kısayolları
