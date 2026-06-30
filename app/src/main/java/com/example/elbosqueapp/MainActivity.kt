package com.example.elbosqueapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.elbosqueapp.data.local.AppDatabase
import com.example.elbosqueapp.data.repository.ProductoRepository
import com.example.elbosqueapp.data.repository.VentaRepository
import com.example.elbosqueapp.ui.screens.DetalleVentaScreen
import com.example.elbosqueapp.ui.screens.HistorialVentasScreen
import com.example.elbosqueapp.ui.screens.ModuloApp
import com.example.elbosqueapp.ui.screens.Pantalla
import com.example.elbosqueapp.ui.screens.PedidoScreen
import com.example.elbosqueapp.ui.screens.ProductoViewModel
import com.example.elbosqueapp.ui.screens.ProductoViewModelFactory
import com.example.elbosqueapp.ui.screens.ProductosScreen
import com.example.elbosqueapp.ui.screens.VentaViewModel
import com.example.elbosqueapp.ui.screens.VentaViewModelFactory
import com.example.elbosqueapp.ui.screens.VentasScreen
import com.example.elbosqueapp.ui.theme.ElBosqueAppTheme
import com.example.elbosqueapp.ui.theme.VerdePrincipal
import kotlinx.coroutines.launch

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
                val context = LocalContext.current

                val excelLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri ->
                    uri?.let {
                        productoViewModel.importarExcel(context, it)
                    }
                }

                var moduloActual by remember { mutableStateOf(ModuloApp.VENTAS) }
                var pantallaActual by remember { mutableStateOf(Pantalla.VENTAS) }
                var selectedVentaId by remember { mutableStateOf<Long?>(null) }

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val coroutineScope = rememberCoroutineScope()

                val cantidadTotalPedido by productoViewModel.cantidadTotal.collectAsState()
                val cantidadProductosVenta by ventaViewModel.cantidadProductosVenta.collectAsState()

                val abrirMenu: () -> Unit = {
                    coroutineScope.launch {
                        drawerState.open()
                    }
                }

                fun seleccionarModulo(modulo: ModuloApp) {
                    moduloActual = modulo
                    selectedVentaId = null
                    pantallaActual = when (modulo) {
                        ModuloApp.INVENTARIO -> Pantalla.PRODUCTOS
                        ModuloApp.VENTAS -> Pantalla.VENTAS
                    }
                    coroutineScope.launch {
                        drawerState.close()
                    }
                }

                LaunchedEffect(Unit) {
                    productoViewModel.cargarPedidoGuardado()
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            drawerState.close()
                                        }
                                    }
                                ) {
                                    Text(
                                        text = "\u2630",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                Image(
                                    painter = painterResource(id = R.drawable.logo),
                                    contentDescription = "Logo",
                                    modifier = Modifier.size(48.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = "El Bosque App",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            NavigationDrawerItem(
                                icon = { Text("\uD83D\uDCB5") },
                                label = { Text("Ventas") },
                                selected = moduloActual == ModuloApp.VENTAS,
                                onClick = { seleccionarModulo(ModuloApp.VENTAS) },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            NavigationDrawerItem(
                                icon = { Text("\uD83D\uDCE6") },
                                label = { Text("Inventario") },
                                selected = moduloActual == ModuloApp.INVENTARIO,
                                onClick = { seleccionarModulo(ModuloApp.INVENTARIO) },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        drawerState.close()
                                    }
                                    excelLauncher.launch(
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VerdePrincipal,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Cargar Excel")
                            }
                        }
                    }
                ) {
                    Column {
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            when (pantallaActual) {
                                Pantalla.PRODUCTOS -> ProductosScreen(
                                    viewModel = productoViewModel,
                                    onMenuClick = abrirMenu
                                )

                                Pantalla.PEDIDO -> PedidoScreen(
                                    viewModel = productoViewModel,
                                    onMenuClick = abrirMenu
                                )

                                Pantalla.VENTAS -> VentasScreen(
                                    viewModel = ventaViewModel,
                                    onMenuClick = abrirMenu
                                )

                                Pantalla.HISTORIAL_VENTAS -> HistorialVentasScreen(
                                    viewModel = ventaViewModel,
                                    onVentaClick = { venta ->
                                        moduloActual = ModuloApp.VENTAS
                                        selectedVentaId = venta.id
                                        pantallaActual = Pantalla.DETALLE_VENTA
                                    },
                                    onMenuClick = abrirMenu
                                )

                                Pantalla.DETALLE_VENTA -> DetalleVentaScreen(
                                    viewModel = ventaViewModel,
                                    ventaId = selectedVentaId,
                                    onVolver = {
                                        moduloActual = ModuloApp.VENTAS
                                        selectedVentaId = null
                                        pantallaActual = Pantalla.HISTORIAL_VENTAS
                                    },
                                    onVentaEliminada = {
                                        moduloActual = ModuloApp.VENTAS
                                        selectedVentaId = null
                                        pantallaActual = Pantalla.HISTORIAL_VENTAS
                                    },
                                    onMenuClick = abrirMenu
                                )
                            }
                        }

                        NavigationBar {
                            when (moduloActual) {
                                ModuloApp.INVENTARIO -> {
                                    NavigationBarItem(
                                        selected = pantallaActual == Pantalla.PRODUCTOS,
                                        onClick = {
                                            selectedVentaId = null
                                            pantallaActual = Pantalla.PRODUCTOS
                                        },
                                        label = { Text("Productos") },
                                        icon = { Text("\uD83D\uDCE6") }
                                    )

                                    NavigationBarItem(
                                        selected = pantallaActual == Pantalla.PEDIDO,
                                        onClick = {
                                            selectedVentaId = null
                                            pantallaActual = Pantalla.PEDIDO
                                        },
                                        label = { Text("Pedido") },
                                        icon = { Text("\uD83D\uDED2 $cantidadTotalPedido") }
                                    )
                                }

                                ModuloApp.VENTAS -> {
                                    NavigationBarItem(
                                        selected = pantallaActual == Pantalla.VENTAS,
                                        onClick = {
                                            selectedVentaId = null
                                            pantallaActual = Pantalla.VENTAS
                                        },
                                        label = { Text("Ventas") },
                                        icon = { Text("\uD83D\uDCB5 $cantidadProductosVenta") }
                                    )

                                    NavigationBarItem(
                                        selected = pantallaActual == Pantalla.HISTORIAL_VENTAS ||
                                                pantallaActual == Pantalla.DETALLE_VENTA,
                                        onClick = {
                                            selectedVentaId = null
                                            pantallaActual = Pantalla.HISTORIAL_VENTAS
                                        },
                                        label = { Text("Historial") },
                                        icon = { Text("\uD83E\uDDFE") }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
