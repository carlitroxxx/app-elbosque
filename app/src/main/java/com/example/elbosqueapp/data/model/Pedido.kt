package com.example.elbosqueapp.data.model

data class Pedido(
    val productos: MutableList<ItemPedido> = mutableListOf()
)

data class ItemPedido(
    val codigo: String,
    val producto: String,
    var cantidad: Int,
    val precioCompra: Double,
    val precioVenta: Double,
    val stock: Int
)