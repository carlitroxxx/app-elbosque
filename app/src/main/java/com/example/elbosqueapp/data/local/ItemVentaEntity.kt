package com.example.elbosqueapp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "venta_items",
    foreignKeys = [
        ForeignKey(
            entity = VentaEntity::class,
            parentColumns = ["id"],
            childColumns = ["ventaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ventaId")]
)
data class ItemVentaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ventaId: Long,
    val codigo: String,
    val producto: String,
    val tipoVenta: String,
    val cantidad: Double,
    val precioUnitario: Double,
    val subtotal: Double
)