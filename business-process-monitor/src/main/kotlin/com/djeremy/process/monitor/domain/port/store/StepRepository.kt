package com.djeremy.process.monitor.domain.port.store

import com.djeremy.process.monitor.domain.process.models.Step

interface StepRepository {
    fun save(model: Step)
    fun saveAll(models: List<Step>)

    /**
     *  This method should returns steps which were recently assigned to processInstanceId
     *  and was not yest admitted
     *  @return List<Step>
     *  @see Step
     */
    fun getNewSteps(): List<Step>

    /**
     *  This method should return steps which belongs to specific @param configurationId
     *  and has specific @param referenceId defined in references and has processInstance already
     *  assigned.
     *  Practically saying this method will return all steps, which had reference to id. Which can be called as
     *  parents.
     *  @return List<Step>
     *  @see Step.references
     *  @see com.djeremy.process.monitor.v2.domain.process.models.Reference
     */
    fun getWithProcessInstanceBy(configurationId: String, referenceId: String): List<Step>

    /**
     * Same as getWithProcessInstanceBy but return steps which was not assigned to processInstance yet.
     * Practically saying which was not joined with other steps.
     *
     * @see getWithProcessInstanceBy
     */
    fun getWithoutProcessInstanceBy(configurationId: String, referenceId: String): List<Step>

    /**
     *  This method should return steps which belongs to specific @param configurationId
     *  and has eventId as one of ids specified in list (@param eventIds).
     *  assigned.
     *  @return List<Step>
     */
    fun getWithoutProcessInstanceBy(configurationId: String, eventIds: List<String>): List<Step>
}