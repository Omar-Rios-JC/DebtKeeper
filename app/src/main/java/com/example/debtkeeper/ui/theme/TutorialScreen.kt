package com.example.debtkeeper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TutorialBackground = Color(0xFF121212)
private val TutorialSurface = Color(0xFF1E1E1E)
private val TutorialAccent = Color(0xFF00BFA5)
private val TutorialTextPrimary = Color(0xFFFFFFFF)
private val TutorialTextSecondary = Color(0xFFB0B0B0)

@Composable
fun TutorialScreen(
    onDismiss: () -> Unit
) {
    Scaffold(
        containerColor = TutorialBackground,
        topBar = {
            Text(
                text = "¿Cómo usar DebtKeeper?",
                style = MaterialTheme.typography.headlineMedium,
                color = TutorialTextPrimary,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 64.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
            )
        },
        bottomBar = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 48.dp, top = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TutorialAccent,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Text(
                    text = "¡Entendido, empezar!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(tutorialSteps) { step ->
                    TutorialStepCard(step)
                }
            }
        }
    }
}

@Composable
fun TutorialStepCard(step: TutorialStep) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TutorialSurface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(TutorialAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = step.icon,
                    contentDescription = null,
                    tint = TutorialAccent,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column {
                Text(
                    text = step.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TutorialTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = step.description,
                    fontSize = 15.sp,
                    color = TutorialTextSecondary,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

data class TutorialStep(
    val title: String,
    val description: String,
    val icon: ImageVector
)

val tutorialSteps = listOf(
    TutorialStep(
        title = "Agregar Deudor",
        description = "Toca el botón '+' en la pantalla principal para registrar una nueva persona.",
        icon = Icons.Default.Add
    ),
    TutorialStep(
        title = "Registrar Pago",
        description = "Usa el campo de monto y el calendario en la tarjeta de la persona para abonar.",
        icon = Icons.Default.CheckCircle
    ),
    TutorialStep(
        title = "Ver Detalles",
        description = "Toca el icono de lista para ver el historial completo de abonos y fechas.",
        icon = Icons.Default.List
    ),
    TutorialStep(
        title = "Editar Información",
        description = "Usa el icono de lápiz para modificar el nombre o el monto total de la deuda.",
        icon = Icons.Default.Edit
    ),
    TutorialStep(
        title = "Eliminar Deudor",
        description = "Si la deuda está saldada, usa el icono de basura para borrar el registro.",
        icon = Icons.Default.Delete
    )
)