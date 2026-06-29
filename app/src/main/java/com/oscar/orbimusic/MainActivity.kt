package com.oscar.orbimusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
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
import androidx.navigation.compose.rememberNavController
import org.w3c.dom.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val navController = rememberNavController();
            OrbiMusicTheme {
                Scaffold(modifier = Modifier.fillMaxSize(), bottomBar = {
                    Navbar(navController)
                }) { innerPadding ->
                    AppNavigation(modifier = Modifier.padding(innerPadding), hostController = navController)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(modifier: Modifier, hostController: NavHostController){

    NavHost(hostController, "home", modifier){

        composable("home"){
            HomeScreen()
        }

        composable("media"){
            MediaScreen()
        }

        composable("search"){
            SearchScreen()
        }


    }

}


@Composable
fun Navbar(navController: NavController){
    var selectedItem by rememberSaveable{ mutableStateOf(0) }

    val items = listOf("home", "media", "search");
    val selectedIcons = listOf(Icons.Filled.Home, Icons.Filled.Album, Icons.Filled.Search);
    val unselectedIcons = listOf(Icons.Filled.Home, Icons.Filled.Album, Icons.Filled.Search);



    NavigationBar(modifier = Modifier,
        containerColor = MaterialTheme.colorScheme.primary,
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
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    unselectedTextColor = MaterialTheme.colorScheme.onPrimary
                )
            );

        }

    }





}