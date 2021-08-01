package com.djeremy.process.monitor.adapter.store.mongo.ddl

import com.djeremy.process.monitor.adapter.store.mongo.ProcessInstanceStateDao
import org.bson.Document
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.index.PartialIndexFilter
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.isEqualTo

fun byInstanceAndVersionUnique(): Index = CompoundIndexDefinition(
    Document(
        mapOf<String, Any>(
            ProcessInstanceStateDao::instance.name to 1,
            ProcessInstanceStateDao::version.name to 1
        )
    )
).named("byInstanceAndVersionUnique")
    .unique()
    .background()

fun byIsAdmittedFalseAndStartedAt(): Index =
    CompoundIndexDefinition(
        Document(
            mapOf<String, Any>(
                ProcessInstanceStateDao::startedAt.name to 1
            )
        )
    ).named("byIsAdmittedFalseAndStartedAt")
        .background()
        .partial(
            PartialIndexFilter.of(
                Criteria(
                    "stage.isAdmitted"
                ).isEqualTo(false)
            )
        )