package com.djeremy.process.monitor.adapter.startup

import com.djeremy.process.monitor.adapter.store.mongo.ProcessInstanceStateDao
import com.djeremy.process.monitor.adapter.store.mongo.StepDao
import com.djeremy.process.monitor.adapter.store.mongo.ddl.byEventIdAndConfiguration
import com.djeremy.process.monitor.adapter.store.mongo.ddl.byInstanceAndVersionUnique
import com.djeremy.process.monitor.adapter.store.mongo.ddl.byIsAdmittedFalseAndStartedAt
import com.djeremy.process.monitor.adapter.store.mongo.ddl.byIsNewlyInstanceAssignedAndReceivedAt
import com.djeremy.process.monitor.adapter.store.mongo.ddl.byReferenceIdAndConfiguration
import com.djeremy.process.monitor.adapter.store.mongo.ddl.deleteIndexes
import com.djeremy.process.monitor.adapter.store.mongo.ddl.ensureIndex
import org.springframework.beans.factory.InitializingBean
import org.springframework.data.mongodb.core.MongoTemplate

class MongoIndexStartup(private val mongoTemplate: MongoTemplate) : InitializingBean {

    override fun afterPropertiesSet() {
        stepCollectionRemoveUnusedIndexes()
        stepCollectionCreateIndexes()

        processInstanceStateCollectionRemoveUnusedIndexes()
        processInstanceStateCollectionCreateIndexes()
    }

    private fun stepCollectionRemoveUnusedIndexes() {
        mongoTemplate.deleteIndexes<StepDao>(
            listOf(
                "by_referenceId_reference",
                "by_configurationId_and_eventId_and_process_instanceId",
                "by_isNew_and_sort_receivedAt_asc",
                "references.referenceId_1_configurationId_1" // manually created
            )
        )
    }

    private fun stepCollectionCreateIndexes() {
        val byReferenceIdAndConfiguration = byReferenceIdAndConfiguration()
        val byIsNewlyInstanceAssignedAndReceivedAt = byIsNewlyInstanceAssignedAndReceivedAt()
        val byEventIdAndConfiguration = byEventIdAndConfiguration()

        mongoTemplate.ensureIndex<StepDao>(byReferenceIdAndConfiguration)
        mongoTemplate.ensureIndex<StepDao>(byIsNewlyInstanceAssignedAndReceivedAt)
        mongoTemplate.ensureIndex<StepDao>(byEventIdAndConfiguration)
    }

    private fun processInstanceStateCollectionRemoveUnusedIndexes() {
        mongoTemplate.deleteIndexes<ProcessInstanceStateDao>(
            listOf(
                "by_process_instance_stages_finished_alerted",
                "instance_and_version_unique"
            )
        )
    }

    private fun processInstanceStateCollectionCreateIndexes() {
        val byInstanceAndVersionUnique = byInstanceAndVersionUnique()
        val byIsAdmittedFalseAndStartedAt = byIsAdmittedFalseAndStartedAt()

        mongoTemplate.ensureIndex<ProcessInstanceStateDao>(byInstanceAndVersionUnique)
        mongoTemplate.ensureIndex<ProcessInstanceStateDao>(byIsAdmittedFalseAndStartedAt)
    }
}