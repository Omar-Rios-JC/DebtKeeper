// README
// Proyecto: CalculadoraDeDeudas
// Lenguaje: Kotlin (Android)
// Arquitectura sencilla: Room + ViewModel + LiveData + RecyclerView
// Incluye: Entidades, DAOs, Database, Repositorios, ViewModels, Adapters, Activities y XMLs.

/* build.gradle (app) - dependencias principales (añádelas al module:app)

plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'kotlin-kapt'
}

android {
    compileSdk 34

    defaultConfig {
        applicationId "com.example.calculadoradedeudas"
        minSdk 21
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }

    buildFeatures {
        viewBinding true
    }
}

dependencies {
    implementation "org.jetbrains.kotlin:kotlin-stdlib:1.9.10"
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.7.0'
    implementation 'com.google.android.material:material:1.9.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.2.0'

    // RecyclerView
    implementation 'androidx.recyclerview:recyclerview:1.3.0'

    // Room
    implementation 'androidx.room:room-runtime:2.6.1'
    kapt 'androidx.room:room-compiler:2.6.1'
    implementation 'androidx.room:room-ktx:2.6.1'

    // Lifecycle / ViewModel
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.6.2'

    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'

}
*/

// -------------------------
// 1) Entidades (Room)
// -------------------------
package com.example.calculadoradedeudas.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "personas")
data class Persona(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val totalPrestado: Double,
    val fechaPrestamo: Long
)

package com.example.calculadoradedeudas.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pagos")
data class Pago(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val personaId: Int,
    val monto: Double,
    val fechaPago: Long
)

// -------------------------
// 2) DAOs
// -------------------------
package com.example.calculadoradedeudas.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PersonaDao {
    @Insert
    suspend fun insertar(persona: Persona): Long

    @Update
    suspend fun actualizar(persona: Persona)

    @Delete
    suspend fun eliminar(persona: Persona)

    @Query("SELECT * FROM personas ORDER BY nombre ASC")
    fun obtenerTodas(): LiveData<List<Persona>>

    @Query("SELECT * FROM personas WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): Persona?
}

package com.example.calculadoradedeudas.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PagoDao {
    @Insert
    suspend fun insertar(pago: Pago): Long

    @Update
    suspend fun actualizar(pago: Pago)

    @Delete
    suspend fun eliminar(pago: Pago)

    @Query("SELECT * FROM pagos WHERE personaId = :personaId ORDER BY fechaPago DESC")
    fun obtenerPorPersona(personaId: Int): LiveData<List<Pago>>

    @Query("SELECT SUM(monto) FROM pagos WHERE personaId = :personaId")
    suspend fun sumaPagos(personaId: Int): Double?
}

// -------------------------
// 3) Base de datos
// -------------------------
package com.example.calculadoradedeudas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Persona::class, Pago::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personaDao(): PersonaDao
    abstract fun pagoDao(): PagoDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "deudas_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// -------------------------
// 4) Repositorios
// -------------------------
package com.example.calculadoradedeudas.repo

import android.content.Context
import com.example.calculadoradedeudas.data.*

class PersonaRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val dao = db.personaDao()

    fun obtenerTodas() = dao.obtenerTodas()

    suspend fun insertar(persona: Persona) = dao.insertar(persona)
    suspend fun actualizar(persona: Persona) = dao.actualizar(persona)
    suspend fun eliminar(persona: Persona) = dao.eliminar(persona)
    suspend fun obtenerPorId(id: Int) = dao.obtenerPorId(id)
}

package com.example.calculadoradedeudas.repo

import android.content.Context
import com.example.calculadoradedeudas.data.*

class PagoRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val dao = db.pagoDao()

    fun obtenerPorPersona(personaId: Int) = dao.obtenerPorPersona(personaId)
    suspend fun insertar(pago: Pago) = dao.insertar(pago)
    suspend fun eliminar(pago: Pago) = dao.eliminar(pago)
    suspend fun sumaPagos(personaId: Int) = dao.sumaPagos(personaId) ?: 0.0
}

