package com.example.elbosqueapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ResponsiveInfo(
    val usaFuenteGrande: Boolean,
    val esPantallaAngosta: Boolean,
    val usarLayoutVertical: Boolean,
    val paddingPantalla: Dp,
    val espacio: Dp,
    val alturaBoton: Dp,
    val alturaBotonCompacto: Dp
)

@Composable
fun responsiveInfo(maxWidth: Dp): ResponsiveInfo {
    val fontScale = LocalDensity.current.fontScale
    val usaFuenteGrande = fontScale >= 1.2f
    val esPantallaAngosta = maxWidth < 360.dp

    return ResponsiveInfo(
        usaFuenteGrande = usaFuenteGrande,
        esPantallaAngosta = esPantallaAngosta,
        usarLayoutVertical = usaFuenteGrande || esPantallaAngosta,
        paddingPantalla = if (esPantallaAngosta) 12.dp else 16.dp,
        espacio = if (usaFuenteGrande) 10.dp else 8.dp,
        alturaBoton = if (usaFuenteGrande) 52.dp else 44.dp,
        alturaBotonCompacto = if (usaFuenteGrande) 44.dp else 36.dp
    )
}

@Composable
fun ResponsiveButtonPair(
    modifier: Modifier = Modifier,
    secondWeight: Float = 1f,
    first: @Composable (Modifier) -> Unit,
    second: @Composable (Modifier) -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val responsive = responsiveInfo(maxWidth)

        if (responsive.usarLayoutVertical) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                first(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = responsive.alturaBoton)
                )
                second(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = responsive.alturaBoton)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                first(
                    Modifier
                        .weight(1f)
                        .heightIn(min = responsive.alturaBoton)
                )
                second(
                    Modifier
                        .weight(secondWeight)
                        .heightIn(min = responsive.alturaBoton)
                )
            }
        }
    }
}
