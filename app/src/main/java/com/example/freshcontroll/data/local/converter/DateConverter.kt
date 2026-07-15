package com.example.freshcontroll.data.local.converter

import androidx.room.TypeConverter
import java.util.Date

/**
 * IMPORTANTE:
 * Dado que tus entidades usan `Long` para los timestamps, Room no necesita usar esta clase.
 * Este código queda como referencia por si en un futuro decides cambiar tus @Entity
 * para que usen `java.util.Date` directamente, o si necesitas reutilizar esta lógica
 * en tus Mappers de la capa Domain.
 */
class DateConverter {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}