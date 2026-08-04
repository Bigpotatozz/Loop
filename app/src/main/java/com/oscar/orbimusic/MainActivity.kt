package com.oscar.orbimusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.oscar.orbimusic.presentation.ui.screens.HomeScreen
import com.oscar.orbimusic.presentation.ui.screens.MediaScreen
import com.oscar.orbimusic.presentation.ui.screens.SearchScreen
import com.oscar.orbimusic.presentation.ui.theme.OrbiMusicTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import org.w3c.dom.Text
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.oscar.orbimusic.di.AppModule
import com.oscar.orbimusic.presentation.viewmodel.MediaViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {


    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[android.Manifest.permission.READ_MEDIA_AUDIO] ?: false
        val storageGranted = permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] ?: false

        if (audioGranted || storageGranted) {
            // Permiso otorgado, escanear archivos
            scanMusicFiles()
        } else {
            android.util.Log.e("MainActivity", "Permisos denegados por el usuario")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppModule.initialize(this)
        val mediaViewModel: MediaViewModel = AppModule.provideMediaViewModel()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions.launch(arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO))
        } else {
            requestPermissions.launch(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE))
        }



        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.BLACK,
                android.graphics.Color.WHITE
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.parseColor("#D9D9D9"), // Fondo gris abajo (tu terciario)
                android.graphics.Color.parseColor("#D9D9D9")
            )
        )
        setContent {
            val navController = rememberNavController();
            OrbiMusicTheme {
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
                                    bottomStart = 25.dp,
                                    bottomEnd = 25.dp
                                ))
                                .background(MaterialTheme.colorScheme.primary)){
                            AppNavigation(Modifier,
                                hostController = navController,
                                mediaViewModel)
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

@Composable
fun AppNavigation(modifier: Modifier, hostController: NavHostController, mediaViewModel: MediaViewModel){

    NavHost(hostController, "home", modifier){

        composable("home"){
            HomeScreen()
        }

        composable("media"){
            MediaScreen(mediaViewModel)
        }

        composable("search"){
            SearchScreen()
        }


    }


}



@Composable
fun Navbar(navController: NavController){
    var selectedItem by rememberSaveable{ mutableStateOf(0) }

    val items = listOf("Home", "Media", "Search");
    val selectedIcons = listOf(Icons.Filled.Home, Icons.Filled.Album, Icons.Filled.Search);
    val unselectedIcons = listOf(Icons.Filled.Home, Icons.Filled.Album, Icons.Filled.Search);



    NavigationBar(modifier = Modifier,
        containerColor = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.contentColorFor(NavigationBarDefaults.containerColor)) {


        items.forEachIndexed { index, item ->
            NavigationBarItem(icon = { Icon(if(selectedItem == index) selectedIcons[index] else unselectedIcons[index], contentDescription = item) },
                label = { Text(item) },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    navController.navigate(item)
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