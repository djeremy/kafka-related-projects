package com.djeremy.splitter

import org.apache.avro.generic.GenericRecord

class TypeSafeSplitterBuilder {
    private val specifics: MutableList<TypeSafeSplitter.SpecificNode<GenericRecord>> = mutableListOf()

    fun branch(specificMode: TypeSafeSplitter.SpecificNode<out GenericRecord>): TypeSafeSplitterBuilder {
        @Suppress("UNCHECKED_CAST")
        specifics.add(specificMode as TypeSafeSplitter.SpecificNode<GenericRecord>)
        return this
    }

    internal fun build(): TypeSafeSplitter {
        require(specifics.isNotEmpty()) { "Please specify at least one SpecificBranch" }
        return TypeSafeSplitter(specifics)
    }

}