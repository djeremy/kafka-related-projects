package com.djeremy.process.monitor.embeded.v2

import com.djeremy.process.monitor.adapter.startup.MongoIndexStartup
import com.djeremy.process.monitor.adapter.store.mongo.ProcessInstanceStateDao
import com.djeremy.process.monitor.adapter.store.mongo.StepDao
import com.djeremy.process.monitor.domain.process.models.Reference
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataMongoTest
class MongoIndexesIT {

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    @BeforeAll
    fun beforeAll() {
        val mongoIndexStartup = MongoIndexStartup(mongoTemplate)
        mongoIndexStartup.afterPropertiesSet()
    }

    @Test
    fun `StepDao mongo collection should contain all necessary indexes`() {

        // when context is created and indexes should be created automatically
        val indexes = mongoTemplate.indexOps(StepDao::class.java).indexInfo
        val indexedFields = indexes.map { indexInfo -> indexInfo.indexFields.map { it.key } }
        // then
        assertThat(indexedFields)
            .containsAll(
                listOf(
                    listOf(StepDao::references.name + "." + Reference::referenceId.name, StepDao::configurationId.name),
                    listOf(StepDao::eventId.name, StepDao::configurationId.name),
                    listOf(StepDao::isNewlyInstanceAssigned.name, StepDao::receivedAt.name),
                    listOf(StepDao::processInstance.name)
                )
            )
    }

    @Test
    fun `ProcessInstanceStateDao mongo collection should contain all necessary indexes`() {
        // when context is created and indexes should be created automatically
        val indexes = mongoTemplate.indexOps(ProcessInstanceStateDao::class.java).indexInfo
        val indexedFields =
            indexes.map { indexInfo -> indexInfo.indexFields.map { it.key } to indexInfo.partialFilterExpression }
        // then
        assertThat(indexedFields)
            .containsAll(
                listOf(
                    listOf(ProcessInstanceStateDao::startedAt.name) to "{\"stage.isAdmitted\": false}",
                    listOf("_id", ProcessInstanceStateDao::version.name) to null
                )
            )
    }
}