# Notas de trabajo — OrbiMusic

Apuntes para retomar el trabajo en otra sesión o en otra máquina.
Lo que está en `CLAUDE.md` es la descripción del proyecto; esto son hallazgos
y decisiones vivas.

---

## Investigación: buscador de canciones por nombre (en curso)

**Objetivo:** que `DownloadScreen` permita buscar por nombre y descargar desde
los resultados, en lugar de pegar la URL de YouTube a mano.

**Estado:** investigación técnica terminada y verificada. El diseño de la
funcionalidad (cómo se ve y se comporta la pantalla) está **sin empezar** —
se cortó la conversación de brainstorming en la primera pregunta.

### Conclusión: se puede, sin dependencias nuevas

yt-dlp soporta búsqueda mediante pseudo-URLs. En vez de una URL real se le
pasa `ytsearch<N>:<consulta>`:

```
ytsearch20:bad bunny titi me pregunto
```

### Lo que NO funciona: `YoutubeDL.getInfo()`

Verificado leyendo las fuentes de `io.github.junkfood02.youtubedl-android:library:0.18.1`
(descomprimiendo `library-0.18.1-sources.jar` de la caché de Gradle).

```kotlin
// YoutubeDL.kt:109
fun getInfo(request: YoutubeDLRequest): VideoInfo {
    request.addOption("--dump-json")
    val response = execute(request, null, null)
    objectMapper.readValue(response.out, VideoInfo::class.java)   // UN solo objeto
}
```

Dos motivos por los que no sirve para búsquedas:

1. Deserializa a **un único** `VideoInfo`, no a una lista.
2. `VideoInfo` (en `mapper/VideoInfo.kt`) **no tiene campo `entries`**. Se
   revisaron sus ~40 campos: `id`, `title`, `fulltitle`, `thumbnail`,
   `duration`, `uploader`, `webpageUrl`, `formats`… todos de un solo vídeo.

Con `--dump-json` sobre una búsqueda, yt-dlp emite **un JSON por línea**, y
`readValue` no lo procesa correctamente.

### Lo que sí funciona: `execute()` directo

Saltarse `getInfo` y usar `execute()`, el mismo método que ya usa
`DownloadMusicService.downloadFromYt()`:

```kotlin
val request = YoutubeDLRequest("ytsearch20:$query").apply {
    addOption("--dump-single-json")   // un JSON único con "entries": [...]
    addOption("--flat-playlist")      // no resuelve cada vídeo entero
}
val json = YoutubeDL.getInstance().execute(request).out
```

- **`--dump-single-json`** devuelve un objeto con un array `entries`, en lugar
  de un JSON por línea. Mucho más fácil de parsear.
- **`--flat-playlist` no es opcional en la práctica**: sin él yt-dlp resuelve
  los formatos de los 20 vídeos uno por uno y la búsqueda tarda muchísimo.

Cada entrada trae `id`, `title`, `duration`, `channel`/`uploader` y
`thumbnails`. Para descargar luego basta construir
`https://www.youtube.com/watch?v=<id>`.

**Parseo:** con `org.json.JSONObject` / `JSONArray`, que vienen en el SDK de
Android. Cero dependencias nuevas. (Jackson también está disponible como
transitiva de la librería, por si se prefiere.)

### Consideraciones pendientes de decidir

- La búsqueda es una llamada bloqueante al binario de yt-dlp: va en
  `Dispatchers.IO`, nunca en el hilo principal.
- No hay cancelación ni *debounce* pensados todavía. `YoutubeDL` tiene
  `destroyProcessById(id)` si se quiere cancelar una búsqueda en curso.
- Descartada la YouTube Data API v3: exige clave de API y su cuota
  (100 unidades por búsqueda, 10.000 diarias) da para ~100 búsquedas al día.

### Estado actual de `DownloadScreen.kt`

Es un placeholder: dos `TextField` sin etiqueta (url y título) y un botón
"Descargar". Sin estilo, sin validación, sin progreso. El rediseño visual de
esta pantalla no se ha tocado; la lista, el mini-player y el expandido sí.

---

## Idea: personalización a partir del uso (sin diseñar)

**Estado:** idea del usuario, con un análisis de viabilidad hecho. **No hay
diseño ni plan.** Cuando se retome, empezar por brainstorming y escribir un
documento antes de tocar código — es un proyecto, no un retoque.

### La idea, en sus palabras

