package ru.ushakov.order.config

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(ex: Exception): ResponseEntity<Map<Any, Any>> {
        val errorMessage = ex.message ?: "Произошла ошибка"
        return ResponseEntity(mapOf("message" to errorMessage), HttpStatus.INTERNAL_SERVER_ERROR)
    }
}