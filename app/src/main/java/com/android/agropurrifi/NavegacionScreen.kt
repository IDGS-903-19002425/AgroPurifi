package com.android.agropurrifi

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.android.agropurrifi.Destinations.phScreen
import com.android.agropurrifi.Destinations.turbidezScreen

@Composable
fun BarraNavegacion() {
    val navController = rememberNavController()
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF007C68),
            ) {
                Destinations.items.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedItem == index,
                        onClick = {
                            navController.navigate(destination.ruta) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            selectedItem = index
                        },
                        icon = { Icon(painterResource(id = destination.iconoId), contentDescription = destination.label,tint = Color.White) },
                        label = { Text(destination.label, color = Color.White) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent,
                    )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.agropurifi),
                contentDescription = "Logo de la App",
                modifier = Modifier.size(400.dp)
            )
        }


        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}


@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Destinations.phScreen.ruta, modifier = modifier) {
        composable(Destinations.phScreen.ruta) { PHScreen() }
        composable(Destinations.turbidezScreen.ruta) { TurbidezScreen() }
    }
}

@Preview
@Composable
fun BarraNavegacionPreview() {
    BarraNavegacion()
}