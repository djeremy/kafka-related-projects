package com.djeremy.splitter

import org.apache.avro.generic.GenericRecord

class GenericTypeSplitterBuilder {
    private val specifics: MutableList<GenericTypeSplitter.SpecificNode<GenericRecord>> = mutableListOf()

    fun branchNext(specificMode: GenericTypeSplitter.SpecificNode<out GenericRecord>): GenericTypeSplitterBuilder {
        @Suppress("UNCHECKED_CAST")
        specifics.add(specificMode as GenericTypeSplitter.SpecificNode<GenericRecord>)
        return this
    }

    fun build(): GenericTypeSplitter {
        require(specifics.isNotEmpty()) { "Please specify at least one SpecificBranch" }
        return GenericTypeSplitter(specifics)
    }

}