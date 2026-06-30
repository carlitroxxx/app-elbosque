package com.example.elbosqueapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.elbosqueapp.data.local.ProductoEntity
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import com.example.elbosqueapp.ui.components.Header
import com.example.elbosqueapp.ui.components.ResponsiveButtonPair
import com.example.elbosqueapp.ui.components.responsiveInfo
import com.example.elbosqueapp.ui.theme.FondoCrema
import com.example.elbosqueapp.ui.theme.VerdePrincipal

@Composable
fun ProductosScreen(
    viewModel: ProductoViewModel,
    onMenuClick: (() -> Unit)? = null
) {

    val productos by viewModel.productos.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarProductos()
    }
    var soloBajoStock by remember { mutableStateOf(false) }
    var textoBusqueda by remember { mutableStateOf("") }
    var productoSeleccionado by remember { mutableStateOf<ProductoEntity?>(null) }
    val productosFiltrados = productos
        .filter {
            it.descripcion.contains(textoBusqueda, ignoreCase = true) ||
                    it.codigo.contains(textoBusqueda, ignoreCase = true)
        }
        .filter {
            if (soloBajoStock) {
                it.existencia <= it.inventarioMinimo
            } else true
        }
        .sortedBy {
            it.existencia > it.inventarioMinimo
        }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val responsive = responsiveInfo(maxWidth)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FondoCrema)
                .statusBarsPadding()
                .padding(responsive.paddingPantalla)
        ) {
            Header(
                titulo = "Productos",
                onMenuClick = onMenuClick
            )

            Spacer(modifier = Modifier.height(2.dp))

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                label = { Text("Buscar producto") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(responsive.espacio))

            Button(
                onClick = { soloBajoStock = !soloBajoStock },
                modifier = Modifier.heightIn(min = responsive.alturaBoton),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdePrincipal
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (soloBajoStock) "Mostrar todos" else "Ver bajo stock",
                    maxLines = 2,
                    softWrap = true
                )
            }

            Spacer(modifier = Modifier.height(responsive.espacio))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(items = productosFiltrados) { producto ->
                    ProductoItem(
                        producto = producto,
                        onClick = { productoSeleccionado = producto }
                    )
                }
            }
            productoSeleccionado?.let { producto ->
                AlertDialog(
                    onDismissRequest = { productoSeleccionado = null },
                    confirmButton = {
                        ResponsiveButtonPair(
                            first = { modifier ->
                                Button(
                                    onClick = { productoSeleccionado = null },
                                    modifier = modifier,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    ),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                ) {
                                    Text("Cerrar", maxLines = 2, softWrap = true)
                                }
                            },
                            second = { modifier ->
                                Button(
                                    onClick = {
                                        viewModel.agregarAlPedido(producto)
                                        productoSeleccionado = null
                                    },
                                    modifier = modifier,
                                    colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                ) {
                                    Text("Agregar", maxLines = 2, softWrap = true)
                                }
                            }
                        )
                    },
                    title = {
                        Text(producto.descripcion)
                    },
                    text = {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            Text("C\u00f3digo: ${producto.codigo}")
                            Text("Costo: ${producto.costo}")
                            Text("Precio: ${producto.precioVenta}")
                            Text("Stock: ${producto.existencia}")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ProductoItem(
    producto: ProductoEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = producto.codigo,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                softWrap = true
            )
            Text(producto.descripcion, softWrap = true)
            Text("Stock: ${producto.existencia}")
            Text("Mínimo: ${producto.inventarioMinimo}")
            Text("Precio: $${producto.precioVenta.toInt()}")
        }
    }
}
