package com.example.debtkeeper

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.debtkeeper.data.DebtEntity
import com.example.debtkeeper.ui.theme.DebtKeeperTheme

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch


// -------------------- MAIN ACTIVITY -------------------
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

// -------------------- PANTALLA LISTA ------------------
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ListaPantalla(
    navController: NavHostController,
    viewModel: PersonasViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ✅ Opción A: SIN "by"
    val personas = viewModel.listaPersonas

    // ✅ Agrupar por nombre
    val personasAgrupadas = personas.groupBy { it.nombre }

    // ✅ Preferencia del switch (DataStore)
    val mostrarSaldadasState =
        UserPreferences.mostrarSaldadas(context)
            .collectAsState(initial = true)
    val mostrarSaldadas = mostrarSaldadasState.value

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("agregar") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            Text(
                "Personas que te deben dinero",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(16.dp))

            // -------- DEUDAS ACTIVAS --------
            Text("Deudas activas", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            LazyColumn {
                personasAgrupadas.forEach { (nombre, deudas) ->

                    val activas = deudas.filter { !it.saldada }
                    if (activas.isNotEmpty()) {

                        item {
                            Text(nombre, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                        }

                        items(activas) { deuda ->
                            PersonaCard(
                                persona = deuda,
                                viewModel = viewModel
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // -------- SWITCH --------
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Mostrar préstamos saldados")
                Spacer(Modifier.width(8.dp))

                Switch(
                    checked = mostrarSaldadas,
                    onCheckedChange = { valor ->
                        scope.launch {
                            UserPreferences.setMostrarSaldadas(context, valor)
                        }
                    }
                )
            }

            Spacer(Modifier.height(12.dp))

            // -------- SALDADAS --------
            if (mostrarSaldadas) {
                Text("Préstamos saldados", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                LazyColumn {
                    personasAgrupadas.forEach { (nombre, deudas) ->

                        val saldadas = deudas.filter { it.saldada }
                        if (saldadas.isNotEmpty()) {

                            item {
                                Text(nombre, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))
                            }

                            items(saldadas) { deuda ->
                                PersonaCard(
                                    persona = deuda,
                                    viewModel = viewModel
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}



// -------------------- PANTALLA AGREGAR PERSONA -------
@Composable
fun AgregarPersonaPantalla(navController: NavHostController, viewModel: PersonasViewModel) {

    var nombre by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Agregar nueva persona", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = monto,
            onValueChange = { monto = it },
            label = { Text("Monto de la deuda") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {

                val montoDouble = monto.toDoubleOrNull() ?: return@Button

                val nuevaPersona = DebtEntity(
                    nombre = nombre,
                    totalDeuda = montoDouble,
                    restante = montoDouble,
                    saldada = false
                )

                viewModel.agregarPersona(nuevaPersona)
                navController.popBackStack()
                if (nombre.isBlank()) return@Button

            }
        ) {
            Text("Agregar")
        }

    }
}
