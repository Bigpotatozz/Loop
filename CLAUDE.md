# OrbiMusic - Contexto del Proyecto

## 📱 Descripción General

**OrbiMusic** es una aplicación Android de reproducción de música desarrollada en Kotlin con Jetpack Compose. El app escanea música local del dispositivo y permite reproducirla con controles avanzados.

**Estado:** En desarrollo activo
**Stack:** Kotlin + Jetpack Compose + Media3/ExoPlayer + Room Database

## 🏗️ Estructura del Proyecto

```
app/src/main/java/com/oscar/orbimusic/
├── data/
│   ├── audio/                 # Servicios de audio
│   │   ├── AudioPlayerService.kt
│   │   ├── AudioServiceConnection.kt
│   │   ├── DownloadMusicService.kt         # Descarga + búsqueda (todo yt-dlp)
│   │   ├── DownloadMusicServiceConnection.kt
│   │   └── FileScanner.kt
│   ├── models/                # Data classes de la capa de datos
│   │   ├── DownloadProgress.kt
│   │   └── YtSearchResult.kt               # ✨ Nuevo
│   ├── repository/
│   │   └── SongRepositoryImpl.kt
│   └── local/database/        # Room Database
│       ├── SongDao.kt
│       ├── SongDatabase.kt
│       └── SongEntity.kt
├── domain/
│   ├── model/
│   │   ├── PlayerState.kt
│   │   └── Song.kt
│   └── repository/
│       └── SongRepository.kt
├── presentation/
│   ├── ui/
│   │   ├── components/        # Componentes reutilizables
│   │   │   ├── MediaCard.kt
│   │   │   ├── PreviewMediaSong.kt
│   │   │   └── PastPrevMediaSong.kt
│   │   └── screens/
│   │       ├── ExapandedPlayerScreen.kt
│   │       └── DownloadScreen.kt           # ✨ Nuevo (en desarrollo)
│   ├── utils/
│   │   └── parseTime.kt
│   └── viewmodel/
│       ├── MediaViewModel.kt
│       └── DownloadViewModel.kt            # ✨ Nuevo
├── di/
│   └── AppModule.kt
└── MainActivity.kt
```

## 🎯 Características Actuales

- ✅ Escaneo de música local
- ✅ Reproducción de audio con ExoPlayer
- ✅ Controles de play/pause/siguiente/anterior
- ✅ Slider de progreso con tiempo
- ✅ Modo Shuffle
- ✅ Modo Repeat
- ✅ Pantalla expandida del reproductor
- ✅ Indicador de modo (Normal/Shuffle/Repeat)
- ✅ Botones con estilos mejorados (favorito, cerrar)
- ✅ **Descarga de música desde YouTube (backend)**
  - DownloadMusicService funcional
  - Descarga de audio M4A con metadatos
  - Integración con FileScanner para escaneo automático
  - Notificaciones de progreso
  - Guardado en carpeta privada de app

## 🚀 Características en Desarrollo

- 🔄 **UI para descargas**
  - DownloadScreen (pantalla de búsqueda)
  - Diálogo de descarga con progreso
  - Integración con DownloadViewModel

## 📋 Decisiones Arquitectónicas

### Estado y ViewModel
- Uso de `StateFlow` para reactive state management
- `MediaViewModel` centraliza toda la lógica del reproductor
- Estados: `playerState`, `shuffle`, `repeat`, `nextSong`, `currentPosition`

### UI
- Jetpack Compose para toda la interfaz
- Material Design 3 con tema personalizado (colores primarios)
- Estilos consistentes: contadores redondeados con fondo semi-transparente

### Audio
- **Media3/ExoPlayer** para reproducción robusta
- **AudioPlayerService** como servicio de foreground
- **AudioServiceConnection** maneja la conexión cliente-servidor

### Persistencia
- **Room Database** para metadatos de canciones
- Escaneo automático con `FileScanner` al iniciar

## 🔧 Dependencias Clave

```kotlin
// Compose
androidx.compose.material3
androidx.compose.ui
androidx.compose.foundation

// Audio
androidx.media3:media3-exoplayer:1.1.1
androidx.media3:media3-session:1.1.1
androidx.media:media:1.6.0

// Database
androidx.room:room-runtime
androidx.room:room-ktx

// Networking/Images
io.coil-kt:coil-compose:2.5.0

// Descargas ✨
io.github.junkfood02.youtubedl-android:library:0.18.1
io.github.junkfood02.youtubedl-android:ffmpeg:0.18.1
```

