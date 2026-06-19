package com.example.elbosqueapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import com.example.elbosqueapp.data.local.AppDatabase
import com.example.elbosqueapp.data.repository.ProductoRepository
import com.example.elbosqueapp.ui.screens.ProductoViewModel
import com.example.elbosqueapp.ui.screens.ProductoViewModelFactory
import com.example.elbosqueapp.ui.screens.ProductosScreen
import com.example.elbosqueapp.ui.screens.PedidoScreen
import com.example.elbosqueapp.ui.screens.Pantalla
import com.example.elbosqueapp.ui.theme.ElBosqueAppTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val repository = ProductoRepository(
            database.productoDao(),
            database.pedidoDao()
        )
        val factory = ProductoViewModelFactory(repository)

        setContent {
            ElBosqueAppTheme {
                val productoViewModel: ProductoViewModel = viewModel(factory = factory)
                var pantallaActual by remember { mutableStateOf(Pantalla.PRODUCTOS) }
                val cantidadTotal by productoViewModel.cantidadTotal.collectAsState()
                LaunchedEffect(Unit) {
                    productoViewModel.cargarPedidoGuardado()
                }
                Column {

                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        when (pantallaActual) {
                            Pantalla.PRODUCTOS -> ProductosScreen(viewModel = productoViewModel)
                            Pantalla.PEDIDO -> PedidoScreen(viewModel = productoViewModel)
                        }
                    }

                    NavigationBar {
                        NavigationBarItem(
                            selected = pantallaActual == Pantalla.PRODUCTOS,
                            onClick = { pantallaActual = Pantalla.PRODUCTOS },
                            label = { Text("Productos") },
                            icon = { Text("📦") }
                        )

                        NavigationBarItem(
                            selected = pantallaActual == Pantalla.PEDIDO,
                            onClick = { pantallaActual = Pantalla.PEDIDO },
                            label = { Text("Pedido") },
                            icon = {
                                Text("🛒 $cantidadTotal")
                            }                        )
                    }
                }
            }
        }
    }
}