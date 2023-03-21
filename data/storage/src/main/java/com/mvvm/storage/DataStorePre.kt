package com.mvvm.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Created by yan_x
 * @date 2022/12/10/010 11:47
 * @description
 */
class DataStorePre constructor(tableName: String) {
    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = tableName)

    suspend fun <T> saveData(context: Context, key: String?, value: T?) {
        if (key.isNullOrBlank()) return
        if (value == null) return
        context.dataStore.edit {
            when (value) {
                is String -> {
                    it[stringPreferencesKey(key)] = value
                }
                is Double -> {
                    it[doublePreferencesKey(key)] = value
                }
                is Long -> {
                    it[longPreferencesKey(key)] = value
                }
                is Boolean -> {
                    it[booleanPreferencesKey(key)] = value
                }
                is Int -> {
                    it[intPreferencesKey(key)] = value
                }
                is Float -> {
                    it[floatPreferencesKey(key)] = value
                }
            }
        }
    }


    inline fun <reified T> getData(context: Context, key: String?, default: T? = null) : Flow<T?> {
        if (key.isNullOrBlank()) return flow { }
        val preferenceKey = when (T::class.java) {
            java.lang.Integer::class.java -> {
                intPreferencesKey(key)
            }
            java.lang.Long::class.java -> {
                longPreferencesKey(key)
            }
            java.lang.Double::class.java -> {
                doublePreferencesKey(key)
            }
            java.lang.Boolean::class.java -> {
                booleanPreferencesKey(key)
            }
            java.lang.Float::class.java -> {
                floatPreferencesKey(key)
            }
            java.lang.String::class.java -> {
                stringPreferencesKey(key)
            }
            else -> {
                return flow { }
            }
        }
        return context.dataStore.data.map { preferences ->
            preferences[preferenceKey] ?: default
        } as? Flow<T?> ?: flow { }
    }

}