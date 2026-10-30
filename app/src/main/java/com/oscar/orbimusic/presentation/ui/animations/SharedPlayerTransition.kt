package com.oscar.orbimusic.presentation.ui.animations

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

//Scopes de la transicion barrita <-> expandido.
//Son nulos por defecto a proposito: asi los @Preview siguen funcionando
//sin tener que montar un SharedTransitionLayout alrededor.
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalPlayerAnimatedScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

//Claves de los elementos que viajan de la barrita al expandido
const val PlayerArtKey = "player-art"
const val PlayerTitleKey = "player-title"
const val PlayerContainerKey = "player-container"

@OptIn(ExperimentalSharedTransitionApi::class)
private val playerBounds = BoundsTransform { _, _ ->
    spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
}

//Marca el elemento como compartido solo si hay una transicion montada alrededor.
//Fuera de un SharedTransitionLayout se queda en no-op y el @Preview sigue funcionando.
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedPlayerElement(key: String): Modifier {
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    val animatedScope = LocalPlayerAnimatedScope.current ?: return this

    return with(sharedScope) {
        this@sharedPlayerElement.sharedElement(
            sharedContentState = rememberSharedContentState(key = key),
            animatedVisibilityScope = animatedScope,
            boundsTransform = playerBounds
        )
    }
}

//Para contenedores que ademas de moverse cambian de contenido.
//corner = radio de ESTE lado, cornerOther = radio del lado contrario.
//Hacen falta porque en la capa de overlay el elemento se escapa del clip de su
//padre: si el radio no viaja con el, se ve cuadrado durante toda la transicion.
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedPlayerBounds(
    key: String,
    corner: Dp = 0.dp,
    cornerOther: Dp = 0.dp
): Modifier {
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    val animatedScope = LocalPlayerAnimatedScope.current ?: return this

    //Interpola el radio al mismo ritmo que la entrada/salida
    val radius by animatedScope.transition.animateDp(label = "corner-$key") { state ->
        if (state == EnterExitState.Visible) corner else cornerOther
    }

    return with(sharedScope) {
        this@sharedPlayerBounds.sharedBounds(
            sharedContentState = rememberSharedContentState(key = key),
            animatedVisibilityScope = animatedScope,
            boundsTransform = playerBounds,
            clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(radius))
        )
    }
}
