package com.oksusu.susu.api.common.dto

import com.oksusu.susu.common.consts.FAIL_TO_VALIDATE_MESSAGE
import com.oksusu.susu.common.exception.ErrorCode
import jakarta.validation.ConstraintViolationException
import kotlinx.coroutines.CancellationException
import org.springframework.core.codec.DecodingException
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebInputException

data class ErrorResponse(
    val errorCode: String,
    val reason: String,
    val extra: Map<String, Any>? = null,
) {
    companion object {
        fun of(e: WebExchangeBindException): ErrorResponse {
            return ErrorResponse(
                errorCode = ErrorCode.BAD_REQUEST_ERROR.name,
                reason = e.fieldErrors
                    .joinToString(" ") { error -> "[${error.field}] ${error.defaultMessage}." }
                    .ifBlank { FAIL_TO_VALIDATE_MESSAGE }
            )
        }

        fun of(e: DecodingException): ErrorResponse {
            val errorCode = ErrorCode.BAD_REQUEST_ERROR
            return ErrorResponse(
                errorCode = errorCode.name,
                reason = e.message ?: errorCode.description
            )
        }

        fun of(e: ConstraintViolationException): ErrorResponse {
            return ErrorResponse(
                errorCode = ErrorCode.BAD_REQUEST_ERROR.name,
                reason = e.constraintViolations.firstOrNull()?.messageTemplate
                    ?: FAIL_TO_VALIDATE_MESSAGE
            )
        }

        fun of(e: ServerWebInputException): ErrorResponse {
            return ErrorResponse(
                errorCode = ErrorCode.BAD_REQUEST_ERROR.name,
                reason = e.reason ?: FAIL_TO_VALIDATE_MESSAGE
            )
        }

        fun of(e: CancellationException): ErrorResponse {
            val errorCode = ErrorCode.COROUTINE_CANCELLATION_ERROR
            return ErrorResponse(
                errorCode = errorCode.name,
                reason = e.message ?: errorCode.description
            )
        }

        fun of(e: Exception): ErrorResponse {
            val errorCode = ErrorCode.INTERNAL_SERVER_ERROR
            return ErrorResponse(
                errorCode = errorCode.name,
                reason = e.message ?: errorCode.description
            )
        }
    }
}
