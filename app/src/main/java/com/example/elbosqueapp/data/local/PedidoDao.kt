package com.example.elbosqueapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PedidoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarItems(items: List<ItemPedidoEntity>)

    @Query("SELECT * FROM pedido_items")
    suspend fun obtenerItems(): List<ItemPedidoEntity>

    @Query("DELETE FROM pedido_items")
    suspend fun eliminarTodos()
}