package com.example.elbosqueapp.data.model

data class ItemVenta(
    val codigo: String,
    val producto: String,
    val tipoVenta: String,
    val cantidad: Double,
    val precioUnitario: Double,
    val subtotal: Double
)