## 📌 Convenciones de Código

- **Naming:** camelCase para variables, PascalCase para Composables
- **Composables:** Sufijo opcional, pero se entiende por contexto
- **Modificadores:** Siempre el primer parámetro
- **Estados:** Se declaran con `collectAsState()` dentro del Composable
- **Colores:** Usar `MaterialTheme.colorScheme.primary` en lugar de hardcoding

## 🎨 Estilos UI

### Header del Reproductor Expandido
- 3 elementos con contenedores redondeados (BorderRadius 8/100)
- Fondo: `primary.copy(alpha = 0.2f)`
- Padding: 6.dp
- Distribuidos con `SpaceBetween`

### Componentes
- Imágenes con clip `RoundedCornerShape(25.dp)`
- Textos primarios: `fontSize = 24.sp`, color blanco
- Textos secundarios: `fontSize = 18.sp`, color gris

## ⚙️ Configuración Actual

- **Min SDK:** 24
- **Target SDK:** 36
- **Compile SDK:** 36
- **Java Version:** 11
- **Repositorios:** google(), mavenCentral(), maven("https://jitpack.io")

## 📝 Próximas Tareas

1. ✅ Mejorar header del ExpandedPlayerScreen
2. ✅ Backend de YouTubeDL completado
   - ✅ Agregar permisos
   - ✅ Crear DownloadMusicService
   - ✅ Integrar con FileScanner
   - ✅ Descargas con metadatos
3. 🔄 **Búsqueda en YouTube (PRIORIDAD)**
   - ✅ `searchOnYt()` en DownloadMusicService
   - ✅ `search()` / `clearSearch()` + `searchResults` / `isSearching` en DownloadViewModel
   - 🔄 Lista de resultados en DownloadScreen con botón de descarga por fila
4. ⏸️ **Limitar a una descarga a la vez** (pendiente, decidido pero no aplicado)
5. 🔄 Sistema de favoritos
6. 🔄 Historial de reproducción
7. 🔄 Playlists personalizadas

## 🔧 Notas Técnicas de la Sesión

### DownloadMusicService
- Servicio de foreground que descarga desde YouTube
- Usa YouTubeDL (yt-dlp) para extraer audio
- Configura `--add-metadata` y `--embed-metadata` para metadatos
- Requiere FFmpeg inicializado para procesar metadatos
- Guarda en `/data/data/com.oscar.orbimusic/files/Music/`
- Notifica mediante callback `DownloadProgress`

### Búsqueda en YouTube (`searchOnYt`)
- Vive en `DownloadMusicService` por decisión explícita: todo lo de yt-dlp
  (init, update, búsqueda y descarga) queda centralizado ahí aunque el nombre
  del servicio no lo refleje.
- Es `suspend` y **no** fire-and-forget como la descarga: devuelve la lista
  directamente, sin callback. Tampoco levanta foreground ni notificación —
  es pedir y recibir, no trabajo largo.
- Espera el mismo `isInitialized` que la descarga; sin eso yt-dlp lanza
  "instance not initialized".
- Usa `ytsearch20:<query>` con `--flat-playlist --dump-json --no-warnings`.
  **`--flat-playlist` es crítico**: sin él yt-dlp extrae los 20 videos uno por
  uno (20 peticiones completas) y la búsqueda se vuelve inusable.
- `--dump-json` imprime **un JSON por línea**, no un array. Se parsea con
  `org.json.JSONObject` (viene en Android, cero dependencias nuevas) y cada
  línea va en su propio try/catch para que un resultado roto no tumbe los otros.
- En modo flat faltan campos: el canal puede venir en `channel` o en `uploader`,
  y `webpage_url` suele no venir — se arma desde el `id`.
- La miniatura se construye como `https://i.ytimg.com/vi/<id>/hqdefault.jpg` en
  vez de leer el array `thumbnails`: siempre existe para un id válido y trae un
  tamaño predecible, en lugar de la de 1280px que yt-dlp deja al final del array.
