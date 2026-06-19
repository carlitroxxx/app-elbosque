package com.example.elbosqueapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pedido_items")
data class ItemPedidoEntity(
    @PrimaryKey val codigo: String,
    val producto: String,
    val cantidad: Int,
    val precioCompra: Double,
    val precioVenta: Double,
    val stock: Int
)