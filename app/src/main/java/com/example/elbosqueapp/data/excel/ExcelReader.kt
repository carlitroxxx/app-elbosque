package com.example.elbosqueapp.data.excel

import android.content.Context
import android.net.Uri
import com.example.elbosqueapp.data.local.ProductoEntity
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.WorkbookFactory

class ExcelReader {

    fun leerProductosDesdeExcel(context: Context, uri: Uri): List<ProductoEntity> {
        val productos = mutableListOf<ProductoEntity>()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)

            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i) ?: continue

                var codigo = leerTexto(row.getCell(0))
                var descripcion = leerTexto(row.getCell(1))

                if (codigo.length > 20 || descripcion.all { it.isDigit() }) {
                    val temp = codigo
                    codigo = descripcion
                    descripcion = temp
                }

                val producto = ProductoEntity(
                    codigo = codigo,
                    descripcion = descripcion,
                    costo = leerNumero(row.getCell(2)),
                    precioVenta = leerNumero(row.getCell(3)),
                    existencia = leerNumero(row.getCell(6)).toInt(),
                    inventarioMinimo = leerNumero(row.getCell(7)).toInt(),
                    tipoVenta = leerTexto(row.getCell(9)).uppercase()
                )

                if (producto.codigo.isNotBlank() && producto.descripcion.isNotBlank()) {
                    productos.add(producto)
                }
            }

            workbook.close()
        }

        return productos
    }

    private fun leerTexto(cell: Cell?): String {
        return cell?.toString()?.trim() ?: ""
    }

    private fun leerNumero(cell: Cell?): Double {
        if (cell == null) return 0.0

        val texto = cell.toString()
            .replace("$", "")
            .replace(".", "")
            .replace(",", "")
            .replace(" ", "")
            .replace("-", "0")
            .trim()

        return texto.toDoubleOrNull() ?: 0.0
    }
}