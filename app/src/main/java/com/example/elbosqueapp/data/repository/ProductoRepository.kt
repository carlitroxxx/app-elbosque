package com.example.elbosqueapp.data.repository

import com.example.elbosqueapp.data.local.ProductoDao
import com.example.elbosqueapp.data.local.ProductoEntity
import com.example.elbosqueapp.data.local.PedidoDao
import com.example.elbosqueapp.data.local.ItemPedidoEntity
class ProductoRepository(
    private val productoDao: ProductoDao,
    private val pedidoDao: PedidoDao
) {
    suspend fun insertarProductos(productos: List<ProductoEntity>) {
        productoDao.insertarProductos(productos)
    }

    suspend fun obtenerProductos(): List<ProductoEntity> {
        return productoDao.obtenerProductos()
    }

    suspend fun eliminarTodos() {
        productoDao.eliminarTodos()
    }

    suspend fun guardarPedido(items: List<ItemPedidoEntity>) {
        pedidoDao.eliminarTodos()
        pedidoDao.insertarItems(items)
    }

    suspend fun obtenerPedido(): List<ItemPedidoEntity> {
        return pedidoDao.obtenerItems()
    }

    suspend fun vaciarPedidoGuardado() {
        pedidoDao.eliminarTodos()
    }
}