package com.example.debtkeeper

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

object UserPreferences {

    private val MOSTRAR_SALDADAS = booleanPreferencesKey("mostrar_saldadas")
    private val TUTORIAL_COMPLETADO = booleanPreferencesKey("tutorial_completado")

    fun mostrarSaldadas(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[MOSTRAR_SALDADAS] ?: true
        }

    suspend fun setMostrarSaldadas(context: Context, valor: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[MOSTRAR_SALDADAS] = valor
        }
    }

    fun tutorialCompletado(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[TUTORIAL_COMPLETADO] ?: false
        }

    suspend fun setTutorialCompletado(context: Context, valor: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[TUTORIAL_COMPLETADO] = valor
        }
    }
}