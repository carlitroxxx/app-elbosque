package com.example.elbosqueapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.elbosqueapp.R
import com.example.elbosqueapp.ui.theme.VerdePrincipal

@Composable
fun Header(
    titulo: String,
    botonTexto: String? = null,
    onBotonClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null
) {
    val usaFuenteGrande = LocalDensity.current.fontScale >= 1.2f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (usaFuenteGrande) 8.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onMenuClick != null) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            ) {
                Text(
                    text = "\u2630",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.width(4.dp))
        }


        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.weight(1f),
            maxLines = if (usaFuenteGrande) 2 else 1,
            softWrap = true
        )

        if (botonTexto != null && onBotonClick != null) {
            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onBotonClick,
                modifier = Modifier.heightIn(min = if (usaFuenteGrande) 48.dp else 40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdePrincipal
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = botonTexto,
                    maxLines = if (usaFuenteGrande) 2 else 1,
                    softWrap = true
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(if (usaFuenteGrande) 36.dp else 40.dp)
        )

    }
}
