package com.djeremy.process.monitor.domain.process

import com.djeremy.avro.UUID
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord

fun extractFieldStringFrom(schemaPath: String?, key: String, genericRecord: GenericRecord): String =
    schemaPath?.let { extractFieldStringFrom(it, genericRecord) } ?: key

fun extractFieldStringFrom(schemaPath: String, genericRecord: GenericRecord): String? {
    var nextValue: GenericRecord? = genericRecord

    val pathKeys = schemaPath.split('.')
    val lastIndex = pathKeys.size - 1

    val pathWithExtracts = pathKeys.mapIndexed { _, path ->
        if (nextValue == null) path to null
        else {
            val nextExtractedValue: Any? = runCatching { nextValue?.get(path) }.getOrNull()
            val possibleNextValue = nextExtractedValue as? GenericRecord
            nextValue = possibleNextValue
            path to possibleNextValue
        }
    }

    return if (pathWithExtracts.size == 1) {
        when (val get = runCatching { genericRecord.get(pathKeys.first()) }.getOrNull()) {
            is GenericRecord -> tryToMapPredefinedType(get)
            is String -> get
            else -> null
        }
    } else {
        val (lastValue, value) = pathWithExtracts[lastIndex]
        if (value != null) tryToMapPredefinedType(value)
        else {
            val (_, prevValue) = pathWithExtracts[lastIndex - 1]
            runCatching { prevValue?.get(lastValue) as? String }.getOrNull()
        }
    }
}

fun tryToMapPredefinedType(genericRecord: GenericRecord): String? =
    predefinedTypeToStringMappers.first { (schema, _) -> schema == genericRecord.schema }.second(genericRecord)


val predefinedTypeToStringMappers: List<Pair<Schema, (GenericRecord) -> String>> = listOf(
    UUID.`SCHEMA$` to { it -> it.get("value") as String }
)