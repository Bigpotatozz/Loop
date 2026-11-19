package com.oscar.orbimusic.presentation.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.oscar.orbimusic.data.models.YtSearchResult
import com.oscar.orbimusic.presentation.ui.theme.LoopTheme

//En que punto de la descarga esta esta fila
sealed interface DownloadRowState {
    //Sin descargar: se muestra el boton
    data object Idle : DownloadRowState
    //Se lanzo pero yt-dlp aun no reporta porcentaje: spinner indeterminado
    data object Starting : DownloadRowState
    data class Running(val percent: Int) : DownloadRowState
    data object Done : DownloadRowState
}

//Fila de un resultado de busqueda de YouTube.
@Composable
fun SearchResultCard(
    modifier: Modifier = Modifier,
    result: YtSearchResult,
    onDownload: () -> Unit,
    state: DownloadRowState = DownloadRowState.Idle,
    downloadEnabled: Boolean = true
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //La miniatura llega en 16:9 (mqdefault). Crop la recorta por los lados
        //para llenar el cuadrado sin deformarla ni dejar huecos: en las
        //canciones del catalogo el centro es justo la portada.
        AsyncImage(
            model = result.thumbnailUrl,
            contentDescription = result.title,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                text = result.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.2).sp
                ),
                //Dos lineas: los titulos del catalogo pueden ser largos y aqui
                //no hay subtitulo que les quite espacio
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        DownloadStateButton(
            state = state,
            title = result.title,
            enabled = downloadEnabled,
            onDownload = onDownload
        )
    }
}


//El boton de descarga con sus cuatro estados. Publico porque lo comparten la
//busqueda y el detalle de album.
@Composable
fun DownloadStateButton(
    state: DownloadRowState,
    title: String,
    enabled: Boolean,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    buttonSize: Dp = 48.dp,
    iconSize: Dp = 24.dp
) {
    //Tamaño fijo para que la fila no se mueva al cambiar de estado,
    //igual que el Box de ancho fijo de MediaSongCard
    Box(modifier.size(buttonSize), contentAlignment = Alignment.Center) {
        //secondary es la tinta de alto contraste en este tema;
        //primary es el color de fondo, no un acento
        val accent = tint ?: MaterialTheme.colorScheme.secondary

        when (state) {
            DownloadRowState.Idle -> {
                IconButton(
                    onClick = onDownload,
                    enabled = enabled,
                    modifier = Modifier.size(buttonSize)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = "Descargar $title",
                        //Con tinte propio (sobre una caratula) se apaga el mismo
                        //color: caer a onSurfaceVariant lo haria desaparecer.
                        //Sin tinte, las filas conservan el gris de siempre.
                        tint = when {
                            enabled -> accent
                            tint != null -> accent.copy(alpha = 0.4f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(iconSize)
                    )
                }
            }

            DownloadRowState.Starting -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(iconSize),
                    strokeWidth = 2.dp,
                    color = accent
                )
            }

            is DownloadRowState.Running -> {
                CircularProgressIndicator(
                    progress = { state.percent / 100f },
                    modifier = Modifier.size(iconSize),
                    strokeWidth = 2.dp,
                    color = accent
                )
            }

            DownloadRowState.Done -> {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Descargada",
                    tint = accent,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

@Preview(name = "Claro", showBackground = true)
@Preview(name = "Oscuro", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SearchResultCardPreview() {
    LoopTheme {
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(vertical = 12.dp)
        ) {
            //Un estado por fila para poder compararlos de un vistazo
            SearchResultCard(
                result = YtSearchResult(
                    id = "1",
                    title = "Bad Bunny - MONACO (Video Oficial)",
                    channel = "Bad Bunny",
                    durationSeconds = 291,
                    thumbnailUrl = "",
                    url = ""
                ),
                onDownload = {},
                state = DownloadRowState.Idle
            )
            SearchResultCard(
                result = YtSearchResult(
                    id = "2",
                    title = "Arrancando, todavía sin porcentaje",
                    channel = "Canal",
                    durationSeconds = 180,
                    thumbnailUrl = "",
                    url = ""
                ),
                onDownload = {},
                state = DownloadRowState.Starting
            )
            SearchResultCard(
                result = YtSearchResult(
                    id = "3",
                    title = "Descargando a la mitad",
                    channel = "Canal",
                    durationSeconds = 240,
                    thumbnailUrl = "",
                    url = ""
                ),
                onDownload = {},
                state = DownloadRowState.Running(55)
            )
            SearchResultCard(
                result = YtSearchResult(
                    id = "4",
                    title = "Un título muy largo que no cabe en una sola línea y tiene que cortarse",
                    channel = "Canal con nombre largo",
                    durationSeconds = 3725,
                    thumbnailUrl = "",
                    url = ""
                ),
                onDownload = {},
                state = DownloadRowState.Done
            )
        }
    }
}
