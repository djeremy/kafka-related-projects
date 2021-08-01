package com.djeremy.process.monitor.adapter.streams

import com.djeremy.avro.business.process.monitor.Reference
import java.time.Instant
import java.util.UUID
import com.djeremy.avro.business.process.monitor.ProcessStep

fun `random ProcessStep`() = ProcessStep("configurationId",
        "ignored",
        UUID.randomUUID().toString(),
        listOf(Reference(UUID.randomUUID().toString(), "undefined")),
        false,
        Instant.now())
