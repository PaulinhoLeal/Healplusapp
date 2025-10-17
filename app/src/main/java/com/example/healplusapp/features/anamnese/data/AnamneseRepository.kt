package com.example.healplusapp.features.anamnese.data

import android.content.ContentValues
import android.content.Context
import com.example.healplusapp.features.anamnese.model.Anamnese

class AnamneseRepository(context: Context) {
    private val dbHelper = AnamneseDbHelper(context)

    fun insert(anamnese: Anamnese): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(AnamneseDbHelper.COL_NOME, anamnese.nomeCompleto)
            put(AnamneseDbHelper.COL_DATA_CONSULTA, anamnese.dataConsulta)
            put(AnamneseDbHelper.COL_LOCALIZACAO, anamnese.localizacao)
            put(AnamneseDbHelper.COL_JSON, anamnese.dadosJson)
        }
        return db.insert(AnamneseDbHelper.TABLE, null, values)
    }

    fun update(anamnese: Anamnese): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(AnamneseDbHelper.COL_NOME, anamnese.nomeCompleto)
            put(AnamneseDbHelper.COL_DATA_CONSULTA, anamnese.dataConsulta)
            put(AnamneseDbHelper.COL_LOCALIZACAO, anamnese.localizacao)
            put(AnamneseDbHelper.COL_JSON, anamnese.dadosJson)
        }
        return db.update(
            AnamneseDbHelper.TABLE,
            values,
            "${AnamneseDbHelper.COL_ID}=?",
            arrayOf(anamnese.id?.toString() ?: "0")
        )
    }

    fun delete(id: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            AnamneseDbHelper.TABLE,
            "${AnamneseDbHelper.COL_ID}=?",
            arrayOf(id.toString())
        )
    }

    fun getAll(): List<Anamnese> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(AnamneseDbHelper.TABLE, null, null, null, null, null, "${AnamneseDbHelper.COL_ID} DESC")
        val result = mutableListOf<Anamnese>()
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(AnamneseDbHelper.COL_ID))
                val nome = it.getString(it.getColumnIndexOrThrow(AnamneseDbHelper.COL_NOME))
                val dataConsulta = it.getString(it.getColumnIndexOrThrow(AnamneseDbHelper.COL_DATA_CONSULTA))
                val localizacao = it.getString(it.getColumnIndexOrThrow(AnamneseDbHelper.COL_LOCALIZACAO))
                val json = it.getString(it.getColumnIndexOrThrow(AnamneseDbHelper.COL_JSON))
                result.add(
                    Anamnese(
                        id = id,
                        nomeCompleto = nome,
                        dataConsulta = dataConsulta,
                        localizacao = localizacao,
                        dadosJson = json
                    )
                )
            }
        }
        return result
    }

    fun getById(id: Long): Anamnese? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            AnamneseDbHelper.TABLE,
            null,
            "${AnamneseDbHelper.COL_ID}=?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                val nome = it.getString(it.getColumnIndexOrThrow(AnamneseDbHelper.COL_NOME))
                val dataConsulta = it.getString(it.getColumnIndexOrThrow(AnamneseDbHelper.COL_DATA_CONSULTA))
                val localizacao = it.getString(it.getColumnIndexOrThrow(AnamneseDbHelper.COL_LOCALIZACAO))
                val json = it.getString(it.getColumnIndexOrThrow(AnamneseDbHelper.COL_JSON))
                return Anamnese(id, nome, dataConsulta, localizacao, json)
            }
        }
        return null
    }
}