Recopilar cuántas veces se reproduce cada canción y usar ese dato para:

- **En Search:** accesos directos a los últimos lanzamientos o *hits* de los
  artistas que más escucha, para descargarlos sin buscarlos a mano.
- **En Home:** sus playlists, y debajo bloques tipo "tu artista más escuchado
  del mes" o "tu top de canciones".

El buscador manual de descarga (ver sección anterior) sigue existiendo; esto
se suma, no lo sustituye.

### La mitad fácil

Nada de esto tiene dificultad técnica real:

- **Registrar reproducciones.** Tabla `play_history` y una fila por escucha.
  `MediaViewModel.playSong()` es el punto donde ocurre.
- **Las estadísticas.** Son consultas SQL con `GROUP BY` y `COUNT`. Para esto
  existen las bases relacionales.
- **Playlists.** Tablas, CRUD y pantalla. Trabajo normal.

Detalle a decidir: **cuándo cuenta como "reproducida"**. `playSong()` se llama
también al saltar de canción, así que contar en la llamada infla las cifras al
pasar canciones rápido. Lo habitual es contar tras N segundos o al superar un
porcentaje de la duración.

### El punto débil: "últimos lanzamientos o hits"

Esta parte **no la puede resolver yt-dlp** y conviene saberlo antes de
prometerla.

Para listar lo que ha sacado un artista hace falta conocer su discografía.
`ytsearch:<artista>` devuelve lo que YouTube considera relevante para ese
texto: vídeos con letra, versiones, reacciones, directos antiguos. Es una
búsqueda, no una discografía.

Opciones reales:

- **El canal del artista** (`/@artista/videos`) ordenado por fecha. El problema
  es mapear el nombre de la etiqueta del archivo al canal oficial, que falla a
  menudo.
- **Una API de metadatos musicales.** MusicBrainz es gratuita y sin clave y sí
  da discografía de verdad; Last.fm daría "temas más escuchados". Implica
  dependencia nueva y red, pero es la herramienta correcta.

### La dependencia oculta: la calidad del campo `artist`

Todo el apartado de "tu artista más escuchado" se apoya en `songs.artist`, que
sale de los metadatos que yt-dlp incrusta al descargar:

```kotlin
val artist = retriever.extractMetadata(METADATA_KEY_ARTIST) ?: "Desconocido"
```

Riesgo conocido: en YouTube ese campo suele traer el nombre del canal
(`Bad Bunny - Topic`), el de un sello, o nada. Si está sucio, las
estadísticas agrupan mal y la función queda rara sin que se entienda por qué.

**Comprobado el 2026-10-31: el usuario revisó su biblioteca y los nombres de
artista salen limpios.** La idea es viable por ese lado, pero conviene
revisarlo otra vez si la biblioteca crece mucho.

### Tablas que haría falta modelar

Nada de esto existe hoy; la base tiene una sola tabla (`songs`).

- `play_history` — una fila por escucha (`songId`, `playedAt`)
- `playlists` + `playlist_songs` — relación muchos a muchos
- favoritos: columna en `songs` o tabla aparte, sin decidir

**Aviso importante:** `SongDatabase` usa `fallbackToDestructiveMigration()`,
que **borra la base entera** al cambiar de versión. Hoy no duele porque todo
se regenera escaneando la carpeta, pero estas tablas guardan datos que **solo
viven en la base**. Antes de crearlas hay que poner `exportSchema = true` y
escribir migraciones de verdad.

---

## Bug resuelto que conviene recordar: `403 Forbidden` permanente al descargar

Síntoma: las descargas funcionaban, y de pronto **todas** empezaron a fallar
con `HTTP Error 403: Forbidden`, incluso con URLs correctas. No se arreglaba
solo ni reiniciando.

**Causa: la caché de yt-dlp.** yt-dlp guarda datos del reproductor de YouTube
(entre otras cosas, funciones de firma). Si en una descarga concreta cachea
algo que YouTube luego rechaza, **todas las descargas siguientes dan 403**.
El síntoma no apunta en absoluto a la causa — parece una rotura general de
YouTube. La pista buena fue que empezó justo tras una descarga concreta.

**Arreglo aplicado:** `downloadFromYt` reintenta **una vez** con
`--rm-cache-dir` si el primer intento falla. En el caso normal se descarga con
caché a velocidad plena; si se envenena, la app se recupera sola. Las opciones
se extrajeron a `buildRequest(url, dir, clearCache)` para no duplicarlas.

