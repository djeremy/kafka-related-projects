package com.djeremy.process.monitor.utils

import org.assertj.core.api.AbstractComparableAssert
import org.assertj.core.api.AbstractListAssert
import org.assertj.core.api.AbstractObjectAssert
import org.assertj.core.api.Condition
import org.assertj.core.api.ObjectAssert
import java.util.function.Function
import kotlin.reflect.KFunction1

fun <T : AbstractObjectAssert<*, ELEMENT>, ELEMENT> T._extracting(vararg extractors: KFunction1<ELEMENT, Any?>): AbstractListAssert<*, MutableList<*>, Any, ObjectAssert<Any>> {
    return this.extracting(*(extractors.map { it.f() }.toTypedArray()))
}

@Suppress("UNCHECKED_CAST")
fun <T : AbstractComparableAssert<*, *>, A> T._has(con: Condition<A>): T = apply { has(con as Condition<in Comparable<*>>) }
fun <T : AbstractComparableAssert<*, *>> T._describedAs(desc: String, vararg args: String): T = apply { describedAs(desc, args) }

// add manually import kotlin.reflect.KFunction1
fun <ELEMENT, V> ((ELEMENT) -> V).f(): Function<ELEMENT, V> = java.util.function.Function { e -> this(e) }
fun <ELEMENT, V> KFunction1<ELEMENT, V>.f(): Function<in ELEMENT, V> = java.util.function.Function { e -> this(e) }