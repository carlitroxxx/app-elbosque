package com.example.elbosqueapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.elbosqueapp.data.local.VentaEntity
import com.example.elbosqueapp.ui.components.Header
import com.example.elbosqueapp.ui.theme.FondoCrema
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistorialVentasScreen(
    viewModel: VentaViewModel,
    onVentaClick: (VentaEntity) -> Unit
) {
    val ventas by viewModel.historialVentas.collectAsState()
    val cargando by viewModel.cargandoHistorialVentas.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarHistorialVentas()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoCrema)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Header(titulo = "Historial de ventas")

        when {
            cargando -> Text("Cargando ventas...")
            ventas.isEmpty() -> Text("No hay ventas guardadas")
            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ventas, key = { it.id }) { venta ->
                    VentaHistorialCard(
                        venta = venta,
                        onClick = { onVentaClick(venta) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VentaHistorialCard(
    venta: VentaEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = formatoFechaHistorial(venta.fecha),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text("Pago: " + venta.tipoPago)
            Text(
                text = "Total: " + formatoDineroHistorial(venta.total),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

private fun formatoFechaHistorial(fecha: Long): String {
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")).format(Date(fecha))
}

private fun formatoDineroHistorial(valor: Double): String {
    val formato = NumberFormat.getNumberInstance(Locale("es", "CL"))
    return "$" + formato.format(valor.toLong())
}
