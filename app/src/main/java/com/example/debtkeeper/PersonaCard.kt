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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.debtkeeper.data.DebtEntity
import java.time.format.DateTimeFormatter
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaCard(
    persona: DebtEntity,
    viewModel: PersonasViewModel,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var mostrarDialogoReactivar by remember { mutableStateOf(false) }
    var montoPago by remember { mutableStateOf("") }
    var fechaPago by remember { mutableStateOf("") }
    var nuevoMonto by remember { mutableStateOf("") }
    var mostrarSelectorFecha by remember { mutableStateOf(false) }
    val pagos by viewModel.obtenerPagosPorDeuda(persona.id).collectAsState(initial = emptyList())
    val pagado = persona.totalDeuda - persona.restante
    val progreso = if (persona.totalDeuda > 0) (pagado / persona.totalDeuda).toFloat() else 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .clickable { expandido = !expandido },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = persona.nombre,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (persona.saldada) {
                    AssistChip(
                        onClick = {},
                        label = { Text("¡Pagado!", color = Color.White) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        border = null
                    )
                } else {
                    Icon(
                        imageVector = if (expandido) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = "Expandir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text("$${persona.totalDeuda}", style = MaterialTheme.typography.bodyLarge)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Pendiente", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(
                        "$${persona.restante}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (persona.saldada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Column {
                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Text(
                    text = "${(progreso * 100).toInt()}% Pagado",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (expandido) {
                Divider(modifier = Modifier.padding(vertical = 12.dp))

                if (pagos.isNotEmpty()) {
                    Text("Historial de pagos:", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    pagos.forEach { pago ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("• ${pago.fecha}", style = MaterialTheme.typography.bodySmall)
                            Text("Abonó $${pago.monto}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                } else {
                    Text("Sin pagos registrados.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { mostrarDialogoEliminar = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }

                    Spacer(Modifier.width(8.dp))

                    if (persona.saldada) {
                        Button(onClick = { mostrarDialogoReactivar = true }) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Reactivar")
                        }
                    } else {
                        Button(onClick = { mostrarDialogo = true }) {
                            Text("Abonar")
                        }
                    }
                }
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
                        label = { Text("Monto") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = fechaPago,
                        onValueChange = { fechaPago = it },
                        label = { Text("Fecha") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { mostrarSelectorFecha = true }) {
                                Text("Seleccionar")
                            }
                        }
                    )
                    if (mostrarSelectorFecha) {
                        MostrarDatePicker(
                            onFechaSeleccionada = { fechaPago = it },
                            onCerrar = { mostrarSelectorFecha = false }
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val monto = montoPago.toDoubleOrNull()
                    if (monto != null && fechaPago.isNotBlank()) {
                        viewModel.registrarPago(persona, monto, fechaPago)
                        montoPago = ""
                        fechaPago = ""
                        mostrarDialogo = false
                    }
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") } }
        )
    }

    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("¿Eliminar deuda?") },
            text = { Text("Se borrará todo el historial de pagos de ${persona.nombre}.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarPersona(persona)
                        mostrarDialogoEliminar = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogoEliminar = false }) { Text("Cancelar") } }
        )
    }

    if (mostrarDialogoReactivar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoReactivar = false },
            title = { Text("Reactivar deuda") },
            text = {
                Column {
                    Text("Ingresa el nuevo monto total:")
                    OutlinedTextField(
                        value = nuevoMonto,
                        onValueChange = { nuevoMonto = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val monto = nuevoMonto.toDoubleOrNull()
                    if (monto != null) {
                        viewModel.reactivarDeuda(persona, monto)
                        nuevoMonto = ""
                        mostrarDialogoReactivar = false
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogoReactivar = false }) { Text("Cancelar") } }
        )
    }

}

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
                val selectedDate = datePickerState.selectedDateMillis
                if (selectedDate != null) {
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    formatter.timeZone = TimeZone.getTimeZone("UTC")
                    onFechaSeleccionada(formatter.format(Date(selectedDate)))
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