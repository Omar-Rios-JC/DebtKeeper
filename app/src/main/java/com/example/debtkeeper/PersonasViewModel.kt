package com.example.debtkeeper

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.debtkeeper.data.DatabaseModule
import com.example.debtkeeper.data.DebtEntity
import com.example.debtkeeper.data.PaymentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

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
            debtDao.insert(persona)
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



}
