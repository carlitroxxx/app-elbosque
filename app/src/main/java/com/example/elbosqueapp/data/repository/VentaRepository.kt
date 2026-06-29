package com.example.elbosqueapp.data.repository

import com.example.elbosqueapp.data.local.ItemVentaEntity
import com.example.elbosqueapp.data.local.ProductoDao
import com.example.elbosqueapp.data.local.ProductoEntity
import com.example.elbosqueapp.data.local.VentaDao
import com.example.elbosqueapp.data.local.VentaEntity

class VentaRepository(
    private val productoDao: ProductoDao,
    private val ventaDao: VentaDao
) {
    suspend fun obtenerProductos(): List<ProductoEntity> {
        return productoDao.obtenerProductos()
    }

    suspend fun guardarVenta(
        venta: VentaEntity,
        items: List<ItemVentaEntity>
    ): Long {
        val ventaId = ventaDao.insertarVenta(venta)

        val itemsConVentaId = items.map {
            it.copy(ventaId = ventaId)
        }

        ventaDao.insertarItems(itemsConVentaId)

        return ventaId
    }

    suspend fun obtenerVentas(): List<VentaEntity> {
        return ventaDao.obtenerVentas()
    }

    suspend fun obtenerVentaPorId(ventaId: Long): VentaEntity? {
        return ventaDao.obtenerVentaPorId(ventaId)
    }

    suspend fun obtenerItemsDeVenta(ventaId: Long): List<ItemVentaEntity> {
        return ventaDao.obtenerItemsDeVenta(ventaId)
    }

    suspend fun eliminarVentaCompleta(ventaId: Long) {
        ventaDao.eliminarItemsDeVenta(ventaId)
        ventaDao.eliminarVenta(ventaId)
    }
}