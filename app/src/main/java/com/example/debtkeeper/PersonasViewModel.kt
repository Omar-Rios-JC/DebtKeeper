package com.example.debtkeeper

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.debtkeeper.data.DatabaseModule
import com.example.debtkeeper.data.DebtEntity
import com.example.debtkeeper.data.PaymentEntity
import com.example.debtkeeper.model.Deuda
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

//@RequiresApi(Build.VERSION_CODES.O)
class PersonasViewModel(application: Application) : AndroidViewModel(application) {

    // --------- DAOs ----------
    private val dao = DatabaseModule
        .getDatabase(application)
        .debtDao()
    private val debtDao =
        DatabaseModule.getDatabase(application).debtDao()

    private val paymentDao =
        DatabaseModule.getDatabase(application).paymentDao()



    // --------- LISTA DE DEUDAS ----------
    var listaPersonas by mutableStateOf<List<DebtEntity>>(emptyList())
        private set

    init {
        cargarPersonas()
    }

    private fun cargarPersonas() {
        viewModelScope.launch {
            listaPersonas = debtDao.getAll()
        }
    }

    // --------- CRUD DE DEUDAS ----------
    fun agregarPersona(persona: DebtEntity) {
        viewModelScope.launch {
            val deudaFinal = calcularInteres(persona)
            debtDao.insert(deudaFinal)
            cargarPersonas()
        }
    }

    fun actualizarPersona(persona: DebtEntity) {
        viewModelScope.launch {
            debtDao.update(persona)
            cargarPersonas()
        }
    }

    fun eliminarPersona(persona: DebtEntity) {
        viewModelScope.launch {
            debtDao.delete(persona)
            cargarPersonas()
        }
    }

    // --------- PAGOS ----------
    fun registrarPago(
        deuda: DebtEntity,
        monto: Double,
        fecha: String
    ) {
        viewModelScope.launch {

            // 1️⃣ Guardar el pago
            val pago = PaymentEntity(
                deudaId = deuda.id,
                monto = monto,
                fecha = fecha
            )
            paymentDao.insert(pago)

            // 2️⃣ Actualizar la deuda
            val nuevoRestante = deuda.restante - monto

            val deudaActualizada = deuda.copy(
                restante = nuevoRestante,
                saldada = nuevoRestante <= 0
            )

            debtDao.update(deudaActualizada)

            // 3️⃣ Refrescar lista
            cargarPersonas()
        }
    }

    fun obtenerPagosPorDeuda(deudaId: Int): Flow<List<PaymentEntity>> {
        return paymentDao.getPagosPorDeuda(deudaId)
    }

    fun reactivarDeuda(persona: DebtEntity, nuevoMonto: Double) {
        viewModelScope.launch {

            val deudaReactivada = persona.copy(
                totalDeuda = nuevoMonto,
                restante = nuevoMonto,
                saldada = false
            )

            dao.update(deudaReactivada)
            listaPersonas = dao.getAll()
        }
    }

    // --------- INTERESES ----------
    fun calcularInteres(deuda: DebtEntity): DebtEntity
    {
        if (deuda.tasaInteres <= 0.0) return deuda

        if(deuda.interesAplicado) return deuda

        if(deuda.tipoInteres == "Una sola Vez")
        {
            deuda.plazoPagos == 1
        }

        val interesAcumulado = deuda.tasaInteres * deuda.plazoPagos

        return deuda.copy(
            totalDeuda = deuda.totalDeuda + interesAcumulado,
            restante = deuda.restante + interesAcumulado,
            interesAcumulado = interesAcumulado,
            interesAplicado = true
        )
    }

    /*
    @RequiresApi(Build.VERSION_CODES.O)
    fun aplicarIntereses(deuda: DebtEntity): DebtEntity
    {
        val hoy = LocalDate.now().plusMonths(6)

        val fechaBase = deuda.ultimaActualizacionInteres ?: deuda.fechaCreacion

        val aplicar = when (deuda.tipoInteres)
        {
            "Una sola vez" ->
                {
                deuda.ultimaActualizacionInteres == null
                }
            "Diario" -> ChronoUnit.DAYS.between(fechaBase, hoy) >=1
            "Semanal" -> ChronoUnit.WEEKS.between(fechaBase, hoy) >=1
            "Mensual" -> ChronoUnit.MONTHS.between(fechaBase, hoy) >=1
            "Anual" -> ChronoUnit.YEARS.between(fechaBase, hoy) >=1
            else -> false
        }

        if (!aplicar) return deuda

        // Calcular nueva fecha de ciclo
        val nuevaFecha = when (deuda.tipoInteres)
        {
            "Una sola vez" -> hoy
            "Diario" -> fechaBase.plusDays(1)
            "Semanal" -> fechaBase.plusWeeks(1)
            "Mensual" -> fechaBase.plusMonths(1)
            "Anual" -> fechaBase.plusYears(1)
            else -> fechaBase
        }

        val nuevoInteresAcumulado = deuda.interesAcumulado + deuda.tasaInteres

        return deuda.copy(
            restante = deuda.restante + deuda.tasaInteres,
            interesAcumulado = nuevoInteresAcumulado,
            ultimaActualizacionInteres = nuevaFecha,
            tipoInteres = if (deuda.tipoInteres == "Una sola vez") "Ninguno" else deuda.tipoInteres
        )
    }
    */

}
