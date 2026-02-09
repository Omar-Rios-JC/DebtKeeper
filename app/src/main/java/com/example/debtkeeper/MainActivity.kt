package com.example.debtkeeper

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.debtkeeper.data.DebtEntity
import com.example.debtkeeper.ui.theme.DebtKeeperTheme
import kotlinx.coroutines.launch

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
                    composable("agregar") {
                        AgregarPersonaPantalla(navController, viewModel)
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
    val personasAgrupadas = personas.groupBy { it.nombre }

    val mostrarSaldadas = UserPreferences.mostrarSaldadas(context).collectAsState(initial = true).value
    val tutorialCompletado = UserPreferences.tutorialCompletado(context).collectAsState(initial = true).value

    var mostrarTutorial by remember { mutableStateOf(false) }

    LaunchedEffect(tutorialCompletado) {
        if (!tutorialCompletado) {
            mostrarTutorial = true
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("DebtKeeper", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("agregar") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Deuda")
            }
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text("Ver saldados", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = mostrarSaldadas,
                    onCheckedChange = { valor ->
                        scope.launch { UserPreferences.setMostrarSaldadas(context, valor) }
                    }
                )
            }

            Spacer(Modifier.height(8.dp))

            if (personas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No tienes deudas registradas.\nPulsa + para agregar una.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    personasAgrupadas.forEach { (nombre, deudas) ->
                        val activas = deudas.filter { !it.saldada }

                        if (activas.isNotEmpty()) {
                            item {
                                Text(nombre, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(4.dp))
                            }
                            items(activas) { deuda ->
                                PersonaCard(persona = deuda, viewModel = viewModel)
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }

                    if (mostrarSaldadas) {
                        item {
                            Spacer(Modifier.height(16.dp))
                            Text("Historial (Saldados)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.height(8.dp))
                        }

                        personasAgrupadas.forEach { (_, deudas) ->
                            val saldadas = deudas.filter { it.saldada }
                            if (saldadas.isNotEmpty()) {
                                items(saldadas) { deuda ->
                                    PersonaCard(persona = deuda, viewModel = viewModel)
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TutorialDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("¡Bienvenido a DebtKeeper!", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))

                Text(
                    "Aquí podrás llevar el control de quien te debe dinero de forma sencilla:",
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))

                Text("Pulsa el botón + para agregar una nueva deuda.")
                Spacer(Modifier.height(8.dp))
                Text("Toca una tarjeta para ver opciones como: Abonar o Eliminar.")
                Spacer(Modifier.height(24.dp))

                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("¡Entendido!")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarPersonaPantalla(navController: NavHostController, viewModel: PersonasViewModel) {
    var nombre by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nueva Deuda") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Text("x")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("¿Quién te debe?") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = monto,
                onValueChange = { monto = it },
                label = { Text("Monto ($)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val montoDouble = monto.toDoubleOrNull() ?: return@Button
                    if (nombre.isBlank()) return@Button

                    val nuevaPersona = DebtEntity(
                        nombre = nombre,
                        totalDeuda = montoDouble,
                        restante = montoDouble,
                        saldada = false
                    )
                    viewModel.agregarPersona(nuevaPersona)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Guardar Deuda")
            }
        }
    }
}