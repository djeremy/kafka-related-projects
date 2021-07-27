package com.djeremy.process.monitor.adapter.store.mongo.ddl

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index

inline fun <reified T> MongoTemplate.deleteIndexes(names: List<String>) {
    val existingIndexes = indexOps(T::class.java).indexInfo
    names.forEach { indexName ->
        existingIndexes.filter { it.name.equals(indexName, true) }.forEach {
            indexOps(T::class.java).dropIndex(it.name)
        }
    }
}

inline fun <reified T> MongoTemplate.ensureIndex(index: Index) {
    indexOps(T::class.java).ensureIndex(index)
}