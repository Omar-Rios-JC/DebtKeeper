package com.example.debtkeeper

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.debtkeeper.data.DebtEntity
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
    var mostrarDialogoPago by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var mostrarDialogoReactivar by remember { mutableStateOf(false) }
    var mostrarDialogoActualizarDeuda by remember { mutableStateOf(false) }
    var mostrarSelectorFecha by remember { mutableStateOf(false) }
    var mostrarAlerta by remember { mutableStateOf(false) }
    var montoPago by remember { mutableStateOf("") }
    var fechaPago by remember { mutableStateOf("Seleccionar fecha") }
    var nuevoMonto by remember { mutableStateOf("") }
    var maximoPermitido by remember { mutableStateOf(0.0) }

    val pagos by viewModel.obtenerPagosPorDeuda(persona.id).collectAsState(initial = emptyList())
    val pagado = (persona.totalDeuda - persona.restante).coerceAtLeast(0.0)
    val progreso = if (persona.totalDeuda > 0) (pagado / persona.totalDeuda).toFloat().coerceIn(0f, 1f) else 0f
    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .clickable { expandido = !expandido },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (expandido) 5.dp else 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (persona.saldada) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (persona.saldada) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = if (persona.saldada) Icons.Filled.Refresh else Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.padding(10.dp).size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = persona.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (persona.saldada) "Deuda liquidada" else "${pagos.size} pago(s) registrados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (persona.saldada) {
                    AssistChip(
                        onClick = { mostrarDialogoReactivar = true },
                        label = { Text("Pagado") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
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

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MoneyStat(
                    label = "Total",
                    value = formatCurrency(persona.totalDeuda),
                    modifier = Modifier.weight(1f)
                )
                MoneyStat(
                    label = "Pendiente",
                    value = formatCurrency(persona.restante.coerceAtLeast(0.0)),
                    modifier = Modifier.weight(1f),
                    alert = !persona.saldada
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50)),
                    color = if (persona.saldada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(progreso * 100).toInt()}% pagado",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (persona.interesAcumulado > 0.0) {
                        Text(
                            text = "Interés: ${formatCurrency(persona.interesAcumulado)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            if (expandido) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                if (pagos.isNotEmpty()) {
                    Text("Historial de pagos", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        pagos.forEach { pago ->
                            PaymentRow(fecha = pago.fecha, monto = pago.monto)
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "Sin pagos registrados.",
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { mostrarDialogoActualizarDeuda = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { mostrarDialogoEliminar = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                    Spacer(Modifier.width(8.dp))
                    if (persona.saldada) {
                        Button(
                            onClick = { mostrarDialogoReactivar = true },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Reactivar")
                        }
                    } else {
                        Button(
                            onClick = { mostrarDialogoPago = true },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Abonar")
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoPago) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPago = false },
            icon = { Icon(Icons.Filled.Payments, contentDescription = null) },
            title = { Text("Registrar pago") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PrettyTextField(
                        value = montoPago,
                        onValueChange = { montoPago = it },
                        label = "Monto",
                        keyboardType = KeyboardType.Decimal,
                        icon = Icons.Filled.AttachMoney
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                            .clickable { mostrarSelectorFecha = true },
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(fechaPago, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Filled.CalendarToday, contentDescription = "Seleccionar fecha")
                        }
                    }

                    if (mostrarSelectorFecha) {
                        MostrarDatePicker(
                            onFechaSeleccionada = { fechaString ->
                                val fechaSeleccionada = LocalDate.parse(fechaString, formatter)
                                val hoy = LocalDate.now()

                                if (fechaSeleccionada.isAfter(hoy)) {
                                    Toast.makeText(
                                        context,
                                        "No puedes seleccionar una fecha futura",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    fechaPago = fechaString
                                    mostrarSelectorFecha = false
                                }
                            },
                            onCerrar = { mostrarSelectorFecha = false }
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val monto = montoPago.toDoubleOrNull()
                    if (monto != null && monto > 0 && fechaPago != "Seleccionar fecha") {
                        if (monto > persona.restante) {
                            maximoPermitido = persona.restante
                            mostrarAlerta = true
                        } else {
                            viewModel.registrarPago(persona, monto, fechaPago)
                            montoPago = ""
                            fechaPago = "Seleccionar fecha"
                            mostrarDialogoPago = false
                        }
                    }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoPago = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarAlerta) {
        AlertDialog(
            onDismissRequest = { mostrarAlerta = false },
            confirmButton = {
                TextButton(onClick = { mostrarAlerta = false }) {
                    Text("Aceptar")
                }
            },
            title = { Text("Monto inválido") },
            text = { Text("El pago no puede superar el saldo pendiente. Máximo permitido: ${formatCurrency(maximoPermitido)}") }
        )
    }

    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Eliminar deuda") },
            text = { Text("Se borrará la deuda de ${persona.nombre} y dejará de mostrarse en tu historial.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarPersona(persona)
                        mostrarDialogoEliminar = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarDialogoReactivar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoReactivar = false },
            icon = { Icon(Icons.Filled.Refresh, contentDescription = null) },
            title = { Text("Reactivar deuda") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Ingresa el nuevo monto total.")
                    PrettyTextField(
                        value = nuevoMonto,
                        onValueChange = { nuevoMonto = it },
                        label = "Nuevo monto",
                        keyboardType = KeyboardType.Decimal,
                        icon = Icons.Filled.AttachMoney
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val monto = nuevoMonto.toDoubleOrNull()
                    if (monto != null && monto > 0) {
                        viewModel.reactivarDeuda(persona, monto)
                        nuevoMonto = ""
                        mostrarDialogoReactivar = false
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoReactivar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarDialogoActualizarDeuda) {
        DialogoEditarDeuda(
            persona = persona,
            viewModel = viewModel,
            onCerrar = { mostrarDialogoActualizarDeuda = false }
        )
    }
}

@Composable
private fun MoneyStat(label: String, value: String, modifier: Modifier = Modifier, alert: Boolean = false) {
    Surface(
        modifier = modifier,
        color = if (alert) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.42f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        contentColor = if (alert) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun PaymentRow(fecha: String, monto: Double) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer, shape = CircleShape) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.padding(8.dp).size(16.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(fecha, style = MaterialTheme.typography.bodyMedium)
            }
            Text(formatCurrency(monto), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
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

@Composable
fun DialogoEditarDeuda(
    persona: DebtEntity,
    viewModel: PersonasViewModel,
    onCerrar: () -> Unit
) {
    var nombre by remember { mutableStateOf(persona.nombre) }
    var aumentoDeuda by remember { mutableStateOf("") }
    var aumentarInteres by remember { mutableStateOf("") }
    var disminuirInteres by remember { mutableStateOf("") }
    var duracionCantidad by remember { mutableStateOf(persona.plazoPagos.toString()) }
    var duracionPlazo by remember { mutableStateOf("Meses") }
    var expanded by remember { mutableStateOf(false) }
    var aplicacionInteres by remember { mutableStateOf(persona.tipoInteres) }
    var expandedInteres by remember { mutableStateOf(false) }
    val opciones = listOf("Días", "Meses", "Años")
    val opcionesInteres = listOf("Una sola vez", "Diario", "Semanal", "Mensual", "Anual")

    LaunchedEffect(aplicacionInteres) {
        if (aplicacionInteres == "Una sola vez") {
            duracionCantidad = "1"
            duracionPlazo = "Días"
        }
    }

    AlertDialog(
        onDismissRequest = onCerrar,
        icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
        title = { Text("Actualizar deuda") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PrettyTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = "Nombre del deudor",
                    keyboardType = KeyboardType.Text,
                    icon = Icons.Filled.Person
                )

                PrettyTextField(
                    value = aumentoDeuda,
                    onValueChange = { aumentoDeuda = it },
                    label = "Monto a agregar",
                    keyboardType = KeyboardType.Decimal,
                    icon = Icons.Filled.AttachMoney
                )

                DialogSelector(
                    label = "Interés",
                    value = aplicacionInteres,
                    expanded = expandedInteres,
                    onExpandedChange = { expandedInteres = it },
                    options = opcionesInteres,
                    onOptionSelected = { aplicacionInteres = it }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PrettyTextField(
                        value = duracionCantidad,
                        onValueChange = { duracionCantidad = it },
                        label = "Duración",
                        keyboardType = KeyboardType.Number,
                        icon = Icons.Filled.Schedule,
                        modifier = Modifier.weight(1f),
                        enabled = aplicacionInteres != "Una sola vez"
                    )
                    DialogSelector(
                        label = "Plazo",
                        value = duracionPlazo,
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        options = opciones,
                        onOptionSelected = { duracionPlazo = it },
                        modifier = Modifier.weight(1f),
                        enabled = aplicacionInteres != "Una sola vez"
                    )
                }

                PrettyTextField(
                    value = aumentarInteres,
                    onValueChange = { aumentarInteres = it },
                    label = "Agregar interés",
                    keyboardType = KeyboardType.Decimal,
                    icon = Icons.Filled.AttachMoney
                )

                PrettyTextField(
                    value = disminuirInteres,
                    onValueChange = { disminuirInteres = it },
                    label = "Disminuir interés",
                    keyboardType = KeyboardType.Decimal,
                    icon = Icons.Filled.AttachMoney
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val aumento = aumentoDeuda.toDoubleOrNull() ?: 0.0
                val interesMas = aumentarInteres.toDoubleOrNull() ?: 0.0
                val interesMenos = disminuirInteres.toDoubleOrNull() ?: 0.0
                val duracionPlazoPago = duracionCantidad.toIntOrNull() ?: 0
                val interesAjustado = (interesMas * duracionPlazoPago) - interesMenos

                val deudaActualizada = persona.copy(
                    nombre = nombre.trim().ifBlank { persona.nombre },
                    totalDeuda = persona.totalDeuda + aumento + interesAjustado,
                    restante = persona.restante + aumento + interesAjustado,
                    tipoInteres = aplicacionInteres,
                    plazoPagos = duracionPlazoPago,
                    interesAcumulado = (persona.interesAcumulado + interesAjustado).coerceAtLeast(0.0)
                )

                viewModel.actualizarPersona(deudaActualizada)
                onCerrar()
            }) {
                Icon(Icons.Filled.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun PrettyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        enabled = enabled,
        leadingIcon = { Icon(icon, contentDescription = null) },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun DialogSelector(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { onExpandedChange(true) },
            readOnly = true,
            enabled = enabled,
            shape = RoundedCornerShape(16.dp)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onOptionSelected(opcion)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}
