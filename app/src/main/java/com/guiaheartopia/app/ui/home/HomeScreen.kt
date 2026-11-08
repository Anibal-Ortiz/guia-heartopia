package com.guiaheartopia.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    // Ajusta estos nombres si en tu MainActivity se llaman distinto
    onNavigateToGuide: () -> Unit = {},
    onNavigateToRoutine: () -> Unit = {},
    onNavigateToWeather: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    onNavigateToHobbies: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            // Cabecera Acogedora
            Text(
                text = "¡Hola de nuevo!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "¿Qué aventura toca hoy en Heartopia?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Cuadrícula de Tarjetas Modernas (Sin Pixel Art)
            val menuItems = listOf(
                MenuItem("Guía", Icons.Rounded.MenuBook, onNavigateToGuide),
                MenuItem("Rutinas", Icons.Rounded.Checklist, onNavigateToRoutine),
                MenuItem("Clima", Icons.Rounded.WbSunny, onNavigateToWeather),
                MenuItem("Mapa", Icons.Rounded.Map, onNavigateToMap),
                MenuItem("Hobbies", Icons.Rounded.SportsEsports, onNavigateToHobbies),
                MenuItem("Ajustes", Icons.Rounded.Settings, onNavigateToSettings)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(menuItems) { item ->
                    HomeCard(item)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeCard(item: MenuItem) {
    ElevatedCard(
        onClick = item.onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Hace que la tarjeta sea cuadrada
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

data class MenuItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)