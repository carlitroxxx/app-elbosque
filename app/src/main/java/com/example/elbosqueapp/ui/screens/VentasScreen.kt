package com.example.elbosqueapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.elbosqueapp.data.local.ProductoEntity
import com.example.elbosqueapp.data.model.ItemVenta
import com.example.elbosqueapp.ui.components.Header
import com.example.elbosqueapp.ui.theme.ErrorBt
import com.example.elbosqueapp.ui.theme.FondoCrema
import com.example.elbosqueapp.ui.theme.VerdePrincipal
import java.util.Locale

@Composable
fun VentasScreen(viewModel: VentaViewModel) {

    val productos by viewModel.productos.collectAsState()
    val ventaActual by viewModel.ventaActual.collectAsState()
    val totalVenta by viewModel.totalVenta.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    var textoBusqueda by remember { mutableStateOf("") }
    var codigoProducto by remember { mutableStateOf("") }
    val codigoFocusRequester = remember { FocusRequester() }
    var enfocarCodigoPendiente by remember { mutableStateOf(false) }
    var productoSeleccionado by remember { mutableStateOf<ProductoEntity?>(null) }
    var itemSeleccionado by remember { mutableStateOf<ItemVenta?>(null) }
    var mostrandoProductos by remember { mutableStateOf(false) }
    var mostrarDialogoPago by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.cargarProductos()
    }

    LaunchedEffect(enfocarCodigoPendiente, mostrandoProductos, productoSeleccionado) {
        if (enfocarCodigoPendiente && !mostrandoProductos && productoSeleccionado == null) {
            codigoFocusRequester.requestFocus()
            enfocarCodigoPendiente = false
        }
    }

    val productosFiltrados = productos
        .filter {
            it.descripcion.contains(textoBusqueda, ignoreCase = true) ||
                    it.codigo.contains(textoBusqueda, ignoreCase = true)
        }
        .sortedBy { it.descripcion }

    fun buscarProductoPorCodigo() {
        val codigoIngresado = codigoProducto.trim()

        if (codigoIngresado.isEmpty()) {
            return
        }

        val producto = productos.firstOrNull { producto ->
            producto.codigo.trim() == codigoIngresado
        }

        if (producto == null) {
            viewModel.mostrarMensaje("Producto no encontrado")
            return
        }

        if (esGranel(producto.tipoVenta)) {
            productoSeleccionado = producto
        } else {
            viewModel.agregarProductoUnidad(producto, 1)
            codigoProducto = ""
            enfocarCodigoPendiente = true
        }
    }

    if (mostrandoProductos) {
        PantallaAgregarProductoVenta(
            productosFiltrados = productosFiltrados,
            textoBusqueda = textoBusqueda,
            onTextoBusquedaChange = { textoBusqueda = it },
            onVolver = {
                mostrandoProductos = false
                textoBusqueda = ""
            },
            onProductoClick = { producto ->
                productoSeleccionado = producto
            }
        )
    } else {
        PantallaVentaActual(
            ventaActual = ventaActual,
            totalVenta = totalVenta,
            codigoProducto = codigoProducto,
            codigoFocusRequester = codigoFocusRequester,
            onCodigoProductoChange = { codigoProducto = it },
            onCodigoProductoDone = { buscarProductoPorCodigo() },
            onBuscarProducto = {
                mostrandoProductos = true
            },
            onVaciarVenta = {
                viewModel.vaciarVenta()
            },
            onFinalizarVenta = {
                if (ventaActual.isEmpty()) {
                    viewModel.finalizarVenta()
                } else {
                    mostrarDialogoPago = true
                }
            },
            onItemClick = { item ->
                itemSeleccionado = item
            }
        )
    }

    productoSeleccionado?.let { producto ->
        DialogoAgregarProductoVenta(
            producto = producto,
            onDismiss = {
                productoSeleccionado = null
                if (!mostrandoProductos) {
                    enfocarCodigoPendiente = true
                }
            },
            onAgregarUnidad = { cantidad ->
                viewModel.agregarProductoUnidad(producto, cantidad)
                productoSeleccionado = null
                mostrandoProductos = false
                textoBusqueda = ""
                codigoProducto = ""
                enfocarCodigoPendiente = true
            },
            onAgregarGranel = { monto ->
                viewModel.agregarProductoGranel(producto, monto)
                productoSeleccionado = null
                mostrandoProductos = false
                textoBusqueda = ""
                codigoProducto = ""
                enfocarCodigoPendiente = true
            }
        )
    }

    itemSeleccionado?.let { item ->
        DialogoEditarItemVenta(
            item = item,
            onDismiss = { itemSeleccionado = null },
            onGuardarUnidad = { cantidad ->
                viewModel.actualizarItemUnidad(item.codigo, cantidad)
                itemSeleccionado = null
            },
            onGuardarGranel = { monto ->
                viewModel.actualizarItemGranel(item.codigo, monto)
                itemSeleccionado = null
            },
            onEliminar = {
                viewModel.eliminarItem(item.codigo)
                itemSeleccionado = null
            }
        )
    }

    if (mostrarDialogoPago) {
        DialogoFinalizarVenta(
            totalVenta = totalVenta,
            onDismiss = { mostrarDialogoPago = false },
            onConfirmar = { tipoPago ->
                viewModel.seleccionarTipoPago(tipoPago)
                viewModel.finalizarVenta()
                mostrarDialogoPago = false
            }
        )
    }

    mensaje?.let { texto ->
        AlertDialog(
            onDismissRequest = { viewModel.limpiarMensaje() },
            confirmButton = {
                TextButton(onClick = { viewModel.limpiarMensaje() }) {
                    Text("Aceptar")
                }
            },
            title = { Text("Mensaje") },
            text = { Text(texto) }
        )
    }
}