// -------------------------
// 5) ViewModels
// -------------------------
package com.example.calculadoradedeudas.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.calculadoradedeudas.data.Persona
import com.example.calculadoradedeudas.data.Pago
import com.example.calculadoradedeudas.repo.PagoRepository
import com.example.calculadoradedeudas.repo.PersonaRepository
import kotlinx.coroutines.launch

class PersonaViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PersonaRepository(application)
    val todasPersonas = repo.obtenerTodas()

    fun insertar(persona: Persona) = viewModelScope.launch {
        repo.insertar(persona)
    }

    fun actualizar(persona: Persona) = viewModelScope.launch {
        repo.actualizar(persona)
    }

    fun eliminar(persona: Persona) = viewModelScope.launch {
        repo.eliminar(persona)
    }
}

class PagoViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PagoRepository(application)

    fun obtenerPagos(personaId: Int): LiveData<List<Pago>> = repo.obtenerPorPersona(personaId)

    fun insertar(pago: Pago) = viewModelScope.launch {
        repo.insertar(pago)
    }

    fun eliminar(pago: Pago) = viewModelScope.launch {
        repo.eliminar(pago)
    }

    suspend fun sumaPagos(personaId: Int) = repo.sumaPagos(personaId)
}

// -------------------------
// 6) Adapters
// -------------------------
package com.example.calculadoradedeudas.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.calculadoradedeudas.data.Persona
import com.example.calculadoradedeudas.databinding.ItemPersonaBinding
import java.text.NumberFormat
import java.util.*

class PersonaAdapter(
    private var lista: List<Persona> = emptyList(),
    private val onClick: (Persona) -> Unit
) : RecyclerView.Adapter<PersonaAdapter.VH>() {

    fun update(newList: List<Persona>) {
        lista = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemPersonaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(lista[position])
    }

    override fun getItemCount() = lista.size

    inner class VH(private val b: ItemPersonaBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(p: Persona) {
            b.tvNombre.text = p.nombre
            val nf = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
            b.tvPrestado.text = nf.format(p.totalPrestado)
            // Pagado y restante se calcularán en pantalla detalle, aquí mostraremos 0 por default
            b.root.setOnClickListener { onClick(p) }
        }
    }
}

package com.example.calculadoradedeudas.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.calculadoradedeudas.data.Pago
import com.example.calculadoradedeudas.databinding.ItemPagoBinding
import java.text.SimpleDateFormat
import java.util.*

class PagoAdapter(
    private var lista: List<Pago> = emptyList()
) : RecyclerView.Adapter<PagoAdapter.VH>() {

    fun update(newList: List<Pago>) {
        lista = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemPagoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(lista[position])
    }

    override fun getItemCount() = lista.size

    inner class VH(private val b: ItemPagoBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(p: Pago) {
            b.tvMonto.text = java.text.NumberFormat.getCurrencyInstance(Locale("es","MX")).format(p.monto)
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            b.tvFecha.text = sdf.format(Date(p.fechaPago))
        }
    }
}

// -------------------------
// 7) Activities (UI)
// -------------------------
package com.example.calculadoradedeudas

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calculadoradedeudas.databinding.ActivityMainBinding
import com.example.calculadoradedeudas.data.Persona
import com.example.calculadoradedeudas.ui.PersonaAdapter
import com.example.calculadoradedeudas.viewmodel.PersonaViewModel
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val vm: PersonaViewModel by viewModels()
    private lateinit var adapter: PersonaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = PersonaAdapter(emptyList()) { persona ->
            val i = Intent(this, DetallePersonaActivity::class.java)
            i.putExtra("personaId", persona.id)
            startActivity(i)
        }

        binding.rvPersonas.layoutManager = LinearLayoutManager(this)
        binding.rvPersonas.adapter = adapter

        vm.todasPersonas.observe(this) { lista ->
            adapter.update(lista)
            binding.emptyView.visibility = if (lista.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }

        binding.fabAgregar.setOnClickListener {
            showAgregarPersonaDialog()
        }
    }

    private fun showAgregarPersonaDialog() {
        val dialog = AgregarPersonaDialogFragment { nombre, monto ->
            val persona = Persona(nombre = nombre, totalPrestado = monto, fechaPrestamo = Date().time)
            vm.insertar(persona)
        }
        dialog.show(supportFragmentManager, "agregar_persona")
    }
}

