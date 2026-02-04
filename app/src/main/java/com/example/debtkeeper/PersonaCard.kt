package com.example.debtkeeper

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.debtkeeper.PersonasViewModel
import com.example.debtkeeper.data.DebtEntity
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.input.KeyboardType


@RequiresApi(Build.VERSION_CODES.O)
fun esFechaValida(fecha: String): Boolean {
    val regex = Regex("""\d{2}/\d{2}/\d{4}""")
    if (!regex.matches(fecha)) return false

    return try {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
        java.time.LocalDate.parse(fecha, formatter)
        true
    } catch (_: Exception) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PersonaCard(
    persona: DebtEntity,
    viewModel: PersonasViewModel,
    modifier: Modifier = Modifier
)
{
    var expandido by remember { mutableStateOf(false) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var montoPago by remember { mutableStateOf("") }
    var fechaPago by remember { mutableStateOf("") }

    var mostrarDialogoReactivar by remember { mutableStateOf(false) }
    var nuevoMonto by remember { mutableStateOf("") }

    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    val pagos by viewModel
        .obtenerPagosPorDeuda(persona.id)
        .collectAsState(initial = emptyList())


    val grisClaro = Color(0xFFF8F9FA)
    val textoPrincipal = Color(0xFF0B3D2E)
    val textoSecundario = Color(0xFF145A32)

    var mostrarSelectorFecha by remember { mutableStateOf(false) }




    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .clickable { expandido = !expandido },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = grisClaro),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    persona.nombre,
                    style = MaterialTheme.typography.titleMedium.copy(color = textoPrincipal)
                )

                if (persona.saldada) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Saldada") }
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                "Deuda total: $${persona.totalDeuda}",
                style = MaterialTheme.typography.bodyMedium.copy(color = textoSecundario)
            )

            Text(
                "Restante: $${persona.restante}",
                style = MaterialTheme.typography.bodyMedium.copy(color = textoPrincipal)
            )

            if (expandido) {
                if (pagos.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text("Pagos realizados:", style = MaterialTheme.typography.titleSmall)

                    pagos.forEach { pago ->
                        Text(
                            "• Pago $${pago.monto} — ${pago.fecha}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }


                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    onClick = { mostrarDialogoEliminar = true }
                ) {
                    Text("Eliminar deuda")
                }

                Spacer(Modifier.height(12.dp))

                if (persona.saldada) {
                    Button(
                        onClick = { mostrarDialogoReactivar = true }
                    ) {
                        Text("Reactivar deuda")
                    }

                } else {
                    Button(
                        onClick = { mostrarDialogo = true }
                    ) {
                        Text("Registrar pago")
                    }
                }
            }

            if (mostrarDialogo) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogo = false },
                    title = { Text("Registrar pago") },
                    text = {
                        Column {

                            OutlinedTextField(
                                value = montoPago,
                                onValueChange = { montoPago = it },
                                label = { Text("Monto abonado") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions.Default.copy(
                                        keyboardType = KeyboardType.Number
                                )
                            )

                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = fechaPago,
                                onValueChange = { fechaPago = it },
                                label = { Text("Fecha (dd/MM/yyyy)") },
                                readOnly = true,
                            )

                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Button(onClick = {
                                    mostrarSelectorFecha = true
                                }) {
                                    Text("Seleccionar Fecha")
                                }
                            }


                            if (mostrarSelectorFecha) {
                                MostrarDatePicker(
                                    onFechaSeleccionada = { fecha ->
                                        fechaPago = fecha
                                    },
                                    onCerrar = {
                                        mostrarSelectorFecha = false
                                    }
                                )
                            }

                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val monto = montoPago.toDoubleOrNull() ?: return@Button
                                if (fechaPago.isBlank()) return@Button

                                viewModel.registrarPago(
                                    deuda = persona,
                                    monto = monto,
                                    fecha = fechaPago
                                )

                                montoPago = ""
                                fechaPago = ""
                                mostrarDialogo = false
                            }

                        ) {
                            Text("Guardar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarDialogo = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
            if (mostrarDialogoEliminar) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogoEliminar = false },
                    title = { Text("Eliminar deuda") },
                    text = {
                        Text("¿Seguro que deseas eliminar esta deuda? Esta acción no se puede deshacer.")
                    },
                    confirmButton = {
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            onClick = {
                                viewModel.eliminarPersona(persona)
                                mostrarDialogoEliminar = false
                            }
                        ) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { mostrarDialogoEliminar = false }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }
            if (mostrarDialogoReactivar) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogoReactivar = false },
                    title = { Text("Reactivar deuda") },
                    text = {
                        Column {

                            Text(
                                "Ingresa el nuevo monto de la deuda.\n\n" +
                                        "⚠️ Esta acción modificará la deuda actual.\n" +
                                        "Si deseas conservar el historial intacto, " +
                                        "es recomendable crear una nueva deuda."
                            )

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = nuevoMonto,
                                onValueChange = { nuevoMonto = it },
                                label = { Text("Nuevo monto") },
                                singleLine = true
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val monto = nuevoMonto.toDoubleOrNull() ?: return@Button

                                viewModel.reactivarDeuda(
                                    persona = persona,
                                    nuevoMonto = monto
                                )

                                nuevoMonto = ""
                                mostrarDialogoReactivar = false
                            }
                        ) {
                            Text("Confirmar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarDialogoReactivar = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }



        }
    }

}
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MostrarDatePicker(
    onFechaSeleccionada: (String) -> Unit,
    onCerrar: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onCerrar,
        confirmButton = {
            TextButton(onClick = {
                val millis = datePickerState.selectedDateMillis
                if (millis != null) {
                    val fecha = java.time.Instant.ofEpochMilli(millis)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()

                    val fechaFinal = fecha.format(
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    )

                    onFechaSeleccionada(fechaFinal)
                }
                onCerrar()
            }) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}