@Composable
fun PantallaVentaActual(
    ventaActual: List<ItemVenta>,
    totalVenta: Double,
    codigoProducto: String,
    codigoFocusRequester: FocusRequester,
    onCodigoProductoChange: (String) -> Unit,
    onCodigoProductoDone: () -> Unit,
    onBuscarProducto: () -> Unit,
    onVaciarVenta: () -> Unit,
    onFinalizarVenta: () -> Unit,
    onItemClick: (ItemVenta) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoCrema)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Header(titulo = "Ventas")

        Text(
            text = "Total venta: ${formatoDinero(totalVenta)}",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = codigoProducto,
            onValueChange = onCodigoProductoChange,
            label = { Text("Código del producto") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(codigoFocusRequester)
                .onPreviewKeyEvent { event ->
                    if (event.key == Key.Enter && event.type == KeyEventType.KeyDown) {
                        onCodigoProductoDone()
                        true
                    } else {
                        false
                    }
                },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onCodigoProductoDone() }
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onBuscarProducto,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
        ) {
            Text("Buscar producto")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Venta actual",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (ventaActual.isEmpty()) {
            Text("No hay productos agregados")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(ventaActual) { item ->
                    ItemVentaCard(
                        item = item,
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onVaciarVenta,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorBt),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Text("Vaciar")
            }

            Button(
                onClick = onFinalizarVenta,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Text("Finalizar venta")
            }
        }
    }
}

@Composable
fun PantallaAgregarProductoVenta(
    productosFiltrados: List<ProductoEntity>,
    textoBusqueda: String,
    onTextoBusquedaChange: (String) -> Unit,
    onVolver: () -> Unit,
    onProductoClick: (ProductoEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoCrema)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Header(titulo = "Agregar producto")

        Button(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
        ) {
            Text("Volver a la venta")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = onTextoBusquedaChange,
            label = { Text("Buscar producto") },
            leadingIcon = { Text("🔍") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(productosFiltrados) { producto ->
                ProductoVentaCard(
                    producto = producto,
                    onClick = { onProductoClick(producto) }
                )
            }
        }
    }
}

@Composable
fun ProductoVentaCard(
    producto: ProductoEntity,
    onClick: () -> Unit
) {
    val granel = esGranel(producto.tipoVenta)

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
                text = producto.descripcion,
                style = MaterialTheme.typography.titleMedium
            )

            Text("Código: ${producto.codigo}")

            Text(
                text = if (granel) "Tipo: GRANEL" else "Tipo: UNIDAD"
            )

            Text(
                text = if (granel)
                    "Precio kilo: ${formatoDinero(producto.precioVenta)}"
                else
                    "Precio unidad: ${formatoDinero(producto.precioVenta)}",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ItemVentaCard(
    item: ItemVenta,
    onClick: () -> Unit
) {
    val granel = esGranel(item.tipoVenta)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = item.producto,
                style = MaterialTheme.typography.titleMedium
            )

            Text("Código: ${item.codigo}")

            if (granel) {
                Text("Precio kilo: ${formatoDinero(item.precioUnitario)}")
                Text("Kilos calculados: ${formatoKilos(item.cantidad)} kg")
                Text("Monto pesado: ${formatoDinero(item.subtotal)}")
            } else {
                Text("Precio unidad: ${formatoDinero(item.precioUnitario)}")
                Text("Cantidad: ${item.cantidad.toInt()}")
                Text("Subtotal: ${formatoDinero(item.subtotal)}")
            }
        }
    }
}

