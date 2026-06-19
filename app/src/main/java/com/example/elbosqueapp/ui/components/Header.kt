package com.example.elbosqueapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.elbosqueapp.R
import com.example.elbosqueapp.ui.theme.VerdePrincipal

@Composable
fun Header(
    titulo: String,
    botonTexto: String? = null,
    onBotonClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.weight(1f))

        if (botonTexto != null && onBotonClick != null) {
            Button(onClick = onBotonClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdePrincipal
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)) {
                Text(botonTexto)
            }
        }
    }
}