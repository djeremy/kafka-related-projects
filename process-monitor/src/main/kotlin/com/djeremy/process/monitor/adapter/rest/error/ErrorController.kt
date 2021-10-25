package com.djeremy.process.monitor.adapter.rest.error

import com.djeremy.process.monitor.utils.toCause
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ErrorController : ResponseEntityExceptionHandler() {


    @ExceptionHandler
    fun handleRuntimeException(
        runtimeException: RuntimeException,
        webRequest: WebRequest
    ): ResponseEntity<Any> {
        return handleExceptionInternal(
            runtimeException, Error(
                "internal.error", runtimeException.toCause()
            ), HttpHeaders.EMPTY, HttpStatus.INTERNAL_SERVER_ERROR, webRequest
        )
    }
}