// DetallePersonaActivity
package com.example.calculadoradedeudas

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calculadoradedeudas.databinding.ActivityDetallePersonaBinding
import com.example.calculadoradedeudas.data.Pago
import com.example.calculadoradedeudas.repo.PersonaRepository
import com.example.calculadoradedeudas.ui.PagoAdapter
import com.example.calculadoradedeudas.viewmodel.PagoViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DetallePersonaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetallePersonaBinding
    private val pagoVm: PagoViewModel by viewModels()
    private val personaRepo by lazy { PersonaRepository(application) }
    private lateinit var adapter: PagoAdapter
    private var personaId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetallePersonaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        personaId = intent.getIntExtra("personaId", -1)
        if (personaId == -1) finish()

        adapter = PagoAdapter()
        binding.rvPagos.layoutManager = LinearLayoutManager(this)
        binding.rvPagos.adapter = adapter

        pagoVm.obtenerPagos(personaId).observe(this) { pagos ->
            adapter.update(pagos)
            calcularRestante(pagos)
            binding.tvSinPagos.visibility = if (pagos.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }

        // cargar datos de persona
        lifecycleScope.launch {
            val p = personaRepo.obtenerPorId(personaId)
            p?.let { persona ->
                binding.tvNombre.text = persona.nombre
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.tvFechaPrestamo.text = sdf.format(Date(persona.fechaPrestamo))
                binding.tvPrestado.text = java.text.NumberFormat.getCurrencyInstance(Locale("es","MX")).format(persona.totalPrestado)
            }
        }

        binding.btnAgregarPago.setOnClickListener {
            showAgregarPagoDialog()
        }
    }

    private fun showAgregarPagoDialog() {
        val dialog = AgregarPagoDialogFragment { monto ->
            val pago = Pago(personaId = personaId, monto = monto, fechaPago = Date().time)
            pagoVm.insertar(pago)
        }
        dialog.show(supportFragmentManager, "agregar_pago")
    }

    private fun calcularRestante(pagos: List<Pago>) {
        lifecycleScope.launch {
            val persona = personaRepo.obtenerPorId(personaId) ?: return@launch
            val totalPagado = pagos.sumOf { it.monto }
            val restante = persona.totalPrestado - totalPagado
            binding.tvPagado.text = java.text.NumberFormat.getCurrencyInstance(Locale("es","MX")).format(totalPagado)
            binding.tvRestante.text = java.text.NumberFormat.getCurrencyInstance(Locale("es","MX")).format(restante)
        }
    }
}

// -------------------------
// 8) DialogFragments para agregar Persona y Pago (UI helpers)
// -------------------------
package com.example.calculadoradedeudas

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.calculadoradedeudas.databinding.DialogAgregarPersonaBinding

class AgregarPersonaDialogFragment(private val onAgregar: (String, Double) -> Unit) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogAgregarPersonaBinding.inflate(LayoutInflater.from(context))
        return AlertDialog.Builder(requireContext())
            .setTitle("Agregar persona")
            .setView(binding.root)
            .setPositiveButton("Agregar") { _, _ ->
                val nombre = binding.etNombre.text.toString().trim()
                val monto = binding.etMonto.text.toString().toDoubleOrNull() ?: 0.0
                if (nombre.isNotEmpty()) onAgregar(nombre, monto)
            }
            .setNegativeButton("Cancelar", null)
            .create()
    }
}

package com.example.calculadoradedeudas

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.calculadoradedeudas.databinding.DialogAgregarPagoBinding

class AgregarPagoDialogFragment(private val onAgregar: (Double) -> Unit) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogAgregarPagoBinding.inflate(LayoutInflater.from(context))
        return AlertDialog.Builder(requireContext())
            .setTitle("Agregar pago")
            .setView(binding.root)
            .setPositiveButton("Agregar") { _, _ ->
                val monto = binding.etMonto.text.toString().toDoubleOrNull() ?: 0.0
                if (monto > 0) onAgregar(monto)
            }
            .setNegativeButton("Cancelar", null)
            .create()
    }
}

