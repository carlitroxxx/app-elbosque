package com.example.elbosqueapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.elbosqueapp.R
import com.example.elbosqueapp.data.local.ProductoEntity
import com.example.elbosqueapp.data.model.ItemVenta
import com.example.elbosqueapp.ui.components.Header
import com.example.elbosqueapp.ui.components.ResponsiveButtonPair
import com.example.elbosqueapp.ui.components.responsiveInfo
import com.example.elbosqueapp.ui.theme.ErrorBt
import com.example.elbosqueapp.ui.theme.FondoCrema
import com.example.elbosqueapp.ui.theme.VerdePrincipal
import java.util.Locale

@Composable
fun VentasScreen(
    viewModel: VentaViewModel,
    onMenuClick: (() -> Unit)? = null
) {

    val productos by viewModel.productos.collectAsState()
    val ventaActual by viewModel.ventaActual.collectAsState()
    val totalVenta by viewModel.totalVenta.collectAsState()
    val guardandoVenta by viewModel.guardandoVenta.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var textoBusqueda by remember { mutableStateOf("") }
    var codigoProducto by remember { mutableStateOf("") }
    val codigoFocusRequester = remember { FocusRequester() }
    var enfocarCodigoPendiente by remember { mutableStateOf(false) }
    var productoSeleccionado by remember { mutableStateOf<ProductoEntity?>(null) }
    var itemSeleccionado by remember { mutableStateOf<ItemVenta?>(null) }
    var productoPendienteEliminar by remember { mutableStateOf<ItemVenta?>(null) }
    var mostrarConfirmacionVaciar by remember { mutableStateOf(false) }
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

    LaunchedEffect(guardandoVenta, ventaActual, mostrarDialogoPago) {
        if (mostrarDialogoPago && !guardandoVenta && ventaActual.isEmpty()) {
            mostrarDialogoPago = false
        }
    }

    LaunchedEffect(mensaje) {
        mensaje?.let { texto ->
            snackbarHostState.showSnackbar(texto)
            viewModel.limpiarMensaje()
        }
    }

    val textoBusquedaNormalizado = textoBusqueda.trim()
    val productosFiltrados = productos
        .filter {
            textoBusquedaNormalizado.isEmpty() ||
                    it.descripcion.contains(textoBusquedaNormalizado, ignoreCase = true) ||
                    it.codigo.contains(textoBusquedaNormalizado, ignoreCase = true)
        }
        .sortedWith(
            compareBy<ProductoEntity>(
                { prioridadBusquedaProducto(it, textoBusquedaNormalizado) },
                { it.descripcion.lowercase(Locale.ROOT) }
            )
        )

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

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (mostrandoProductos) {
                PantallaAgregarProductoVenta(
                    productosFiltrados = productosFiltrados,
                    textoBusqueda = textoBusqueda,
                    onTextoBusquedaChange = { textoBusqueda = it },
                    onVolver = {
                        mostrandoProductos = false
                        textoBusqueda = ""
                    },
                    onMenuClick = onMenuClick,
                    onProductoClick = { producto ->
                        if (esGranel(producto.tipoVenta)) {
                            productoSeleccionado = producto
                        } else {
                            viewModel.agregarProductoUnidad(producto, 1)
                            mostrandoProductos = false
                            textoBusqueda = ""
                            codigoProducto = ""
                            enfocarCodigoPendiente = true
                        }
                    }
                )
            } else {
                PantallaVentaActual(
                    ventaActual = ventaActual,
                    totalVenta = totalVenta,
                    guardandoVenta = guardandoVenta,
                    codigoProducto = codigoProducto,
                    codigoFocusRequester = codigoFocusRequester,
                    onCodigoProductoChange = { codigoProducto = it.uppercase(Locale.ROOT) },
                    onCodigoProductoDone = { buscarProductoPorCodigo() },
                    onBuscarProducto = {
                        mostrandoProductos = true
                    },
                    onMenuClick = onMenuClick,
                    onVaciarVenta = {
                        if (ventaActual.isNotEmpty()) {
                            mostrarConfirmacionVaciar = true
                        }
                    },
                    onFinalizarVenta = {
                        if (!guardandoVenta) {
                            if (ventaActual.isEmpty()) {
                                viewModel.finalizarVenta()
                            } else {
                                mostrarDialogoPago = true
                            }
                        }
                    },
                    onAumentarUnidad = { item ->
                        viewModel.aumentarUnidad(item.codigo)
                    },
                    onDisminuirUnidad = { item ->
                        if (!esGranel(item.tipoVenta) && item.cantidad <= 1.0) {
                            productoPendienteEliminar = item
                        } else {
                            viewModel.disminuirUnidad(item.codigo)
                        }
                    },
                    onEditarGranel = { item ->
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

            productoPendienteEliminar?.let { item ->
                AlertDialog(
                    onDismissRequest = { productoPendienteEliminar = null },
                    confirmButton = {
                        ResponsiveButtonPair(
                            first = { modifier ->
                                Button(
                                    onClick = { productoPendienteEliminar = null },
                                    modifier = modifier,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    ),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                ) {
                                    Text("Cancelar", maxLines = 2, softWrap = true)
                                }
                            },
                            second = { modifier ->
                                Button(
                                    onClick = {
                                        viewModel.eliminarItem(item.codigo)
                                        productoPendienteEliminar = null
                                    },
                                    modifier = modifier,
                                    colors = ButtonDefaults.buttonColors(containerColor = ErrorBt),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                ) {
                                    Text("Eliminar", maxLines = 2, softWrap = true)
                                }
                            }
                        )
                    },
                    title = { Text("Eliminar producto") },
                    text = {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text("\u00bfEliminar este producto de la venta?")
                        }
                    }
                )
            }

            if (mostrarConfirmacionVaciar) {
                AlertDialog(
                    onDismissRequest = { mostrarConfirmacionVaciar = false },
                    confirmButton = {
                        ResponsiveButtonPair(
                            first = { modifier ->
                                Button(
                                    onClick = { mostrarConfirmacionVaciar = false },
                                    modifier = modifier,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    ),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                ) {
                                    Text("Cancelar", maxLines = 2, softWrap = true)
                                }
                            },
                            second = { modifier ->
                                Button(
                                    onClick = {
                                        viewModel.vaciarVenta()
                                        mostrarConfirmacionVaciar = false
                                    },
                                    modifier = modifier,
                                    colors = ButtonDefaults.buttonColors(containerColor = ErrorBt),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                ) {
                                    Text("Vaciar", maxLines = 2, softWrap = true)
                                }
                            }
                        )
                    },
                    title = { Text("Vaciar venta") },
                    text = {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text("\u00bfSeguro que quieres quitar todos los productos agregados?")
                        }
                    }
                )
            }

            if (mostrarDialogoPago) {
                DialogoFinalizarVenta(
                    totalVenta = totalVenta,
                    guardandoVenta = guardandoVenta,
                    onDismiss = { if (!guardandoVenta) mostrarDialogoPago = false },
                    onConfirmar = { tipoPago ->
                        if (!guardandoVenta) {
                            viewModel.seleccionarTipoPago(tipoPago)
                            viewModel.finalizarVenta()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PantallaVentaActual(
    ventaActual: List<ItemVenta>,
    totalVenta: Double,
    guardandoVenta: Boolean,
    codigoProducto: String,
    codigoFocusRequester: FocusRequester,
    onCodigoProductoChange: (String) -> Unit,
    onCodigoProductoDone: () -> Unit,
    onBuscarProducto: () -> Unit,
    onMenuClick: (() -> Unit)? = null,
    onVaciarVenta: () -> Unit,
    onFinalizarVenta: () -> Unit,
    onAumentarUnidad: (ItemVenta) -> Unit,
    onDisminuirUnidad: (ItemVenta) -> Unit,
    onEditarGranel: (ItemVenta) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val responsive = responsiveInfo(maxWidth)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FondoCrema)
                .statusBarsPadding()
                .padding(horizontal = responsive.paddingPantalla, vertical = 8.dp)
        ) {
            HeaderVentas(
                onMenuClick = onMenuClick,
                onBuscarClick = onBuscarProducto
            )

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = codigoProducto,
                onValueChange = onCodigoProductoChange,
                label = { Text("C\u00f3digo del producto") },
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

            Spacer(modifier = Modifier.height(responsive.espacio))

            if (responsive.usarLayoutVertical) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Venta actual (${ventaActual.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Total: ${formatoDinero(totalVenta)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Venta actual (${ventaActual.size})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Total: ${formatoDinero(totalVenta)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (ventaActual.isEmpty()) {
                Text("No hay productos agregados")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(ventaActual, key = { it.codigo }) { item ->
                        ItemVentaCard(
                            item = item,
                            onAumentarUnidad = { onAumentarUnidad(item) },
                            onDisminuirUnidad = { onDisminuirUnidad(item) },
                            onEditarGranel = { onEditarGranel(item) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(responsive.espacio))

            ResponsiveButtonPair(
                first = { modifier ->
                    Button(
                        onClick = onVaciarVenta,
                        modifier = modifier,
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorBt),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text("Vaciar", maxLines = 2, softWrap = true)
                    }
                },
                second = { modifier ->
                    Button(
                        onClick = onFinalizarVenta,
                        enabled = !guardandoVenta,
                        modifier = modifier,
                        colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (guardandoVenta) "Guardando..." else "Finalizar venta",
                            maxLines = 2,
                            softWrap = true
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun HeaderVentas(
    onMenuClick: (() -> Unit)?,
    onBuscarClick: () -> Unit
) {
    val usaFuenteGrande = LocalDensity.current.fontScale >= 1.2f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (usaFuenteGrande) 6.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onMenuClick != null) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            ) {
                Text(
                    text = "\u2630",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        Text(
            text = "Ventas",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.weight(1f),
            maxLines = if (usaFuenteGrande) 2 else 1,
            softWrap = true
        )

        IconButton(
            onClick = onBuscarClick,
            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
        ) {
            IconoBuscar(modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.width(8.dp))

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(if (usaFuenteGrande) 36.dp else 40.dp)
        )
    }
}

@Composable
private fun IconoBuscar(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier) {
        val radio = size.minDimension * 0.32f
        val centro = Offset(size.width * 0.43f, size.height * 0.43f)
        val grosor = 2.2.dp.toPx()

        drawCircle(
            color = color,
            radius = radio,
            center = centro,
            style = Stroke(width = grosor)
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.65f, size.height * 0.65f),
            end = Offset(size.width * 0.88f, size.height * 0.88f),
            strokeWidth = grosor,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun PantallaAgregarProductoVenta(
    productosFiltrados: List<ProductoEntity>,
    textoBusqueda: String,
    onTextoBusquedaChange: (String) -> Unit,
    onVolver: () -> Unit,
    onMenuClick: (() -> Unit)? = null,
    onProductoClick: (ProductoEntity) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val responsive = responsiveInfo(maxWidth)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FondoCrema)
                .statusBarsPadding()
                .padding(horizontal = responsive.paddingPantalla, vertical = 8.dp)
        ) {
            Header(
                titulo = "Agregar producto",
                onMenuClick = onMenuClick
            )

            Button(
                onClick = onVolver,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = responsive.alturaBoton),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Text("Volver a la venta", maxLines = 2, softWrap = true)
            }

            Spacer(modifier = Modifier.height(responsive.espacio))

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = onTextoBusquedaChange,
                label = { Text("Buscar producto") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(responsive.espacio))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
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
}
@Composable
fun EtiquetaTipoVenta(granel: Boolean) {
    val colorFondo = if (granel) MaterialTheme.colorScheme.secondary else VerdePrincipal
    val colorTexto = if (granel) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary

    Text(
        text = if (granel) "GRANEL" else "UNIDAD",
        style = MaterialTheme.typography.labelSmall,
        color = colorTexto,
        modifier = Modifier
            .background(
                color = colorFondo,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = producto.descripcion,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                EtiquetaTipoVenta(granel = granel)
            }

            Text("Código: ${producto.codigo}")

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
    onAumentarUnidad: () -> Unit,
    onDisminuirUnidad: () -> Unit,
    onEditarGranel: () -> Unit
) {
    val granel = esGranel(item.tipoVenta)

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val responsive = responsiveInfo(maxWidth)

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.producto,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                        softWrap = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    EtiquetaTipoVenta(granel = granel)
                }

                Spacer(modifier = Modifier.height(4.dp))

                val datos: @Composable () -> Unit = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("C\u00f3digo: ${item.codigo}", style = MaterialTheme.typography.bodySmall)

                        if (granel) {
                            Text(
                                "Precio kilo: ${formatoDinero(item.precioUnitario)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Kilos calculados: ${formatoKilos(item.cantidad)} kg",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Monto pesado: ${formatoDinero(item.subtotal)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Text(
                                "Precio unidad: ${formatoDinero(item.precioUnitario)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Cantidad: ${item.cantidad.toInt()}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Subtotal: ${formatoDinero(item.subtotal)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                if (responsive.usarLayoutVertical) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        datos()

                        if (granel) {
                            Button(
                                onClick = onEditarGranel,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = responsive.alturaBoton),
                                colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                            ) {
                                Text("Editar monto", maxLines = 2, softWrap = true)
                            }
                        } else {
                            ResponsiveButtonPair(
                                first = { modifier ->
                                    Button(
                                        onClick = onAumentarUnidad,
                                        modifier = modifier,
                                        contentPadding = PaddingValues(0.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                    ) {
                                        Text("+", style = MaterialTheme.typography.titleMedium)
                                    }
                                },
                                second = { modifier ->
                                    Button(
                                        onClick = onDisminuirUnidad,
                                        modifier = modifier,
                                        contentPadding = PaddingValues(0.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ErrorBt),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                    ) {
                                        Text("-", style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            datos()
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        if (granel) {
                            Button(
                                onClick = onEditarGranel,
                                modifier = Modifier
                                    .widthIn(min = 124.dp)
                                    .heightIn(min = responsive.alturaBotonCompacto),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                            ) {
                                Text("Editar monto", maxLines = 2, softWrap = true)
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = onAumentarUnidad,
                                    modifier = Modifier
                                        .widthIn(min = 56.dp)
                                        .heightIn(min = responsive.alturaBotonCompacto),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                ) {
                                    Text("+", style = MaterialTheme.typography.titleMedium)
                                }

                                Button(
                                    onClick = onDisminuirUnidad,
                                    modifier = Modifier
                                        .widthIn(min = 56.dp)
                                        .heightIn(min = responsive.alturaBotonCompacto),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ErrorBt),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                ) {
                                    Text("-", style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoFinalizarVenta(
    totalVenta: Double,
    guardandoVenta: Boolean,
    onDismiss: () -> Unit,
    onConfirmar: (String) -> Unit
) {
    var tipoPagoSeleccionado by remember { mutableStateOf("Efectivo") }

    AlertDialog(
        onDismissRequest = { if (!guardandoVenta) onDismiss() },
        confirmButton = {
            ResponsiveButtonPair(
                secondWeight = 1.35f,
                first = { modifier ->
                    Button(
                        onClick = onDismiss,
                        enabled = !guardandoVenta,
                        modifier = modifier,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancelar", maxLines = 1, softWrap = false)
                    }
                },
                second = { modifier ->
                    Button(
                        onClick = {
                            if (!guardandoVenta) {
                                onConfirmar(tipoPagoSeleccionado)
                            }
                        },
                        enabled = !guardandoVenta,
                        modifier = modifier,
                        colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (guardandoVenta) "Guardando..." else "Guardar venta",
                            maxLines = 1,
                            softWrap = false,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            )
        },
        title = {
            Text("Finalizar venta")
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text("Total: ${formatoDinero(totalVenta)}")

                Spacer(modifier = Modifier.height(12.dp))

                Text("Selecciona el tipo de pago:")

                Spacer(modifier = Modifier.height(8.dp))

                listOf("Efectivo", "Transferencia", "Tarjeta").forEach { pago ->
                    val seleccionado = tipoPagoSeleccionado == pago

                    Button(
                        onClick = { tipoPagoSeleccionado = pago },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (seleccionado)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (seleccionado)
                                MaterialTheme.colorScheme.onSecondaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (seleccionado) "\u2713 $pago" else pago,
                            maxLines = 1,
                            softWrap = false
                        )
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
            ResponsiveButtonPair(
                first = { modifier ->
                    Button(
                        onClick = onDismiss,
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
                            if (granel) {
                                onAgregarGranel(parseMonto(texto))
                            } else {
                                onAgregarUnidad(parseEntero(texto))
                            }
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
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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
            ResponsiveButtonPair(
                first = { modifier ->
                    Button(
                        onClick = onEliminar,
                        modifier = modifier,
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorBt),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text("Eliminar", maxLines = 2, softWrap = true)
                    }
                },
                second = { modifier ->
                    Button(
                        onClick = {
                            if (granel) {
                                onGuardarGranel(parseMonto(texto))
                            } else {
                                onGuardarUnidad(parseEntero(texto))
                            }
                        },
                        modifier = modifier,
                        colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text("Guardar", maxLines = 2, softWrap = true)
                    }
                }
            )
        },
        title = {
            Text("Editar producto")
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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

private fun prioridadBusquedaProducto(producto: ProductoEntity, textoBusqueda: String): Int {
    val texto = textoBusqueda.trim()
    if (texto.isEmpty()) {
        return 4
    }

    val codigo = producto.codigo.trim()
    return when {
        codigo.equals(texto, ignoreCase = true) -> 0
        codigo.startsWith(texto, ignoreCase = true) -> 1
        codigo.contains(texto, ignoreCase = true) -> 2
        producto.descripcion.contains(texto, ignoreCase = true) -> 3
        else -> 4
    }
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
