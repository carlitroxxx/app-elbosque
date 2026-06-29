package com.example.elbosqueapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface VentaDao {

    @Insert
    suspend fun insertarVenta(venta: VentaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarItems(items: List<ItemVentaEntity>)

    @Query("SELECT * FROM ventas ORDER BY fecha DESC")
    suspend fun obtenerVentas(): List<VentaEntity>

    @Query("SELECT * FROM ventas WHERE id = :ventaId LIMIT 1")
    suspend fun obtenerVentaPorId(ventaId: Long): VentaEntity?

    @Query("SELECT * FROM venta_items WHERE ventaId = :ventaId")
    suspend fun obtenerItemsDeVenta(ventaId: Long): List<ItemVentaEntity>

    @Query("DELETE FROM venta_items WHERE ventaId = :ventaId")
    suspend fun eliminarItemsDeVenta(ventaId: Long)

    @Query("DELETE FROM ventas WHERE id = :ventaId")
    suspend fun eliminarVenta(ventaId: Long)
}