package com.example.elbosqueapp.data.model

data class Producto(
    val codigo: String,
    val descripcion: String,
    val costo: Double,
    val precioVenta: Double,
    val existencia: Int,
    val inventarioMinimo: Int,
    val tipoVenta: String
)