package com.djeremy.process.monitor.domain.streams

import com.djeremy.avro.business.process.monitor.ProcessStep
import com.djeremy.avro.business.process.monitor.v2.Reference
import java.time.Instant
import java.util.UUID
import com.djeremy.avro.business.process.monitor.v2.ProcessStep as ProcessStepV2

fun `random ProcessStepV2`() = ProcessStepV2("configurationId",
        "ignored",
        UUID.randomUUID().toString(),
        listOf(Reference(UUID.randomUUID().toString(), "undefined")),
        false,
        Instant.now())

fun `random ProcessStep`() = ProcessStep("configurationId",
        "ignored",
        UUID.randomUUID().toString(),
        listOf(UUID.randomUUID().toString()),
        Instant.now())