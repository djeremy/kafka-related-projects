package com.djeremy.process.monitor.utils

import org.apache.commons.lang3.exception.ExceptionUtils

fun Throwable.toCause(): String {
    fun cause(throwable: Throwable): String = throwable::class.simpleName + ": " + (throwable.message ?: "Unknown")

    return ExceptionUtils.getThrowableList(this).joinToString(transform = ::cause)
}