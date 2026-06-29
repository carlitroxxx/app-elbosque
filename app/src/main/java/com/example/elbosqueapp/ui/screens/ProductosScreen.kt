package com.example.elbosqueapp.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.elbosqueapp.data.local.ProductoEntity
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ButtonDefaults
import com.example.elbosqueapp.ui.components.Header
import com.example.elbosqueapp.ui.theme.FondoCrema
import com.example.elbosqueapp.ui.theme.VerdePrincipal

@Composable
fun ProductosScreen(
    viewModel: ProductoViewModel,
    onMenuClick: (() -> Unit)? = null
) {

    val context = LocalContext.current
    val productos by viewModel.productos.collectAsState()

    val excelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.importarExcel(context, it)
        }
    }

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoCrema)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Header(
            titulo = "Productos",
            botonTexto = "Cargar Excel",
            onBotonClick = {
                excelLauncher.launch(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
            },
            onMenuClick = onMenuClick
        )

        Spacer(modifier = Modifier.height(2.dp))



        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = { textoBusqueda = it },
            label = { Text("Buscar producto") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Text("🔍")
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(2.dp))

        Button(
            onClick = { soloBajoStock = !soloBajoStock },
            colors = ButtonDefaults.buttonColors(
                containerColor = VerdePrincipal
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
        ) {
            Text(
                if (soloBajoStock) "Mostrar todos"
                else "Ver bajo stock"
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn {
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
                    TextButton(
                        onClick = {
                            viewModel.agregarAlPedido(producto)
                            productoSeleccionado = null
                        }
                    ) {
                        Text("Agregar al pedido")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { productoSeleccionado = null }) {
                        Text("Cerrar")
                    }
                },
                title = {
                    Text(producto.descripcion)
                },
                text = {
                    Column {
                        Text("Código: ${producto.codigo}")
                        Text("Costo: ${producto.costo}")
                        Text("Precio: ${producto.precioVenta}")
                        Text("Stock: ${producto.existencia}")
                    }
                }
            )
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
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = producto.codigo,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = producto.descripcion,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Compra: $${producto.costo.toInt()}",
                color = androidx.compose.material3.MaterialTheme.colorScheme.secondary
            )

            Text(
                text = "Venta: $${producto.precioVenta.toInt()}",
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = "Stock: ${producto.existencia}")

            if (producto.existencia <= producto.inventarioMinimo) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "⚠ Bajo stock",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error
                )
            }
        }
    }
}