@Composable
fun DialogoFinalizarVenta(
    totalVenta: Double,
    onDismiss: () -> Unit,
    onConfirmar: (String) -> Unit
) {
    var tipoPagoSeleccionado by remember { mutableStateOf("Efectivo") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmar(tipoPagoSeleccionado)
                }
            ) {
                Text("Guardar venta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = {
            Text("Finalizar venta")
        },
        text = {
            Column {
                Text("Total: ${formatoDinero(totalVenta)}")

                Spacer(modifier = Modifier.height(12.dp))

                Text("Selecciona el tipo de pago:")

                Spacer(modifier = Modifier.height(8.dp))

                listOf("Efectivo", "Transferencia", "Tarjeta").forEach { pago ->
                    Button(
                        onClick = { tipoPagoSeleccionado = pago },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (tipoPagoSeleccionado == pago)
                                VerdePrincipal
                            else
                                MaterialTheme.colorScheme.secondary
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text(pago)
                    }
                }
            }
        }
    )
}

@Composable
fun DialogoAgregarProductoVenta(
    producto: ProductoEntity,
    onDismiss: () -> Unit,
    onAgregarUnidad: (Int) -> Unit,
    onAgregarGranel: (Double) -> Unit
) {
    val granel = esGranel(producto.tipoVenta)
    var texto by remember(producto.codigo) { mutableStateOf("") }

    val monto = parseMonto(texto)
    val kilos = if (producto.precioVenta > 0) monto / producto.precioVenta else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    if (granel) {
                        onAgregarGranel(parseMonto(texto))
                    } else {
                        onAgregarUnidad(parseEntero(texto))
                    }
                }
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        title = {
            Text(producto.descripcion)
        },
        text = {
            Column {
                Text("Código: ${producto.codigo}")

                Spacer(modifier = Modifier.height(6.dp))

                if (granel) {
                    Text("Precio kilo: ${formatoDinero(producto.precioVenta)}")

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = texto,
                        onValueChange = { texto = soloNumeros(it) },
                        label = { Text("Monto marcado en la pesa") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )

                    if (monto > 0 && producto.precioVenta > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Kilos calculados: ${formatoKilos(kilos)} kg")
                        Text("Subtotal: ${formatoDinero(monto)}")
                    }
                } else {
                    Text("Precio unidad: ${formatoDinero(producto.precioVenta)}")

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = texto,
                        onValueChange = { texto = soloNumeros(it) },
                        label = { Text("Cantidad") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                }
            }
        }
    )
}

@Composable
fun DialogoEditarItemVenta(
    item: ItemVenta,
    onDismiss: () -> Unit,
    onGuardarUnidad: (Int) -> Unit,
    onGuardarGranel: (Double) -> Unit,
    onEliminar: () -> Unit
) {
    val granel = esGranel(item.tipoVenta)

    var texto by remember(item.codigo) {
        mutableStateOf(
            if (granel) item.subtotal.toInt().toString()
            else item.cantidad.toInt().toString()
        )
    }

    val monto = parseMonto(texto)
    val kilos = if (item.precioUnitario > 0) monto / item.precioUnitario else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    if (granel) {
                        onGuardarGranel(parseMonto(texto))
                    } else {
                        onGuardarUnidad(parseEntero(texto))
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onEliminar) {
                    Text("Eliminar")
                }

                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        },
        title = {
            Text("Editar producto")
        },
        text = {
            Column {
                Text(item.producto)
                Text("Código: ${item.codigo}")

                Spacer(modifier = Modifier.height(8.dp))

                if (granel) {
                    Text("Precio kilo: ${formatoDinero(item.precioUnitario)}")

                    OutlinedTextField(
                        value = texto,
                        onValueChange = { texto = soloNumeros(it) },
                        label = { Text("Nuevo monto pesado") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )

                    if (monto > 0 && item.precioUnitario > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Kilos calculados: ${formatoKilos(kilos)} kg")
                        Text("Subtotal: ${formatoDinero(monto)}")
                    }
                } else {
                    Text("Precio unidad: ${formatoDinero(item.precioUnitario)}")

                    OutlinedTextField(
                        value = texto,
                        onValueChange = { texto = soloNumeros(it) },
                        label = { Text("Nueva cantidad") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                }
            }
        }
    )
}

private fun esGranel(tipoVenta: String): Boolean {
    return tipoVenta.trim().equals("GRANEL", ignoreCase = true)
}

private fun soloNumeros(texto: String): String {
    return texto.filter { it.isDigit() }
}

private fun formatoDinero(valor: Double): String {
    return "$${valor.toInt()}"
}

private fun formatoKilos(valor: Double): String {
    return String.format(Locale("es", "CL"), "%.3f", valor)
}

private fun parseMonto(texto: String): Double {
    return texto
        .replace("$", "")
        .replace(".", "")
        .replace(",", ".")
        .replace(" ", "")
        .trim()
        .toDoubleOrNull() ?: 0.0
}

private fun parseEntero(texto: String): Int {
    return texto
        .replace(".", "")
        .replace(",", "")
        .replace(" ", "")
        .trim()
        .toIntOrNull() ?: 0
}
