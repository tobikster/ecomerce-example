package com.example.paymentservice.infrastructure.rest

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.method.ParameterErrors
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalRestExceptionHandler {
	@ExceptionHandler(MethodArgumentNotValidException::class)
	fun handleValidationException(exception: MethodArgumentNotValidException): ProblemDetail {
		val fieldErrors =
			exception.bindingResult.fieldErrors.map {
				mapOf(
					"field" to it.field,
					"rejectedValue" to it.rejectedValue,
					"message" to (it.defaultMessage ?: "invalid"),
				)
			}

		return ProblemDetail
			.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
				"Validation failed for one or more fields",
			).apply {
				setProperty("errors", fieldErrors)
			}
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException::class)
	fun handleMethodArgumentTypeMismatchException(exception: MethodArgumentTypeMismatchException): ProblemDetail {
		val requiredType = exception.requiredType?.simpleName ?: "unknown"
		val message =
			"Parameter '${exception.name}' " +
				"with value '${exception.value}' " +
				"cannot be converted into type $requiredType"

		val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message)
		return problemDetail
	}

	@ExceptionHandler(HandlerMethodValidationException::class)
	fun handleMethodValidation(ex: HandlerMethodValidationException): ProblemDetail {
		val problem =
			ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
				"Path or query parameter is not valid",
			)

		val errors =
			ex.parameterValidationResults.flatMap { result ->
				if (result is ParameterErrors) {
					result.fieldErrors.map { fieldError ->
						mapOf(
							"field" to fieldError.field,
							"rejectedValue" to fieldError.rejectedValue,
							"message" to (fieldError.defaultMessage ?: "invalid"),
						)
					}
				} else {
					result.resolvableErrors.map { error ->
						mapOf(
							"field" to result.methodParameter.parameterName,
							"message" to (error.defaultMessage ?: "invalid"),
						)
					}
				}
			}
		problem.setProperty("errors", errors)
		return problem
	}

	@ExceptionHandler(ConstraintViolationException::class)
	fun handleConstraintViolation(ex: ConstraintViolationException): ProblemDetail {
		val problem =
			ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
				"Parameters constraints violation",
			)

		val errors =
			ex.constraintViolations.map { violation ->
				mapOf(
					"field" to violation.propertyPath.last().name,
					"rejectedValue" to violation.invalidValue,
					"message" to violation.message,
				)
			}
		problem.setProperty("errors", errors)
		return problem
	}

	@ExceptionHandler(HttpMessageNotReadableException::class)
	fun handleMalformedJson(): ProblemDetail {
		val problemDetail =
			ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
				"Incorrect JSON format of the request",
			)
		return problemDetail
	}
}