// -------------------------
// 9) Layout XMLs (strings aproximadas) - crea archivos en res/layout
// -------------------------

// activity_main.xml
/*
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">

    <androidx.coordinatorlayout.widget.CoordinatorLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/rvPersonas"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:padding="16dp"/>

        <TextView
            android:id="@+id/emptyView"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="No hay personas aún"
            android:layout_gravity="center"
            android:visibility="gone"/>

        <com.google.android.material.floatingactionbutton.FloatingActionButton
            android:id="@+id/fabAgregar"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="bottom|end"
            android:layout_margin="16dp"
            app:srcCompat="@android:drawable/ic_input_add" />

    </androidx.coordinatorlayout.widget.CoordinatorLayout>
</layout>
*/

// item_persona.xml
/*
<layout xmlns:android="http://schemas.android.com/apk/res/android">
    <com.google.android.material.card.MaterialCardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginBottom="8dp"
        android:padding="12dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">

            <TextView android:id="@+id/tvNombre" android:textSize="18sp"/>
            <TextView android:id="@+id/tvPrestado" />

        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>
</layout>
*/

// activity_detalle_persona.xml
/*
<layout xmlns:android="http://schemas.android.com/apk/res/android">
    <LinearLayout android:layout_width="match_parent" android:layout_height="match_parent" android:orientation="vertical" android:padding="16dp">

        <TextView android:id="@+id/tvNombre" android:textSize="20sp" android:textStyle="bold" />
        <TextView android:id="@+id/tvFechaPrestamo" />
        <TextView android:id="@+id/tvPrestado" />

        <LinearLayout android:orientation="horizontal" android:layout_width="match_parent" android:layout_height="wrap_content">
            <TextView android:text="Pagado:"/>
            <TextView android:id="@+id/tvPagado"/>
            <TextView android:text="Restante:"/>
            <TextView android:id="@+id/tvRestante"/>
        </LinearLayout>

        <Button android:id="@+id/btnAgregarPago" android:text="Agregar pago" />

        <TextView android:id="@+id/tvSinPagos" android:text="Sin pagos" android:visibility="gone"/>

        <androidx.recyclerview.widget.RecyclerView android:id="@+id/rvPagos" android:layout_width="match_parent" android:layout_height="wrap_content"/>
    </LinearLayout>
</layout>
*/

// item_pago.xml
/*
<layout xmlns:android="http://schemas.android.com/apk/res/android">
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="8dp">
        <TextView android:id="@+id/tvMonto" />
        <TextView android:id="@+id/tvFecha" />
    </LinearLayout>
</layout>
*/

// dialog_agregar_persona.xml
/*
<layout xmlns:android="http://schemas.android.com/apk/res/android">
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="12dp">
        <EditText android:id="@+id/etNombre" android:hint="Nombre" />
        <EditText android:id="@+id/etMonto" android:hint="Monto prestado" android:inputType="numberDecimal" />
    </LinearLayout>
</layout>
*/

// dialog_agregar_pago.xml
/*
<layout xmlns:android="http://schemas.android.com/apk/res/android">
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="12dp">
        <EditText android:id="@+id/etMonto" android:hint="Monto" android:inputType="numberDecimal" />
    </LinearLayout>
</layout>
*/

// -------------------------
// 10) Notas finales
// -------------------------
// - He usado ViewBinding en ejemplos (ActivityMainBinding, etc). Genera binding activando viewBinding en build.gradle.
// - EmptyView: en MainActivity se muestra/oculta según si la lista está vacía.
// - Para simplificar, la edición/eliminación no está implementada, puedes agregar swipeActions con ItemTouchHelper.
// - Asegúrate de crear los archivos XML correctamente y ajustar los ids para que coincidan con los bindings usados.
// - Si quieres, puedo generar los archivos exactos listos para pegar (cada .kt y cada .xml por separado) o ayudarte a mejorar la UI (cards expandibles, animaciones).

// ¡Listo! Copia el contenido, crea los archivos correspondientes en Android Studio y deberías tener una app funcional que guarda datos con Room.