Si el aviso `"Falló (código N), reintentando sin caché"` aparece a menudo en
Logcat, tocaría pasar a `--no-cache-dir` permanente.

**Pista falsa:** el log trae también `Your yt-dlp version (2025.11.12) is older
than 90 days` y un aviso sobre SABR. No era eso. Pero sigue siendo cierto que
el binario está desactualizado — ver el punto siguiente.

---

## Problemas conocidos sin arreglar

Detectados durante el trabajo de esta sesión. Ninguno bloquea, pero están
identificados con su causa.

**La actualización de yt-dlp no parece surtir efecto.**
`initializeYtDL()` llama a `updateYoutubeDL()` en cada arranque, pero el
binario sigue reportando `2025.11.12` (casi un año). Tres cosas a revisar:
1. El `UpdateStatus` que devuelve (`DONE` / `ALREADY_UP_TO_DATE`) se descarta,
   así que no se sabe si funciona. Habría que registrarlo.
2. `isInitialized.complete(true)` se ejecuta **antes** de actualizar, así que
   una descarga puede adelantar a la actualización y usar el binario viejo.
3. Usa el canal `STABLE` por defecto. Para roturas de YouTube, los arreglos
   llegan antes a `UpdateChannel.NIGHTLY`.
Sospechoso de que falle: la API de GitHub sin autenticar permite 60 peticiones
por hora y por IP; al agotarse, la actualización falla y el `catch` lo registra.

**`_isDownloading` se queda colgado al fallar una descarga.**
`DownloadViewModel` hace `_isDownloading.value = !progress.isCompleted`, y
cuando hay error el servicio envía `isCompleted = false`. El estado se queda
en "descargando" para siempre.

**Las portadas embebidas se guardan en `cacheDir`.**
`FileScanner.savePictureToFile()` escribe en `context.cacheDir/albumArth/`.
Android puede vaciar ese directorio cuando necesita espacio, y entonces
`albumArtPath` apunta a ficheros que ya no existen: las carátulas
desaparecen de forma intermitente. Mismo síntoma que el bug de las carpetas
ya resuelto. Arreglo: escribir en la carpeta de carátulas junto al audio.

**El navbar apila destinos.**
`MainActivity.Navbar` llama a `navigate()` sin `popUpTo` ni `launchSingleTop`,
así que cada toque de pestaña añade una entrada a la pila. Ir Home → Media →
Home → Media deja cuatro entradas y el botón atrás las recorre una a una.
Además `selectedItem` es estado local y puede desincronizarse de la ruta real.
Se planteó arreglarlo y **el usuario decidió dejarlo como está** por ahora.

**Rutas de almacenamiento duplicadas.**
`FileScanner` y `DownloadMusicService` construyen por separado la ruta de la
carpeta de música (`getExternalFilesDir(null)/Music`) y la de carátulas
(`arthPath`). Esa duplicación ya causó un bug: el servicio guardaba en
`arthPath` y el scanner buscaba en `album_art`, así que las portadas nunca
se encontraban. Ya está corregido igualando el nombre, pero siguen siendo
dos definiciones separadas que pueden volver a divergir. Se propuso un
`MusicStorage` como único origen; quedó sin aplicar.

---

## Decisiones de diseño tomadas

Para no re-litigar lo ya acordado:

- **Radios de esquina:** escala corta 8 / 10 / 12 dp. 8 para carátulas
  pequeñas, 10 para filas, 12 para superficies grandes. Las píldoras de
  filtro (`RoundedCornerShape(100)`) y los botones circulares se quedan
  redondos a propósito.
- **Iconos:** estilo `Icons.Rounded` en toda la app.
- **Modo claro/oscuro:** sigue al sistema, sin interruptor manual. La paleta
  se invierte, así que la barrita del reproductor pasa a clara en modo
  oscuro. **El reproductor expandido es oscuro siempre** y no sigue al tema
  (usa los tokens `PlayerSurface` / `PlayerInk` de `Color.kt`).
- **Transición barrita ↔ expandido:** `SharedTransitionLayout` con tres
  elementos compartidos. Las claves y los modificadores están en
  `presentation/ui/animations/SharedPlayerTransition.kt`. Cada clave debe
  aparecer **exactamente dos veces**, una en cada extremo; si aparece una
  sola vez no hay animación, solo un fundido.
