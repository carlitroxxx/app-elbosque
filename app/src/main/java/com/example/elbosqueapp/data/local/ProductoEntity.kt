package com.example.elbosqueapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey val codigo: String,
    val descripcion: String,
    val costo: Double,
    val precioVenta: Double,
    val existencia: Int,
    val inventarioMinimo: Int,
    val tipoVenta: String
)