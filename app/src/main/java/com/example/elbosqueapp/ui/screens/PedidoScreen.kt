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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import com.example.elbosqueapp.ui.components.Header
import com.example.elbosqueapp.ui.components.ResponsiveButtonPair
import com.example.elbosqueapp.ui.components.responsiveInfo
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
                titulo = "Pedido actual",
                onMenuClick = onMenuClick
            )

            Spacer(modifier = Modifier.height(responsive.espacio))

            Text(
                text = "Total: $${total.toInt()}",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                softWrap = true
            )

            Spacer(modifier = Modifier.height(responsive.espacio))

            ResponsiveButtonPair(
                first = { modifier ->
                    Button(
                        onClick = { mostrarConfirmacion = true },
                        modifier = modifier,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ErrorBt
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text("Vaciar", maxLines = 2, softWrap = true)
                    }
                },
                second = { modifier ->
                    Button(
                        onClick = {
                            clipboardManager.setText(
                                AnnotatedString(viewModel.generarTextoPedido())
                            )
                        },
                        modifier = modifier,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerdeClaro
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text("Copiar", maxLines = 2, softWrap = true)
                    }
                }
            )

            Spacer(modifier = Modifier.height(responsive.espacio))

            if (pedido.isEmpty()) {
                Text("No hay productos en el pedido")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pedido) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            val datos: @Composable () -> Unit = {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = item.codigo,
                                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                        softWrap = true
                                    )
                                    Text(item.producto, softWrap = true)
                                    Text("Stock: ${item.stock}")
                                    Text("Compra: $${item.precioCompra.toInt()}")
                                    Text("Venta: $${item.precioVenta.toInt()}")
                                    Text("Cantidad: ${item.cantidad}")
                                    Text("Subtotal: $${(item.precioVenta * item.cantidad).toInt()}")
                                }
                            }

                            if (responsive.usarLayoutVertical) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    datos()
                                    ResponsiveButtonPair(
                                        first = { modifier ->
                                            Button(
                                                onClick = { viewModel.disminuirCantidad(item) },
                                                modifier = modifier,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = VerdePrincipal
                                                ),
                                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                            ) {
                                                Text("-")
                                            }
                                        },
                                        second = { modifier ->
                                            Button(
                                                onClick = { viewModel.aumentarCantidad(item) },
                                                modifier = modifier,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = VerdePrincipal
                                                ),
                                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                            ) {
                                                Text("+")
                                            }
                                        }
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        datos()
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.disminuirCantidad(item) },
                                            modifier = Modifier.heightIn(min = responsive.alturaBotonCompacto),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = VerdePrincipal
                                            ),
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp)
                                        ) {
                                            Text("-")
                                        }

                                        Button(
                                            onClick = { viewModel.aumentarCantidad(item) },
                                            modifier = Modifier.heightIn(min = responsive.alturaBotonCompacto),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = VerdePrincipal
                                            ),
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp)
                                        ) {
                                            Text("+")
                                        }
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
                        ResponsiveButtonPair(
                            first = { modifier ->
                                Button(
                                    onClick = { mostrarConfirmacion = false },
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
                                        viewModel.vaciarPedido()
                                        mostrarConfirmacion = false
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
                    title = {
                        Text("Confirmar")
                    },
                    text = {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text("\u00bfSeguro que quieres vaciar el pedido?")
                        }
                    }
                )
            }
        }
    }
}
