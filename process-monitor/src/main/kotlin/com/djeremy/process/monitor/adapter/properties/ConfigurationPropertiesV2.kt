package com.djeremy.process.monitor.adapter.properties

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated
import java.time.Duration
import javax.annotation.PostConstruct
import javax.validation.Valid
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("djeremy.kafka.system.monitor.v2")
@Validated
class ConfigurationPropertiesV2 {
    @get:NotNull
    @get:Size(min = 1)
    @get:Valid
    var configurations: List<ProcessConfigurationPropertiesV2>? = null

    @PostConstruct
    fun constructSteps() {
        val mapper = ObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val validator = Validation.buildDefaultValidatorFactory().validator

        configurations!!.forEach {
            it.convertSteps(mapper, validator)
        }
    }
}

data class ProcessConfigurationPropertiesV2(
        @get:NotBlank
        var id: String? = null,
        @get:NotBlank
        var description: String? = null,
        @get:NotNull
        @get:Size(min = 2)
        @get:Valid
        var steps: List<HashMap<String, Any>>? = null,
        @get:NotNull
        var expectToFinishIn: Duration? = null
) {
    companion object {
        val stepsTypeReference = object : TypeReference<List<StepConfigurationPropertiesV2>>() {}
    }

    lateinit var convertedSteps: List<StepConfigurationPropertiesV2>

    internal fun convertSteps(objectMapper: ObjectMapper, validator: Validator) {
        replaceNestedMapsWithListOfValues(StepConfigurationPropertiesV2::referenceIdSchemaPaths.name)
        replaceNestedMapsWithListOfValues(MultipleExclusiveStepConfiguration::altReferenceIdSchemaPaths.name)

        convertedSteps = objectMapper.convertValue(steps, stepsTypeReference)

        convertedSteps.flatMap { validator.validate(it) }.takeIf { it.isNotEmpty() }?.let {
            throw IllegalArgumentException("Constructed steps failed validation due constrain violations: $it")
        }
    }

    internal fun replaceNestedMapsWithListOfValues(propertyName: String) {
        steps!!.forEach {
            it.computeIfPresent(propertyName) { _, value -> @Suppress("UNCHECKED_CAST") (value as Map<String, Any>).values }
        }
    }
}

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type", visible = true)
@JsonSubTypes(
        value = [
            JsonSubTypes.Type(value = SingleStepConfiguration::class, name = "single"),
            JsonSubTypes.Type(value = MultipleExclusiveStepConfiguration::class, name = "multipleExclusive")
        ]
)
abstract class StepConfigurationPropertiesV2 {
    @NotNull
    var type: String? = null

    @NotNull
    var topic: String? = null

    @NotNull
    var description: String? = null

    @NotNull
    var schemaName: String? = null

    var count: Int = 1

    var eventIdSchemaPath: String? = null
    var referenceIdSchemaPaths: List<String> = emptyList()

    var indicateProcessFinished: Boolean = false
}

class SingleStepConfiguration : StepConfigurationPropertiesV2()

// TODO think about list of alternatives :)
class MultipleExclusiveStepConfiguration : StepConfigurationPropertiesV2() {

    val altSchemaName: String? = null
    val altEventIdSchemaPath: String? = null
    val altReferenceIdSchemaPaths: List<String> = emptyList()
    var altIndicateProcessFinished: Boolean = false
}