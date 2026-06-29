package com.example.elbosqueapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import com.example.elbosqueapp.ui.components.Header
import com.example.elbosqueapp.ui.theme.AmarilloAccent
import com.example.elbosqueapp.ui.theme.ErrorBt
import com.example.elbosqueapp.ui.theme.FondoCrema
import com.example.elbosqueapp.ui.theme.VerdeClaro
import com.example.elbosqueapp.ui.theme.VerdePrincipal

@Composable
fun PedidoScreen(
    viewModel: ProductoViewModel,
    onMenuClick: (() -> Unit)? = null
) {

    val pedido by viewModel.pedido.collectAsState()
    val total by viewModel.total.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoCrema)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Header(
            titulo = "Pedido actual",
            onMenuClick = onMenuClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Total: $${total.toInt()}",
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { mostrarConfirmacion = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorBt
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Text("Vaciar")
            }

            Button(
                onClick = {
                    clipboardManager.setText(
                        AnnotatedString(viewModel.generarTextoPedido())
                    )
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeClaro
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Text("Copiar")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (pedido.isEmpty()) {
            Text("No hay productos en el pedido")
        } else {
            LazyColumn {

                items(pedido) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = item.codigo,
                                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                                )
                                Text(item.producto)
                                Text("Stock: ${item.stock}")
                                Text("Compra: $${item.precioCompra.toInt()}")
                                Text("Venta: $${item.precioVenta.toInt()}")
                                Text("Cantidad: ${item.cantidad}")
                                Text("Subtotal: $${(item.precioVenta * item.cantidad).toInt()}")
                            }

                            Row (
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ){
                                Button(onClick = { viewModel.disminuirCantidad(item) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VerdePrincipal
                                    ),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp)) {
                                    Text("-")
                                }

                                Button(onClick = { viewModel.aumentarCantidad(item) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VerdePrincipal
                                    ),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp)) {
                                    Text("+")
                                }
                            }
                        }
                    }
                }
            }
        }
        if (mostrarConfirmacion) {
            AlertDialog(
                onDismissRequest = { mostrarConfirmacion = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.vaciarPedido()
                            mostrarConfirmacion = false
                        }
                    ) {
                        Text("Sí, vaciar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarConfirmacion = false }) {
                        Text("Cancelar")
                    }
                },
                title = {
                    Text("Confirmar")
                },
                text = {
                    Text("¿Seguro que quieres vaciar el pedido?")
                }
            )
        }

    }
}