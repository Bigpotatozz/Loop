package com.oscar.orbimusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.oscar.orbimusic.presentation.ui.animations.LocalPlayerAnimatedScope
import com.oscar.orbimusic.presentation.ui.animations.LocalSharedTransitionScope
import com.oscar.orbimusic.presentation.ui.screens.ExpandedPlayerScreen
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.oscar.orbimusic.presentation.ui.screens.AlbumScreen
import com.oscar.orbimusic.presentation.viewmodel.AlbumScreenViewModel
import com.oscar.orbimusic.presentation.ui.screens.HomeScreen
import com.oscar.orbimusic.presentation.ui.screens.MediaScreen
import com.oscar.orbimusic.presentation.ui.theme.OrbiMusicTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.oscar.orbimusic.di.AppModule
import com.oscar.orbimusic.presentation.ui.screens.DownloadScreen
import com.oscar.orbimusic.presentation.viewmodel.DownloadViewModel
import com.oscar.orbimusic.presentation.viewmodel.HomeViewModel
import com.oscar.orbimusic.presentation.viewmodel.MediaViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {


    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[android.Manifest.permission.READ_MEDIA_AUDIO] ?: false
        val storageGranted = permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
        val writeStorageGranted = permissions[android.Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false

        if (audioGranted || storageGranted || writeStorageGranted) {
            // Permiso otorgado, escanear archivos
            scanMusicFiles()
        } else {
            android.util.Log.e("MainActivity", "Permisos denegados por el usuario")
        }
    }

    @OptIn(UnstableApi::class, ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppModule.initialize(this)

        val mediaViewModel: MediaViewModel =
            ViewModelProvider(this, AppModule.mediaViewModelFactory)[MediaViewModel::class.java]
        val downloadViewModel: DownloadViewModel =
            ViewModelProvider(this, AppModule.downloadViewModelFactory)[DownloadViewModel::class.java]

        val homeViewModel: HomeViewModel = ViewModelProvider(this, AppModule.homeViewModelFactory)[HomeViewModel::class.java]

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions.launch(arrayOf(
                android.Manifest.permission.READ_MEDIA_AUDIO,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                //Sin este permiso la notificacion de progreso de descarga no se ve
                android.Manifest.permission.POST_NOTIFICATIONS
            ))
        } else {
            requestPermissions.launch(arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        }
        
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.parseColor("#D9D9D9"), // terciario claro
                android.graphics.Color.parseColor("#1C1C1E")  // terciario oscuro
            )
        )
        setContent {
            val navController = rememberNavController();
            val isExpanded by mediaViewModel.isExpanded.collectAsState()
            val playerState by mediaViewModel.playerState.collectAsState()

            // Es la view o variable que almacena toda la activity
            val view = LocalView.current
            val darkTheme = isSystemInDarkTheme()
            LaunchedEffect(isExpanded, darkTheme) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !isExpanded && !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }

            OrbiMusicTheme {


                //Es una caja que me da herramientas para animacion y las ejecuta
                SharedTransitionLayout {
                    //Accede y guarda un valor al que van a poder acceder todos sus hijos
                    CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                        Box(Modifier.fillMaxSize()) {

                            Scaffold(modifier = Modifier.fillMaxSize(),
                                containerColor = MaterialTheme.colorScheme.primary,
                                bottomBar = {
                                Navbar(navController)
                            }) { innerPadding ->

                                Box(Modifier.fillMaxSize()
                                    .padding(innerPadding)
                                    .background(MaterialTheme.colorScheme.tertiary)){

                                    Box(Modifier.clip(RoundedCornerShape(
                                                topStart = 0.dp,
                                                topEnd = 0.dp,
                                                bottomStart = 12.dp,
                                                bottomEnd = 12.dp
                                            ))
                                            .background(MaterialTheme.colorScheme.primary)){
                                        AppNavigation(Modifier,
                                            hostController = navController,
                                            mediaViewModel,
                                            downloadViewModel,
                                            homeViewModel)
                                    }

                                }

                            }
                            //Muestra u oculta la animacion
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                CompositionLocalProvider(LocalPlayerAnimatedScope provides this) {
                                    //Si es expanded el boton hacia atras simplemente pone el expanded en false
                                    BackHandler(enabled = isExpanded) {
                                        mediaViewModel.setExpanded(false)
                                    }
                                    
                                    //Componente expandedPlayer, recibe el playerState y su respectivo viewModel
                                    ExpandedPlayerScreen(playerState, mediaViewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun scanMusicFiles(){
        lifecycleScope.launch {
            AppModule.scanMusicFiles(this@MainActivity);
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun AppNavigation(modifier: Modifier, hostController: NavHostController, mediaViewModel: MediaViewModel, downloadViewModel: DownloadViewModel, homeViewModel: HomeViewModel){

    NavHost(hostController, "home", modifier){

        composable("home"){
            HomeScreen(homeViewModel, onAlbumClick = { id ->
                hostController.navigate("album/$id")
            })
        }

        composable(
            "album/{albumId}",
            arguments = listOf(navArgument("albumId") { type = NavType.LongType })
        ){ entry ->

            val albumViewModel = ViewModelProvider(
                entry, AppModule.albumViewModelFactory
            )[AlbumScreenViewModel::class.java]

            AlbumScreen(
                albumId = entry.arguments?.getLong("albumId") ?: 0L,
                albumViewModel = albumViewModel,
                onBack = { hostController.popBackStack() }
            )
        }

        composable("media"){
            MediaScreen(mediaViewModel)
        }

        composable("search"){
            DownloadScreen(downloadViewModel);
        }


    }


}



@Composable
fun Navbar(navController: NavController){
    var selectedItem by rememberSaveable{ mutableStateOf(0) }

    val items = listOf("Home", "Media", "Search");
    val selectedIcons = listOf(Icons.Rounded.Home, Icons.Rounded.Album, Icons.Rounded.Search);
    val unselectedIcons = listOf(Icons.Rounded.Home, Icons.Rounded.Album, Icons.Rounded.Search);



    NavigationBar(modifier = Modifier,
        containerColor = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.contentColorFor(NavigationBarDefaults.containerColor)) {


        items.forEachIndexed { index, item ->
            NavigationBarItem(icon = { Icon(if(selectedItem == index) selectedIcons[index] else unselectedIcons[index], contentDescription = item) },
                label = { Text(item) },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    navController.navigate(item.lowercase())
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                    indicatorColor = MaterialTheme.colorScheme.tertiary,
                    unselectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    unselectedTextColor = MaterialTheme.colorScheme.onPrimary
                )
            );

        }


    }










}