package com.example.elbosqueapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProductoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProductos(productos: List<ProductoEntity>)

    @Query("SELECT * FROM productos")
    suspend fun obtenerProductos(): List<ProductoEntity>

    @Query("DELETE FROM productos")
    suspend fun eliminarTodos()
}