package com.oscar.orbimusic.presentation.ui.screens

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import com.oscar.orbimusic.presentation.viewmodel.DownloadViewModel

@OptIn(UnstableApi::class)
@Composable
fun DownloadScreen(downloadViewModel: DownloadViewModel) {
    var title by rememberSaveable { mutableStateOf("") }

    val widgetsList = listOf(
        Widget("Pop",        url = "idk", colors = listOf(Color(0xFFEB1855), Color(0xFFC52A70), Color(0xFFF59E0B))),
        Widget("Focus",      url = "idk", colors = listOf(Color(0xFF385D6C), Color(0xFF527C8C), Color(0xFFA9C3CC))),
        Widget("Workout",    url = "idk", colors = listOf(Color(0xFFA92D12), Color(0xFFED7C15), Color(0xFF30221A))),
        Widget("Chill",      url = "idk", colors = listOf(Color(0xFF153B86), Color(0xFF3477AD), Color(0xFF343B9B))),
        Widget("Rock",       url = "idk", colors = listOf(Color(0xFF142B26), Color(0xFF1D452F), Color(0xFF5F251F))),
        Widget("Jazz",       url = "idk", colors = listOf(Color(0xFF4B4120), Color(0xFF75622A), Color(0xFF1A1A17))),
        Widget("Electronic", url = "idk", colors = listOf(Color(0xFF075D76), Color(0xFF1293AA), Color(0xFF123D71))),
        Widget("Ambient",    url = "idk", colors = listOf(Color(0xFF24206A), Color(0xFF5E3187), Color(0xFFC574A9))),
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

        OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = title, onValueChange = { title = it })

        Column(Modifier.padding(5.dp)) {
            for (elements in chunkedWidgets) {
                Row {
                    PersonalizedWidget(
                        modifier = Modifier.weight(1f).padding(2.dp),
                        label = elements[0].label,
                        url = elements[0].url,
                        colors = elements[0].colors
                    )
                    if (elements.size > 1) {
                        PersonalizedWidget(
                            modifier = Modifier.weight(1f).padding(2.dp),
                            label = elements[1].label,
                            url = elements[1].url,
                            colors = elements[1].colors
                        )
                    }
                }
            }
        }
    }
}

class Widget(val label: String, val url: String, val colors: List<Color>)

@Composable
fun PersonalizedWidget(modifier: Modifier = Modifier, label: String, url: String, colors: List<Color>) {
    val shape = RoundedCornerShape(15.dp)
    val gradient = Brush.linearGradient(
        colors = colors,
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(shape)
            .background(gradient)
            .clickable { print(url) },
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
