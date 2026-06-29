package com.example.elbosqueapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.elbosqueapp.data.local.AppDatabase
import com.example.elbosqueapp.data.repository.ProductoRepository
import com.example.elbosqueapp.data.repository.VentaRepository
import com.example.elbosqueapp.ui.screens.DetalleVentaScreen
import com.example.elbosqueapp.ui.screens.HistorialVentasScreen
import com.example.elbosqueapp.ui.screens.Pantalla
import com.example.elbosqueapp.ui.screens.PedidoScreen
import com.example.elbosqueapp.ui.screens.ProductoViewModel
import com.example.elbosqueapp.ui.screens.ProductoViewModelFactory
import com.example.elbosqueapp.ui.screens.ProductosScreen
import com.example.elbosqueapp.ui.screens.VentaViewModel
import com.example.elbosqueapp.ui.screens.VentaViewModelFactory
import com.example.elbosqueapp.ui.screens.VentasScreen
import com.example.elbosqueapp.ui.theme.ElBosqueAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)

        val productoRepository = ProductoRepository(
            database.productoDao(),
            database.pedidoDao()
        )

        val ventaRepository = VentaRepository(
            database.productoDao(),
            database.ventaDao()
        )

        val productoFactory = ProductoViewModelFactory(productoRepository)
        val ventaFactory = VentaViewModelFactory(ventaRepository)

        setContent {
            ElBosqueAppTheme {
                val productoViewModel: ProductoViewModel = viewModel(factory = productoFactory)
                val ventaViewModel: VentaViewModel = viewModel(factory = ventaFactory)

                var pantallaActual by remember { mutableStateOf(Pantalla.PRODUCTOS) }
                var selectedVentaId by remember { mutableStateOf<Long?>(null) }

                val cantidadTotalPedido by productoViewModel.cantidadTotal.collectAsState()
                val cantidadProductosVenta by ventaViewModel.cantidadProductosVenta.collectAsState()

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
                            Pantalla.VENTAS -> VentasScreen(viewModel = ventaViewModel)
                            Pantalla.HISTORIAL_VENTAS -> HistorialVentasScreen(
                                viewModel = ventaViewModel,
                                onVentaClick = { venta ->
                                    selectedVentaId = venta.id
                                    pantallaActual = Pantalla.DETALLE_VENTA
                                }
                            )
                            Pantalla.DETALLE_VENTA -> DetalleVentaScreen(
                                viewModel = ventaViewModel,
                                ventaId = selectedVentaId,
                                onVolver = {
                                    selectedVentaId = null
                                    pantallaActual = Pantalla.HISTORIAL_VENTAS
                                },
                                onVentaEliminada = {
                                    selectedVentaId = null
                                    pantallaActual = Pantalla.HISTORIAL_VENTAS
                                }
                            )
                        }
                    }

                    NavigationBar {
                        NavigationBarItem(
                            selected = pantallaActual == Pantalla.PRODUCTOS,
                            onClick = {
                                selectedVentaId = null
                                pantallaActual = Pantalla.PRODUCTOS
                            },
                            label = { Text("Productos") },
                            icon = { Text("📦") }
                        )

                        NavigationBarItem(
                            selected = pantallaActual == Pantalla.PEDIDO,
                            onClick = {
                                selectedVentaId = null
                                pantallaActual = Pantalla.PEDIDO
                            },
                            label = { Text("Pedido") },
                            icon = { Text("🛒 $cantidadTotalPedido") }
                        )

                        NavigationBarItem(
                            selected = pantallaActual == Pantalla.VENTAS,
                            onClick = {
                                selectedVentaId = null
                                pantallaActual = Pantalla.VENTAS
                            },
                            label = { Text("Ventas") },
                            icon = { Text("💵 $cantidadProductosVenta") }
                        )

                        NavigationBarItem(
                            selected = pantallaActual == Pantalla.HISTORIAL_VENTAS ||
                                    pantallaActual == Pantalla.DETALLE_VENTA,
                            onClick = {
                                selectedVentaId = null
                                pantallaActual = Pantalla.HISTORIAL_VENTAS
                            },
                            label = { Text("Historial") },
                            icon = { Text("H") }
                        )
                    }
                }
            }
        }
    }
}