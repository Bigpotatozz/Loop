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
│   │   └── FileScanner.kt
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
│   │       └── ExapandedPlayerScreen.kt
│   ├── utils/
│   │   └── parseTime.kt
│   └── viewmodel/
│       └── MediaViewModel.kt
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

## 🚀 Características en Desarrollo

- 🔄 **Implementación de YouTubeDL-Android**
  - Descargar música desde YouTube
  - Integración con el reproductor existente
  - Permisos y servicio de descargas

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

// Descargas (Nueva)
com.github.yausername:youtubedl-android
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
2. 🔄 Implementar YouTubeDL
   - Agregar permisos (lectura/escritura de almacenamiento)
   - Crear servicio de descargas
   - Integrar con MediaViewModel
   - UI para descargar canciones
3. 🔄 Sistema de favoritos
4. 🔄 Historial de reproducción
5. 🔄 Playlists personalizadas

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
```

## 🔗 Referencias

- Stack: MVVM + Clean Architecture
- Database: Room ORM
- Audio: Media3 + ExoPlayer
- UI Framework: Jetpack Compose
- Estado: Coroutines + Flow

---

**Última actualización:** 2026-08-05
**Versión:** 1.0
