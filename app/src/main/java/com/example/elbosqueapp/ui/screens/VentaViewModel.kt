package com.example.elbosqueapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elbosqueapp.data.local.ItemVentaEntity
import com.example.elbosqueapp.data.local.ProductoEntity
import com.example.elbosqueapp.data.local.VentaEntity
import com.example.elbosqueapp.data.model.ItemVenta
import com.example.elbosqueapp.data.repository.VentaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VentaViewModel(
    private val repository: VentaRepository
) : ViewModel() {

    private val _productos = MutableStateFlow<List<ProductoEntity>>(emptyList())
    val productos: StateFlow<List<ProductoEntity>> = _productos

    private val _ventaActual = MutableStateFlow<List<ItemVenta>>(emptyList())
    val ventaActual: StateFlow<List<ItemVenta>> = _ventaActual

    private val _tipoPago = MutableStateFlow("Efectivo")
    val tipoPago: StateFlow<String> = _tipoPago

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    val totalVenta: StateFlow<Double> = _ventaActual.map { lista ->
        lista.sumOf { it.subtotal }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val cantidadProductosVenta: StateFlow<Int> = _ventaActual.map { lista ->
        lista.size
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun cargarProductos() {
        viewModelScope.launch {
            _productos.value = repository.obtenerProductos()
        }
    }

    fun seleccionarTipoPago(tipoPago: String) {
        _tipoPago.value = tipoPago
    }

    fun agregarProductoUnidad(producto: ProductoEntity, cantidad: Int) {
        if (cantidad <= 0) {
            _mensaje.value = "La cantidad debe ser mayor a 0"
            return
        }

        val lista = _ventaActual.value.toMutableList()
        val index = lista.indexOfFirst { it.codigo == producto.codigo }

        if (index != -1) {
            val item = lista[index]
            val nuevaCantidad = item.cantidad + cantidad
            lista[index] = item.copy(
                cantidad = nuevaCantidad,
                subtotal = nuevaCantidad * item.precioUnitario
            )
        } else {
            lista.add(
                ItemVenta(
                    codigo = producto.codigo,
                    producto = producto.descripcion,
                    tipoVenta = producto.tipoVenta,
                    cantidad = cantidad.toDouble(),
                    precioUnitario = producto.precioVenta,
                    subtotal = producto.precioVenta * cantidad
                )
            )
        }

        _ventaActual.value = lista
    }

    fun agregarProductoGranel(producto: ProductoEntity, montoPesos: Double) {
        if (producto.precioVenta <= 0) {
            _mensaje.value = "El producto no tiene precio de venta válido"
            return
        }

        if (montoPesos <= 0) {
            _mensaje.value = "El monto debe ser mayor a 0"
            return
        }

        val kilos = montoPesos / producto.precioVenta

        val lista = _ventaActual.value.toMutableList()
        val index = lista.indexOfFirst { it.codigo == producto.codigo }

        if (index != -1) {
            val item = lista[index]
            val nuevoSubtotal = item.subtotal + montoPesos
            val nuevaCantidad = nuevoSubtotal / item.precioUnitario

            lista[index] = item.copy(
                cantidad = nuevaCantidad,
                subtotal = nuevoSubtotal
            )
        } else {
            lista.add(
                ItemVenta(
                    codigo = producto.codigo,
                    producto = producto.descripcion,
                    tipoVenta = producto.tipoVenta,
                    cantidad = kilos,
                    precioUnitario = producto.precioVenta,
                    subtotal = montoPesos
                )
            )
        }

        _ventaActual.value = lista
    }

    fun actualizarItemUnidad(codigo: String, cantidad: Int) {
        if (cantidad <= 0) {
            eliminarItem(codigo)
            return
        }

        val lista = _ventaActual.value.toMutableList()
        val index = lista.indexOfFirst { it.codigo == codigo }

        if (index != -1) {
            val item = lista[index]
            lista[index] = item.copy(
                cantidad = cantidad.toDouble(),
                subtotal = cantidad * item.precioUnitario
            )
            _ventaActual.value = lista
        }
    }

    fun actualizarItemGranel(codigo: String, montoPesos: Double) {
        if (montoPesos <= 0) {
            eliminarItem(codigo)
            return
        }

        val lista = _ventaActual.value.toMutableList()
        val index = lista.indexOfFirst { it.codigo == codigo }

        if (index != -1) {
            val item = lista[index]

            if (item.precioUnitario <= 0) {
                _mensaje.value = "El producto no tiene precio válido"
                return
            }

            lista[index] = item.copy(
                cantidad = montoPesos / item.precioUnitario,
                subtotal = montoPesos
            )
            _ventaActual.value = lista
        }
    }

    fun eliminarItem(codigo: String) {
        _ventaActual.value = _ventaActual.value.filterNot { it.codigo == codigo }
    }

    fun vaciarVenta() {
        _ventaActual.value = emptyList()
    }

    fun finalizarVenta() {
        val items = _ventaActual.value

        if (items.isEmpty()) {
            _mensaje.value = "No hay productos en la venta"
            return
        }

        viewModelScope.launch {
            val total = items.sumOf { it.subtotal }

            val venta = VentaEntity(
                fecha = System.currentTimeMillis(),
                tipoPago = _tipoPago.value,
                total = total
            )

            val itemsEntity = items.map {
                ItemVentaEntity(
                    ventaId = 0,
                    codigo = it.codigo,
                    producto = it.producto,
                    tipoVenta = it.tipoVenta,
                    cantidad = it.cantidad,
                    precioUnitario = it.precioUnitario,
                    subtotal = it.subtotal
                )
            }

            repository.guardarVenta(venta, itemsEntity)

            _ventaActual.value = emptyList()
            _tipoPago.value = "Efectivo"
            _mensaje.value = "Venta guardada correctamente"
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}