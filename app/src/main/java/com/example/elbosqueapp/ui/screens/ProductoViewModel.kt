package com.example.elbosqueapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elbosqueapp.data.local.ProductoEntity
import com.example.elbosqueapp.data.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.content.Context
import android.net.Uri
import com.example.elbosqueapp.data.excel.ExcelReader
import com.example.elbosqueapp.data.model.ItemPedido
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import com.example.elbosqueapp.data.local.ItemPedidoEntity

class ProductoViewModel(
    private val repository: ProductoRepository
) : ViewModel() {
    private val _pedido = MutableStateFlow<List<ItemPedido>>(emptyList())
    val pedido: StateFlow<List<ItemPedido>> = _pedido

    fun agregarAlPedido(producto: ProductoEntity) {
        val pedidoActual = _pedido.value.toMutableList()

        val existente = pedidoActual.find { it.codigo == producto.codigo }
        if (existente != null) {
            existente.cantidad += 1
        } else {
            pedidoActual.add(
                ItemPedido(
                    codigo = producto.codigo,
                    producto = producto.descripcion,
                    cantidad = 1,
                    precioCompra = producto.costo,
                    precioVenta = producto.precioVenta,
                    stock = producto.existencia
                )
            )
        }

        _pedido.value = pedidoActual

        viewModelScope.launch {
            repository.guardarPedido(_pedido.value.map { toEntity(it) })
        }
    }
    fun insertarProductosDePrueba() {
        viewModelScope.launch {
            repository.insertarProductos(
                listOf(
                    ProductoEntity(
                        codigo = "001",
                        descripcion = "Coca Cola 1.5L",
                        costo = 1200.0,
                        precioVenta = 1800.0,
                        existencia = 10,
                        inventarioMinimo = 3,
                        tipoVenta = "Unidad"
                    ),
                    ProductoEntity(
                        codigo = "002",
                        descripcion = "Pan de molde",
                        costo = 1500.0,
                        precioVenta = 2200.0,
                        existencia = 5,
                        inventarioMinimo = 2,
                        tipoVenta = "Unidad"
                    )
                )
            )
            cargarProductos()
        }
    }
    fun importarExcel(context: Context, uri: Uri) {
        viewModelScope.launch {
            val productos = ExcelReader().leerProductosDesdeExcel(context, uri)

            repository.eliminarTodos()
            repository.insertarProductos(productos)
            cargarProductos()
        }
    }

    private val _productos = MutableStateFlow<List<ProductoEntity>>(emptyList())
    val productos: StateFlow<List<ProductoEntity>> = _productos

    fun cargarProductos() {
        viewModelScope.launch {
            _productos.value = repository.obtenerProductos()
        }
    }
    fun aumentarCantidad(item: ItemPedido) {
        val lista = _pedido.value.toMutableList()
        val index = lista.indexOfFirst { it.producto == item.producto }

        if (index != -1) {
            lista[index] = lista[index].copy(cantidad = lista[index].cantidad + 1)
            _pedido.value = lista
        }

        viewModelScope.launch {
            repository.guardarPedido(_pedido.value.map { toEntity(it) })
        }
    }

    fun disminuirCantidad(item: ItemPedido) {
        val lista = _pedido.value.toMutableList()
        val index = lista.indexOfFirst { it.producto == item.producto }

        if (index != -1) {
            val nuevaCantidad = lista[index].cantidad - 1

            if (nuevaCantidad > 0) {
                lista[index] = lista[index].copy(cantidad = nuevaCantidad)
            } else {
                lista.removeAt(index)
            }

            _pedido.value = lista
        }

        viewModelScope.launch {
            repository.guardarPedido(_pedido.value.map { toEntity(it) })
        }
    }

    val total: StateFlow<Double> = _pedido.map { lista ->
        lista.sumOf { it.precioVenta * it.cantidad }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    fun vaciarPedido() {
        _pedido.value = emptyList()

        viewModelScope.launch {
            repository.vaciarPedidoGuardado()
        }
    }

    fun generarTextoPedido(): String {
        if (_pedido.value.isEmpty()) {
            return "Pedido vacío"
        }

        val detalle = _pedido.value.joinToString(separator = "\n") { item ->
            "- ${item.producto} | Cantidad: ${item.cantidad} | Subtotal: ${item.precioVenta * item.cantidad}"        }

        return """
        Pedido El Bosque
        
        $detalle
        
        Total: ${total.value}
    """.trimIndent()
    }

    val cantidadTotal: StateFlow<Int> = _pedido.map { lista ->
        lista.sumOf { it.cantidad }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )


    private fun toEntity(item: ItemPedido): ItemPedidoEntity {
        return ItemPedidoEntity(
            codigo = item.codigo,
            producto = item.producto,
            cantidad = item.cantidad,
            precioCompra = item.precioCompra,
            precioVenta = item.precioVenta,
            stock = item.stock
        )
    }

    private fun fromEntity(entity: ItemPedidoEntity): ItemPedido {
        return ItemPedido(
            codigo = entity.codigo,
            producto = entity.producto,
            cantidad = entity.cantidad,
            precioCompra = entity.precioCompra,
            precioVenta = entity.precioVenta,
            stock = entity.stock
        )
    }

    fun cargarPedidoGuardado() {
        viewModelScope.launch {
            val items = repository.obtenerPedido()
            _pedido.value = items.map { fromEntity(it) }
        }
    }
}