package com.example.elbosqueapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.elbosqueapp.data.local.ItemVentaEntity
import com.example.elbosqueapp.data.local.VentaEntity
import com.example.elbosqueapp.ui.components.Header
import com.example.elbosqueapp.ui.components.ResponsiveButtonPair
import com.example.elbosqueapp.ui.components.responsiveInfo
import com.example.elbosqueapp.ui.theme.ErrorBt
import com.example.elbosqueapp.ui.theme.FondoCrema
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DetalleVentaScreen(
    viewModel: VentaViewModel,
    ventaId: Long?,
    onVolver: () -> Unit,
    onVentaEliminada: () -> Unit,
    onMenuClick: (() -> Unit)? = null
) {
    val venta by viewModel.ventaDetalle.collectAsState()
    val itemsDetalle by viewModel.itemsDetalleVenta.collectAsState()
    val cargando by viewModel.cargandoDetalleVenta.collectAsState()
    var mostrarConfirmacionEliminar by remember { mutableStateOf(false) }

    LaunchedEffect(ventaId) {
        if (ventaId == null) {
            viewModel.limpiarDetalleVenta()
        } else {
            viewModel.cargarDetalleVenta(ventaId)
        }
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
                titulo = "Detalle venta",
                botonTexto = "Volver",
                onBotonClick = onVolver,
                onMenuClick = onMenuClick
            )

            when {
                ventaId == null -> Text("No hay una venta seleccionada")
                cargando -> Text("Cargando venta...")
                venta == null -> Text("Venta no encontrada")
                else -> DetalleVentaContenido(
                    venta = venta!!,
                    itemsDetalle = itemsDetalle,
                    onEliminarClick = { mostrarConfirmacionEliminar = true },
                    buttonMinHeight = responsive.alturaBoton,
                    spacing = responsive.espacio
                )
            }
        }
    }

    if (mostrarConfirmacionEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionEliminar = false },
            confirmButton = {
                ResponsiveButtonPair(
                    first = { modifier ->
                        Button(
                            onClick = { mostrarConfirmacionEliminar = false },
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
                                ventaId?.let { id ->
                                    mostrarConfirmacionEliminar = false
                                    viewModel.eliminarVenta(id, onVentaEliminada)
                                }
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
            title = { Text("Eliminar venta", maxLines = 2, softWrap = true) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Esta venta y sus productos se eliminaran definitivamente.")
                }
            }
        )
    }

}

@Composable
private fun ColumnScope.DetalleVentaContenido(
    venta: VentaEntity,
    itemsDetalle: List<ItemVentaEntity>,
    onEliminarClick: () -> Unit,
    buttonMinHeight: androidx.compose.ui.unit.Dp,
    spacing: androidx.compose.ui.unit.Dp
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = formatoFechaDetalle(venta.fecha),
                style = MaterialTheme.typography.titleMedium
            )
            Text("Pago: " + venta.tipoPago, softWrap = true)
            Text(
                text = "Total: " + formatoDineroDetalle(venta.total),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }

    Spacer(modifier = Modifier.height(spacing))

    Text(
        text = "Productos vendidos",
        style = MaterialTheme.typography.titleMedium
    )

    Spacer(modifier = Modifier.height(6.dp))

    if (itemsDetalle.isEmpty()) {
        Text("No hay productos registrados en esta venta")
        Spacer(modifier = Modifier.weight(1f))
    } else {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(itemsDetalle, key = { it.id }) { item ->
                ItemDetalleVentaCard(item = item)
            }
        }
    }

    Spacer(modifier = Modifier.height(spacing))

    Button(
        onClick = onEliminarClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = buttonMinHeight),
        colors = ButtonDefaults.buttonColors(containerColor = ErrorBt),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    ) {
        Text("Eliminar venta")
    }
}

@Composable
private fun ItemDetalleVentaCard(item: ItemVentaEntity) {
    val granel = esGranelDetalle(item.tipoVenta)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = item.producto,
                style = MaterialTheme.typography.titleMedium,
                softWrap = true
            )
            Text("Codigo: " + item.codigo)

            Spacer(modifier = Modifier.height(4.dp))

            if (granel) {
                Text("Precio kilo: " + formatoDineroDetalle(item.precioUnitario))
                Text("Kilos calculados: " + formatoKilosDetalle(item.cantidad) + " kg")
                Text("Monto pesado / subtotal: " + formatoDineroDetalle(item.subtotal))
            } else {
                Text("Cantidad: " + item.cantidad.toInt())
                Text("Precio unidad: " + formatoDineroDetalle(item.precioUnitario))
                Text("Subtotal: " + formatoDineroDetalle(item.subtotal))
            }
        }
    }
}

private fun esGranelDetalle(tipoVenta: String): Boolean {
    return tipoVenta.trim().equals("GRANEL", ignoreCase = true)
}

private fun formatoFechaDetalle(fecha: Long): String {
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")).format(Date(fecha))
}

private fun formatoDineroDetalle(valor: Double): String {
    val formato = NumberFormat.getNumberInstance(Locale("es", "CL"))
    return "$" + formato.format(valor.toLong())
}

private fun formatoKilosDetalle(valor: Double): String {
    return String.format(Locale("es", "CL"), "%.3f", valor)
}
