package com.djeremy.process.monitor.adapter.store.mongo.ddl

import com.djeremy.process.monitor.adapter.store.mongo.ReferenceDao
import com.djeremy.process.monitor.adapter.store.mongo.StepDao
import org.bson.Document
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition
import org.springframework.data.mongodb.core.index.Index


fun byReferenceIdAndConfiguration(): Index = CompoundIndexDefinition(
    Document(
        mapOf<String, Any>(
            StepDao::references.name + "." + ReferenceDao::referenceId.name to 1,
            StepDao::configurationId.name to 1
        )
    )
).named("byReferenceIdAndConfiguration").background()

fun byIsNewlyInstanceAssignedAndReceivedAt(): Index = CompoundIndexDefinition(
    Document(
        mapOf<String, Any>(
            StepDao::isNewlyInstanceAssigned.name to 1,
            StepDao::receivedAt.name to 1
        )
    )
).named("byIsNewlyInstanceAssignedAndReceivedAt").background().sparse()

fun byEventIdAndConfiguration(): Index = CompoundIndexDefinition(
    Document(
        mapOf<String, Any>(
            StepDao::eventId.name to 1,
            StepDao::configurationId.name to 1
        )
    )
).named("byEventIdAndConfiguration").background()
