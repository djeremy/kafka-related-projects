package com.djeremy.splitter

import org.apache.avro.Schema
import org.apache.avro.SchemaCompatibility
import org.apache.avro.generic.GenericRecord
import org.apache.avro.specific.SpecificData
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Predicate


/**
 * This class aims to provide type safe branching functionality based on SpecificType schema,
 * with additional possibility to perform further operations on the branched nodes, without losing
 * original type.
 * Please reference below example:
 *  {@code
 *        GenericTypeSplitter.build {
 *          branchNext(fromSchema(SomeType()) { mapValues { ... }})
 *          branchNext(fromSchema(OtherType()) { mapValues { ... }})
 *        }.executeSplit(transferFlowEventsStream)
 *   }
 *
 * Every {@code nextOperation} defined in @see SpecificNode class, (in this case through factory method)
 * will be executed on corresponding branched node after KStream#branch operation.
 *
 */
class TypeSafeSplitter(private val specifics: List<SpecificNode<GenericRecord>>) {

    fun split(stream: KStream<String, GenericRecord>): Array<KStream<String, GenericRecord>> {
        return stream.branch(*getPredicates().toTypedArray()).zip(specifics)
            .map { (stream, branchNode) ->
                val nextOperation = branchNode.transformation
                stream.mapValues { value -> SpecificData.get().deepCopy(value.schema, value) }
                    .nextOperation()
            }.toTypedArray()
    }

    private fun getPredicates(): List<Predicate<String, in GenericRecord>> = specifics.map { it.genericPredicate }


    class SpecificNode<T : GenericRecord>(
        val schema: Schema,
        val genericPredicate: Predicate<String, GenericRecord>,
        val transformation: (KStream<String, T>.() -> KStream<String, GenericRecord>)
    ) {

        companion object {
            fun <T : GenericRecord> whenIsInstanceOf(instance: T): SpecificNode<T> {
                return SpecificNode(
                    instance.schema,
                    { _, value -> value.schema.isCompatible(instance.schema) }) { this as KStream<String, GenericRecord> }
            }

            fun <T : GenericRecord> whenIsInstanceOf(
                instance: T,
                furtherTransformation: (KStream<String, T>.() -> KStream<String, GenericRecord>)
            ): SpecificNode<T> {
                return SpecificNode(instance.schema,
                    { _, value -> value.schema.isCompatible(instance.schema) }, furtherTransformation
                )
            }

            @Suppress("unused")
            fun <T : GenericRecord, K : Any> keyMapper(mapper: (T) -> K): KStream<String, T>.() -> KStream<K, T> {
                return { map { _, value -> mapper(value) toPair value } }
            }

            private fun Schema.isCompatible(other: Schema) =
                SchemaCompatibility.checkReaderWriterCompatibility(this, other)
                    .result.compatibility == SchemaCompatibility.SchemaCompatibilityType.COMPATIBLE
        }
    }

    companion object {
        fun build(operations: TypeSafeSplitterBuilder.() -> Unit) = with(TypeSafeSplitterBuilder()) {
            operations()
            build()
        }
    }
}

private infix fun <K, V> K.toPair(value: V): KeyValue<K, V> = KeyValue.pair(this, value)