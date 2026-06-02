package com.example.debtkeeper

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.debtkeeper.data.DebtEntity
import com.example.debtkeeper.ui.theme.DebtKeeperTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DebtKeeperTheme {
                val navController = rememberNavController()
                val viewModel: PersonasViewModel = viewModel()

                NavHost(navController = navController, startDestination = "lista") {
                    composable("lista") {
                        ListaPantalla(navController, viewModel)
                    }
                    composable("agregar/{modo}") { backStackEntry ->
                        val modo = DebtMode.fromStorage(backStackEntry.arguments?.getString("modo"))
                        AgregarPersonaPantalla(navController, viewModel, modo)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ListaPantalla(
    navController: NavHostController,
    viewModel: PersonasViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val personas = viewModel.listaPersonas
    var modoActual by remember { mutableStateOf(DebtMode.POR_COBRAR) }
    val deudasDelModo = personas.filter { DebtMode.fromStorage(it.modo) == modoActual }
    val personasAgrupadas = deudasDelModo.groupBy { it.nombre.trim().ifBlank { "Sin nombre" } }
    val mostrarSaldadas = UserPreferences.mostrarSaldadas(context).collectAsState(initial = true).value
    val tutorialCompletado = UserPreferences.tutorialCompletado(context).collectAsState(initial = true).value
    var mostrarTutorial by remember { mutableStateOf(false) }

    LaunchedEffect(tutorialCompletado) {
        if (!tutorialCompletado) {
            mostrarTutorial = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DebtKeeper", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Control claro de tus deudas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { mostrarTutorial = true }) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = "Ver tutorial",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("agregar/${modoActual.storageValue}") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(modoActual.addActionText()) },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        }
    ) { padding ->
        if (mostrarTutorial) {
            TutorialDialog(
                onDismiss = {
                    mostrarTutorial = false
                    scope.launch {
                        UserPreferences.setTutorialCompletado(context, true)
                    }
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DebtSummaryHeader(personas = deudasDelModo, modo = modoActual)
            }

            item {
                DebtModeSwitch(
                    modoActual = modoActual,
                    onModoChange = { modoActual = it }
                )
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            modoActual.activeSectionTitle(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "${deudasDelModo.count { !it.saldada }} pendientes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Historial",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = mostrarSaldadas,
                            onCheckedChange = { valor ->
                                scope.launch { UserPreferences.setMostrarSaldadas(context, valor) }
                            }
                        )
                    }
                }
            }

            if (deudasDelModo.isEmpty()) {
                item {
                    EmptyDebtState(
                        modo = modoActual,
                        onAdd = { navController.navigate("agregar/${modoActual.storageValue}") }
                    )
                }
            } else {
                personasAgrupadas.forEach { (nombre, deudas) ->
                    val activas = deudas.filter { !it.saldada }
                    if (activas.isNotEmpty()) {
                        item {
                            SectionTitle(title = nombre, subtitle = "${activas.size} deuda(s) activas")
                        }
                        items(activas, key = { it.id }) { deuda ->
                            PersonaCard(persona = deuda, viewModel = viewModel)
                        }
                    }
                }

                if (mostrarSaldadas) {
                    val saldadas = deudasDelModo.filter { it.saldada }
                    if (saldadas.isNotEmpty()) {
                        item {
                            HorizontalDivider(Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        }
                        item {
                            SectionTitle(title = "Historial saldado", subtitle = "${saldadas.size} deudas liquidadas")
                        }
                        items(saldadas, key = { it.id }) { deuda ->
                            PersonaCard(persona = deuda, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DebtSummaryHeader(personas: List<DebtEntity>, modo: DebtMode) {
    val totalPrestado = personas.sumOf { it.totalDeuda }
    val pendiente = personas.filterNot { it.saldada }.sumOf { it.restante.coerceAtLeast(0.0) }
    val saldadas = personas.count { it.saldada }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            modo.pendingSummaryTitle(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
                        )
                        Text(
                            formatCurrency(pendiente),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = CircleShape
                    ) {
                        Icon(
                            Icons.Filled.AttachMoney,
                            contentDescription = null,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryChip(label = modo.totalSummaryLabel(), value = formatCurrency(totalPrestado))
                    SummaryChip(label = "Activas", value = personas.count { !it.saldada }.toString())
                    SummaryChip(label = "Saldadas", value = saldadas.toString())
                }
            }
        }
    }
}

@Composable
private fun SummaryChip(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f),
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.76f))
            Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DebtModeSwitch(
    modoActual: DebtMode,
    onModoChange: (DebtMode) -> Unit
) {
    val estaEnModoPagar = modoActual == DebtMode.POR_PAGAR

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column {
                Text(
                    "Modo actual",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    modoActual.screenTitle(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Por cobrar",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (!estaEnModoPagar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = estaEnModoPagar,
                    onCheckedChange = { activo ->
                        onModoChange(if (activo) DebtMode.POR_PAGAR else DebtMode.POR_COBRAR)
                    }
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Por pagar",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (estaEnModoPagar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(Modifier.padding(top = 4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyDebtState(modo: DebtMode, onAdd: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.padding(14.dp).size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                modo.emptyTitle(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                modo.emptyDescription(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(modo.addButtonText())
            }
        }
    }
}

@Composable
fun TutorialDialog(onDismiss: () -> Unit) {
    val pasos = remember {
        listOf(
            OnboardingStep(
                icon = Icons.Filled.Info,
                title = "Bienvenido a DebtKeeper",
                description = "Esta app te ayuda a llevar el control de lo que te deben y de las deudas que tú debes pagar.",
                bullets = listOf(
                    "Tus deudas se organizan por persona o acreedor.",
                    "Puedes registrar abonos y ver el historial.",
                    "Las deudas liquidadas pasan a la sección de historial."
                )
            ),
            OnboardingStep(
                icon = Icons.Filled.CheckCircle,
                title = "Cambia de módulo",
                description = "Usa el switch de la pantalla principal para alternar entre deudas por cobrar y deudas que tú debes.",
                bullets = listOf(
                    "Por cobrar mantiene el flujo actual: personas que te deben.",
                    "Por pagar guarda a quién le debes, cuánto falta y tus pagos.",
                    "Cada módulo conserva su propio resumen, lista e historial."
                )
            ),
            OnboardingStep(
                icon = Icons.Filled.AttachMoney,
                title = "Resumen principal",
                description = "La tarjeta superior resume el estado general del módulo activo para que sepas rápidamente cuánto está pendiente.",
                bullets = listOf(
                    "Pendiente por cobrar o por pagar suma el saldo restante de tus deudas activas.",
                    "Total muestra lo registrado, incluyendo intereses ya aplicados.",
                    "Activas y saldadas te dicen cuántas deudas siguen abiertas o ya se pagaron."
                )
            ),
            OnboardingStep(
                icon = Icons.Filled.Add,
                title = "Agregar una deuda",
                description = "Usa el botón Nueva deuda desde el módulo activo para registrar el nombre, monto inicial y, si aplica, un interés.",
                bullets = listOf(
                    "Por cobrar: nombre de la persona que te debe.",
                    "Por pagar: nombre de a quién le debes.",
                    "Monto: dinero prestado, saldo inicial o cantidad que debes.",
                    "Calcular interés: opción para agregar un cargo fijo por periodo."
                )
            ),
            OnboardingStep(
                icon = Icons.Filled.CheckCircle,
                title = "Tarjetas y pagos",
                description = "Cada tarjeta muestra el total, lo pendiente y el avance de pago. Toca una tarjeta para ver sus acciones.",
                bullets = listOf(
                    "Abonar registra un pago con monto y fecha.",
                    "Editar permite ajustar nombre, monto o interes.",
                    "Eliminar borra la deuda de la lista."
                )
            ),
            OnboardingStep(
                icon = Icons.Filled.Percent,
                title = "Como se calculan los intereses",
                description = "El interes de DebtKeeper se maneja como una cantidad fija en dinero, no como porcentaje automatico.",
                bullets = listOf(
                    "Formula: interes acumulado = interes por periodo x numero de periodos.",
                    "Total final = monto base + interes acumulado.",
                    "Si eliges Una sola vez, la app usa 1 periodo.",
                    "Ejemplo: $1,000 + ($50 x 3 meses) = $1,150."
                )
            ),
            OnboardingStep(
                icon = Icons.Filled.Schedule,
                title = "Reglas importantes",
                description = "Estas validaciones ayudan a mantener tus cuentas claras y evitar registros accidentales.",
                bullets = listOf(
                    "Un pago no puede ser mayor que el saldo pendiente.",
                    "No se permiten fechas futuras para pagos.",
                    "Puedes mostrar u ocultar deudas saldadas con el interruptor Historial."
                )
            )
        )
    }
    var pasoActual by remember { mutableIntStateOf(0) }
    val paso = pasos[pasoActual]
    val esUltimoPaso = pasoActual == pasos.lastIndex

    AlertDialog(
        onDismissRequest = {},
        icon = {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = CircleShape
            ) {
                Icon(
                    paso.icon,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp).size(28.dp)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(paso.title, textAlign = TextAlign.Center)
                Spacer(Modifier.height(10.dp))
                TutorialProgressDots(total = pasos.size, selected = pasoActual)
            }
        },
        text = {
            TutorialStepContent(step = paso)
        },
        confirmButton = {
            Button(
                onClick = {
                    if (esUltimoPaso) {
                        onDismiss()
                    } else {
                        pasoActual += 1
                    }
                }
            ) {
                Text(if (esUltimoPaso) "Empezar" else "Siguiente")
            }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (pasoActual > 0) {
                    TextButton(onClick = { pasoActual -= 1 }) {
                        Text("Atras")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Saltar")
                }
            }
        }
    )
}

private data class OnboardingStep(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val bullets: List<String>
)

@Composable
private fun TutorialStepContent(step: OnboardingStep) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = step.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                step.bullets.forEach { bullet ->
                    TutorialBullet(text = bullet)
                }
            }
        }
    }
}

@Composable
private fun TutorialBullet(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            modifier = Modifier.padding(top = 5.dp),
            color = MaterialTheme.colorScheme.tertiary,
            shape = CircleShape
        ) {
            Spacer(Modifier.size(7.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TutorialProgressDots(total: Int, selected: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { index ->
            Surface(
                modifier = Modifier.size(width = if (index == selected) 22.dp else 8.dp, height = 8.dp),
                color = if (index == selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                shape = CircleShape
            ) {}
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarPersonaPantalla(navController: NavHostController, viewModel: PersonasViewModel, modo: DebtMode) {
    var nombre by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var montoInteresAgregado by remember { mutableStateOf("") }
    var mostrarDialogoInteres by remember { mutableStateOf(false) }
    var duracionCantidad by remember { mutableStateOf("") }
    var duracionPlazo by remember { mutableStateOf("Meses") }
    var aplicacionInteres by remember { mutableStateOf("Mensual") }
    var interesAgregado by remember { mutableDoubleStateOf(0.0) }
    var duracionPlazoPago by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(modo.newDebtTitle()) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Registro rápido",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        modo.formSubtitle(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                    )
                }
            }

            DebtFormTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = modo.nameFieldLabel(),
                icon = Icons.Filled.Person,
                keyboardType = KeyboardType.Text
            )

            DebtFormTextField(
                value = monto,
                onValueChange = { monto = it },
                label = "Monto",
                icon = Icons.Filled.AttachMoney,
                keyboardType = KeyboardType.Decimal
            )

            OutlinedButtonLike(
                text = if (interesAgregado > 0.0) "Interés agregado: ${formatCurrency(interesAgregado * duracionPlazoPago)}" else "Calcular interés",
                icon = Icons.Filled.Percent,
                onClick = { mostrarDialogoInteres = true }
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    val montoDouble = monto.toDoubleOrNull() ?: return@Button
                    if (nombre.isBlank()) return@Button

                    val nuevaPersona = DebtEntity(
                        nombre = nombre.trim(),
                        totalDeuda = montoDouble,
                        restante = montoDouble,
                        tasaInteres = interesAgregado,
                        plazoPagos = duracionPlazoPago,
                        saldada = false,
                        tipoInteres = aplicacionInteres,
                        modo = modo.storageValue
                    )
                    viewModel.agregarPersona(nuevaPersona)
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = nombre.isNotBlank() && monto.toDoubleOrNull() != null
            ) {
                Icon(Icons.Filled.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Guardar deuda")
            }
        }
    }

    if (mostrarDialogoInteres) {
        InterestDialog(
            monto = monto,
            onMontoChange = { monto = it },
            montoInteresAgregado = montoInteresAgregado,
            onMontoInteresChange = { montoInteresAgregado = it },
            duracionCantidad = duracionCantidad,
            onDuracionCantidadChange = { duracionCantidad = it },
            duracionPlazo = duracionPlazo,
            onDuracionPlazoChange = { duracionPlazo = it },
            aplicacionInteres = aplicacionInteres,
            onAplicacionInteresChange = { aplicacionInteres = it },
            onDismiss = { mostrarDialogoInteres = false },
            onSave = {
                val interes = montoInteresAgregado.toDoubleOrNull()
                val duracion = if (aplicacionInteres == "Una sola vez") 1 else duracionCantidad.toIntOrNull()
                if (interes != null && duracion != null) {
                    interesAgregado = interes
                    duracionPlazoPago = duracion
                    mostrarDialogoInteres = false
                }
            }
        )
    }
}

@Composable
private fun DebtFormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = { Icon(icon, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun OutlinedButtonLike(text: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(text, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun InterestDialog(
    monto: String,
    onMontoChange: (String) -> Unit,
    montoInteresAgregado: String,
    onMontoInteresChange: (String) -> Unit,
    duracionCantidad: String,
    onDuracionCantidadChange: (String) -> Unit,
    duracionPlazo: String,
    onDuracionPlazoChange: (String) -> Unit,
    aplicacionInteres: String,
    onAplicacionInteresChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val opciones = listOf("Días", "Meses", "Años")
    val opcionesInteres = listOf("Una sola vez", "Diario", "Semanal", "Mensual", "Anual")
    var expanded by remember { mutableStateOf(false) }
    var expandedInteres by remember { mutableStateOf(false) }

    LaunchedEffect(aplicacionInteres) {
        if (aplicacionInteres == "Una sola vez") {
            onDuracionCantidadChange("1")
            onDuracionPlazoChange("Días")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Percent, contentDescription = null) },
        title = { Text("Registrar interés") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DebtFormTextField(
                    value = monto,
                    onValueChange = onMontoChange,
                    label = "Monto base",
                    icon = Icons.Filled.AttachMoney,
                    keyboardType = KeyboardType.Decimal
                )

                SelectorBox(
                    label = "Se aplica",
                    value = aplicacionInteres,
                    expanded = expandedInteres,
                    onExpandedChange = { expandedInteres = it },
                    options = opcionesInteres,
                    onOptionSelected = onAplicacionInteresChange
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = duracionCantidad,
                        onValueChange = onDuracionCantidadChange,
                        label = { Text("Duración") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = aplicacionInteres != "Una sola vez",
                        leadingIcon = { Icon(Icons.Filled.Schedule, contentDescription = null) },
                        shape = RoundedCornerShape(16.dp)
                    )
                    SelectorBox(
                        label = "Plazo",
                        value = duracionPlazo,
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        options = opciones,
                        onOptionSelected = onDuracionPlazoChange,
                        modifier = Modifier.weight(1f),
                        enabled = aplicacionInteres != "Una sola vez"
                    )
                }

                DebtFormTextField(
                    value = montoInteresAgregado,
                    onValueChange = onMontoInteresChange,
                    label = "Interés por periodo",
                    icon = Icons.Filled.AttachMoney,
                    keyboardType = KeyboardType.Decimal
                )
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun SelectorBox(
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

fun formatCurrency(value: Double): String {
    val locale = Locale.Builder().setLanguage("es").setRegion("MX").build()
    return NumberFormat.getCurrencyInstance(locale).format(value)
}

private fun DebtMode.screenTitle(): String =
    if (this == DebtMode.POR_PAGAR) "Deudas que debes" else "Deudas por cobrar"

private fun DebtMode.pendingSummaryTitle(): String =
    if (this == DebtMode.POR_PAGAR) "Pendiente por pagar" else "Pendiente por cobrar"

private fun DebtMode.totalSummaryLabel(): String =
    if (this == DebtMode.POR_PAGAR) "Total debido" else "Total prestado"

private fun DebtMode.activeSectionTitle(): String =
    if (this == DebtMode.POR_PAGAR) "Tus deudas activas" else "Deudas activas"

private fun DebtMode.addActionText(): String =
    if (this == DebtMode.POR_PAGAR) "Nueva por pagar" else "Nueva por cobrar"

private fun DebtMode.addButtonText(): String =
    if (this == DebtMode.POR_PAGAR) "Agregar deuda que debo" else "Agregar deuda por cobrar"

private fun DebtMode.emptyTitle(): String =
    if (this == DebtMode.POR_PAGAR) "Aún no registras deudas que debes" else "Aún no hay deudas por cobrar"

private fun DebtMode.emptyDescription(): String =
    if (this == DebtMode.POR_PAGAR) {
        "Agrega a quién le debes, el monto y los pagos que vas haciendo para llevar tu saldo al día."
    } else {
        "Agrega tu primera deuda por cobrar y empieza a ver pagos, saldos e historial en un solo lugar."
    }

private fun DebtMode.newDebtTitle(): String =
    if (this == DebtMode.POR_PAGAR) "Nueva deuda que debes" else "Nueva deuda por cobrar"

private fun DebtMode.formSubtitle(): String =
    if (this == DebtMode.POR_PAGAR) {
        "Captura a quién le debes, el monto y opcionalmente un interés fijo."
    } else {
        "Captura el deudor, el monto y opcionalmente un interés fijo."
    }

private fun DebtMode.nameFieldLabel(): String =
    if (this == DebtMode.POR_PAGAR) "A quién le debes" else "Quién te debe"
