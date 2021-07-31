# generic-type-splitter

## How to use splitter

To create splitter please use available builder method.

```kotlin
GenericTypeSplitter.build {
    branch(/*define operation here*/)
}
```

Please use this method to build GenericTypeSplitter with all necessary processing information.
DSL syntax allowing in declarative way define flow of branching and sentential operations.

Please node, that order of execution will preserve order of definition.

[GenericTypeSplitterBuilder](./src/main/kotlin/com/djeremy/splitter/GenericTypeSplitterBuilder.kt) contains
`branch` method allowing us to configure Type condition and further type safe operation performing on that type.
`branch` requires SpecificNode instance. Please reference below snippet for more details:

```kotlin
// only branch to separate stream
branch(whenIsInstanceOf(SplitterTypeOne()))
// branch and change type
branch(whenIsInstanceOf(SplitterTypeTwo()) {
    mapValues { value -> SplitterTypeThree(value.getId(), newValue) }
})
// branch and mutate
branch(whenIsInstanceOf(SplitterTypeThree()) {
    mapValues { value -> value.setPropertyThree(newValue); value }
})

```


!!! You need to configure KStream with `GenericAvroSerde` which assumes using multiple data types per single topic. Please
reference
a [confluent documentation](https://docs.confluent.io/platform/current/streams/developer-guide/config-streams.html#default-value-serde)
. 


## Update

This code was developed prior Kstreams api 2.8.0 version, which made
`branch` method deprecated and introduce new `split` method which has similar functionalities. 
For more details please reference an official confluent documentation:
https://kafka.apache.org/28/documentation/streams/upgrade-guide#streams_api_changes_280
and API: https://kafka.apache.org/28/javadoc/org/apache/kafka/streams/kstream/KStream.html#split().

