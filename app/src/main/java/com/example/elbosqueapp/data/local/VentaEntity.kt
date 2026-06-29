package com.example.elbosqueapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ventas")
data class VentaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fecha: Long,
    val tipoPago: String,
    val total: Double
)