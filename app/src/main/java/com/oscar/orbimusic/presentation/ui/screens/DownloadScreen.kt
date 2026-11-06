package com.oscar.orbimusic.presentation.ui.screens

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import com.oscar.orbimusic.presentation.ui.components.DownloadRowState
import com.oscar.orbimusic.presentation.ui.components.SearchResultCard
import com.oscar.orbimusic.presentation.viewmodel.DownloadViewModel
import kotlinx.coroutines.delay

//Tiempos de la entrada en cascada de los resultados
private const val STAGGER_STEP_MS = 40L      // retraso entre una fila y la siguiente
private const val STAGGER_MAX_STEPS = 10     // a partir de aqui todas entran juntas
private const val ENTRANCE_DURATION_MS = 220L // lo que dura el fade/slide de cada fila

@OptIn(UnstableApi::class)
@Composable
fun DownloadScreen(downloadViewModel: DownloadViewModel) {
    var title by remember { mutableStateOf("") }

    val searchResults by downloadViewModel.searchResults.collectAsState()
    val isSearching by downloadViewModel.isSearching.collectAsState()
    val isDownloading by downloadViewModel.isDownloading.collectAsState()
    val downloadingId by downloadViewModel.downloadingId.collectAsState()
    val downloadProgress by downloadViewModel.downloadProgress.collectAsState()
    val completedDownloads by downloadViewModel.completedDownloads.collectAsState()

    //Para esconder el teclado al lanzar la busqueda y dejar ver los resultados
    val keyboardController = LocalSoftwareKeyboardController.current
    var hasSearched by remember { mutableStateOf(false) }
    
    DisposableEffect(Unit) {
        onDispose { downloadViewModel.clearSearch() }
    }

    val widgetsList = listOf(
        Widget("Pop",        colors = listOf(Color(0xFFEB1855), Color(0xFFC52A70), Color(0xFFF59E0B))),
        Widget("Focus",      colors = listOf(Color(0xFF385D6C), Color(0xFF527C8C), Color(0xFFA9C3CC))),
        Widget("Workout",    colors = listOf(Color(0xFFA92D12), Color(0xFFED7C15), Color(0xFF30221A))),
        Widget("Chill",      colors = listOf(Color(0xFF153B86), Color(0xFF3477AD), Color(0xFF343B9B))),
        Widget("Rock",       colors = listOf(Color(0xFF142B26), Color(0xFF1D452F), Color(0xFF5F251F))),
        Widget("Jazz",       colors = listOf(Color(0xFF4B4120), Color(0xFF75622A), Color(0xFF1A1A17))),
        Widget("Electronic", colors = listOf(Color(0xFF075D76), Color(0xFF1293AA), Color(0xFF123D71))),
        Widget("Ambient",    colors = listOf(Color(0xFF24206A), Color(0xFF5E3187), Color(0xFFC574A9))),
    )

    val chunkedWidgets = widgetsList.chunked(2)

    Column(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 16.dp)) {
            Text(
                text = "Buscar",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Descubre algo nuevo",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            shape = RoundedCornerShape(12.dp),
            placeholder = {
                Text(
                    text = "Artista, canción o álbum...",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                //Solo aparece si hay algo escrito; limpiar devuelve la pantalla a los widgets
                if (title.isNotEmpty()) {
                    IconButton(onClick = {
                        title = ""
                        hasSearched = false
                        downloadViewModel.clearSearch()
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Limpiar busqueda",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            //La busqueda se dispara con la tecla de buscar, no en cada letra:
            //cada pulsacion seria un proceso de yt-dlp
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                hasSearched = title.isNotBlank()
                downloadViewModel.search(title)
                keyboardController?.hide()
            }),
            singleLine = true
        )

        when {
            isSearching -> {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                }
            }

            searchResults.isNotEmpty() -> {
                var entranceDone by remember(searchResults) { mutableStateOf(false) }
                LaunchedEffect(searchResults) {
                    delay(STAGGER_MAX_STEPS * STAGGER_STEP_MS + ENTRANCE_DURATION_MS)
                    entranceDone = true
                }

                LazyColumn(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    itemsIndexed(searchResults, key = { _, result -> result.id }) { index, result ->
                        //Si la fila se compone despues de la cascada (scroll),
                        //arranca ya visible y no se anima
                        var visible by remember(result.id) { mutableStateOf(entranceDone) }
                        LaunchedEffect(result.id) {
                            if (!visible) {
                                //El tope evita que las ultimas filas tarden casi
                                //un segundo en aparecer
                                delay(minOf(index, STAGGER_MAX_STEPS) * STAGGER_STEP_MS)
                                visible = true
                            }
                        }

                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(animationSpec = tween(ENTRANCE_DURATION_MS.toInt())) +
                                    //Arranca un cuarto de su altura mas abajo
                                    slideInVertically(
                                        animationSpec = tween(ENTRANCE_DURATION_MS.toInt())
                                    ) { it / 4 }
                        ) {
                            //El progreso es uno solo porque solo corre una
                            //descarga a la vez; downloadingId dice a que fila toca
                            val rowState = when {
                                result.id in completedDownloads -> DownloadRowState.Done
                                result.id != downloadingId -> DownloadRowState.Idle
                                downloadProgress < 0 -> DownloadRowState.Starting
                                else -> DownloadRowState.Running(downloadProgress)
                            }

                            SearchResultCard(
                                result = result,
                                //El servicio recibe la url tal cual viene de la busqueda
                                onDownload = {
                                    downloadViewModel.DownloadSong(
                                        result.id,
                                        result.url,
                                        result.title
                                    )
                                },
                                state = rowState,
                                downloadEnabled = !isDownloading
                            )
                        }
                    }
                }
            }

            //Se busco y no llego nada. Ojo: searchOnYt tambien devuelve lista
            //vacia cuando falla, asi que este mensaje cubre los dos casos
            hasSearched -> {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron resultados",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            //Sin busqueda activa: los widgets de siempre
            else -> {
                Column(Modifier.padding(horizontal = 10.dp, vertical = 12.dp)) {
                    for (elements in chunkedWidgets) {
                        Row {
                            PersonalizedWidget(
                                modifier = Modifier.weight(1f).padding(4.dp),
                                label = elements[0].label,
                                colors = elements[0].colors,
                                onClick = {
                                    title = elements[0].label
                                    hasSearched = true
                                    downloadViewModel.search(elements[0].label)
                                    keyboardController?.hide()
                                }
                            )
                            if (elements.size > 1) {
                                PersonalizedWidget(
                                    modifier = Modifier.weight(1f).padding(4.dp),
                                    label = elements[1].label,
                                    colors = elements[1].colors,
                                    onClick = {
                                        title = elements[1].label
                                        hasSearched = true
                                        downloadViewModel.search(elements[1].label)
                                        keyboardController?.hide()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

class Widget(val label: String, val colors: List<Color>)

@Composable
fun PersonalizedWidget(modifier: Modifier = Modifier, label: String, colors: List<Color>, onClick: () -> Unit) {
    val shape = RoundedCornerShape(15.dp)
    val gradient = Brush.linearGradient(
        colors = colors,
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(105.dp)
            .clip(shape)
            .background(gradient)
            .clickable { onClick() },
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                letterSpacing = (-0.4).sp
            ),
            color = Color.White,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
