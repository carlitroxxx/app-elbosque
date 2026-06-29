package com.example.elbosqueapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ProductoEntity::class,
        ItemPedidoEntity::class,
        VentaEntity::class,
        ItemVentaEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDao
    abstract fun pedidoDao(): PedidoDao
    abstract fun ventaDao(): VentaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `ventas` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `fecha` INTEGER NOT NULL,
                        `tipoPago` TEXT NOT NULL,
                        `total` REAL NOT NULL
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `venta_items` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `ventaId` INTEGER NOT NULL,
                        `codigo` TEXT NOT NULL,
                        `producto` TEXT NOT NULL,
                        `tipoVenta` TEXT NOT NULL,
                        `cantidad` REAL NOT NULL,
                        `precioUnitario` REAL NOT NULL,
                        `subtotal` REAL NOT NULL,
                        FOREIGN KEY(`ventaId`) REFERENCES `ventas`(`id`) 
                        ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_venta_items_ventaId` ON `venta_items` (`ventaId`)"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "el_bosque_database"
                )
                    .addMigrations(MIGRATION_3_4)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}