- Devuelve `emptyList()` si falla. **No distingue "sin resultados" de "error"**;
  si hace falta ese estado en la UI, hay que agregarlo.

### Sincronización con FileScanner
- FileScanner escanea la misma carpeta que DownloadService
- Al completar descarga, `scanMusicFolder()` la detecta automáticamente
- Metadatos se extraen con `MediaMetadataRetriever`
- Las canciones descargadas aparecen en MediaScreen sin recargar

### Problemas Resueltos
- Native libraries no se empaquetaban: agregado `useLegacyPackaging = true`
- YouTubeDL no se inicializaba: Agregado `YoutubeDL.getInstance().init()`
- Metadatos no se incluían: Agregado `--add-metadata` y `--embed-metadata`
- FFmpeg no estaba inicializado: Descomentado `FFmpeg.getInstance().init()`

### Problemas Conocidos (pendientes)

**Descargas simultáneas se pisan entre sí** — `downloadFromYt` identifica lo que acaba
de bajar listando la carpeta `Music/` y tomando el `.m4a` más reciente
(`DownloadMusicService.kt:211`), y hace lo mismo con el `.jpg` de la carátula
(`DownloadMusicService.kt:231`). Con una sola descarga a la vez es correcto; con
dos o más, ambas leen el mismo archivo y una de las carátulas nunca se mueve a
`arthPath/`, así que esa canción se queda sin portada.

Decisión tomada: limitarlo a **una descarga a la vez** con un `Mutex` en
`downloadFromYt`, en vez de una carpeta de staging. Se aplica después de terminar
la búsqueda. Lo que hace falta junto con el `Mutex`:

- `isDownloading` nunca se pone en `true`: se calcula desde el callback
  (`DownloadViewModel.kt:52`) y el callback solo se dispara al terminar o al
  fallar. `DownloadSong` tiene que marcarlo al arrancar para poder deshabilitar
  los botones de descarga mientras hay una en curso.
- `downloadService!!` (`DownloadViewModel.kt:75`) lanza NPE si se toca un botón
  antes de que el servicio termine de enlazarse.

Hueco que el `Mutex` **no** cubre, aceptado a conciencia: si la app se cierra
mientras el servicio sigue descargando, al reabrirla `MainActivity.kt:80` escanea
y puede indexar un `.m4a` a medias. Como `searchByPath` después lo da por
"repetida sin cambios" (`FileScanner.kt:56`), queda mal permanentemente. Esto sí
lo arreglaría la carpeta de staging.

**Código muerto:** el bloque de `MediaMetadataRetriever` en
`DownloadMusicService.kt:219-222` extrae `audioTitle` y nunca lo usa.

## 🐛 Notas Técnicas Importantes

- El nombre del archivo tiene typo: `ExapandedPlayerScreen` (debería ser `ExpandedPlayerScreen`)
- El `ModeIndicator` muestra el estado actual de shuffle/repeat
- Los botones del header son placeholders (TODO: Implementar menú y favoritos)
- La reproducción maneja correctamente el caso cuando la cola está vacía

## 📱 Permisos Actuales

```xml
<!-- AndroidManifest.xml -->
- READ_EXTERNAL_STORAGE
- READ_MEDIA_AUDIO
- WRITE_EXTERNAL_STORAGE          ✨ Para descargas
- INTERNET                        ✨ Para YouTube
- FOREGROUND_SERVICE              ✨ Servicio de descargas
- FOREGROUND_SERVICE_MEDIA_PLAYBACK
- FOREGROUND_SERVICE_DATA_SYNC    ✨ Para descargas en foreground
- requestLegacyExternalStorage    ✨ Android 10 compatibility
```

### Configuración Build Gradle
```kotlin
// Packaging
packaging {
    jniLibs {
        useLegacyPackaging = true  // ✨ Para native libraries
    }
}

// ABI Filters
ndk {
    abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64"))
}
```

## 🔗 Referencias

- Stack: MVVM + Clean Architecture
- Database: Room ORM
- Audio: Media3 + ExoPlayer
- UI Framework: Jetpack Compose
- Estado: Coroutines + Flow

---

**Última actualización:** 2026-11-05
**Versión:** 1.2 (búsqueda en YouTube: capa de servicio lista, falta ViewModel y UI)
