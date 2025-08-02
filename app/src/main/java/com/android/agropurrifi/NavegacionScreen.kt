// BarraNavegacion.kt modificado
package com.android.agropurrifi

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
// Removemos los imports de Material Icons que no usaremos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.android.agropurrifi.Destinations.dashboardScreen
import com.android.agropurrifi.Destinations.filtrosScreen
import com.android.agropurrifi.Destinations.phScreen
import com.android.agropurrifi.Destinations.turbidezScreen

@Composable
fun BarraNavegacion() {
    val navController = rememberNavController()
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1E3A8A),
                modifier = Modifier.height(80.dp)
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
                        icon = {
                            when (destination) {
                                is dashboardScreen -> Icon(
                                    Icons.Default.Home,
                                    contentDescription = destination.label,
                                    tint = if (selectedItem == index) Color.White else Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(24.dp)
                                )
                                is phScreen -> Image(
                                    painter = painterResource(id = R.drawable.ph), // Ícono personalizado de pH
                                    contentDescription = destination.label,
                                    modifier = Modifier.size(24.dp)
                                    // Nota: Image no tiene tint, si necesitas colorear el ícono,
                                    // asegúrate de que sea un vector drawable o usa Icon con ImageVector
                                )
                                is turbidezScreen -> Image(
                                    painter = painterResource(id = R.drawable.turbidez), // Asumiendo que tienes turbidez.png
                                    contentDescription = destination.label,
                                    modifier = Modifier.size(24.dp)
                                )
                                is filtrosScreen -> Image(
                                    painter = painterResource(id = R.drawable.filtrar), // Asumiendo que tienes filtrar.png
                                    contentDescription = destination.label,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                destination.label,
                                color = if (selectedItem == index) Color.White else Color.White.copy(alpha = 0.7f)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.White.copy(alpha = 0.2f),
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            selectedTextColor = Color.White,
                            unselectedTextColor = Color.White.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            selectedItem = selectedItem,
            onItemSelected = { selectedItem = it },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Destinations.dashboardScreen.ruta,
        modifier = modifier
    ) {
        composable(Destinations.dashboardScreen.ruta) {
            MainDashboard(
                onNavigateToFilters = {
                    // Navegar a filtros y actualizar el item seleccionado
                    navController.navigate(Destinations.filtrosScreen.ruta) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                    // Actualizar el item seleccionado al índice de filtros
                    val filtrosIndex = Destinations.items.indexOf(Destinations.filtrosScreen)
                    if (filtrosIndex != -1) {
                        onItemSelected(filtrosIndex)
                    }
                }
            )
        }
        composable(Destinations.phScreen.ruta) { PHScreen() }
        composable(Destinations.turbidezScreen.ruta) { TurbidezScreen() }
        composable(Destinations.filtrosScreen.ruta) { FiltrosScreen() }
    }
}

@Preview
@Composable
fun BarraNavegacionPreview() {
    BarraNavegacion()
}