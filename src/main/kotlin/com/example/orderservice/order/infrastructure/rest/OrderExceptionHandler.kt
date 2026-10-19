package com.example.orderservice.order.infrastructure.rest

import com.example.orderservice.order.domain.model.OrderException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(basePackageClasses = [OrderRestController::class])
class OrderExceptionHandler {
	@ExceptionHandler(OrderException::class)
	fun handleOrderException(exception: OrderException) =
		when (exception) {
			is OrderException.InvalidAmount -> {
				ProblemDetail.forStatusAndDetail(
					HttpStatus.BAD_REQUEST,
					exception.message ?: "Invalid amount",
				)
			}
		}

	@ExceptionHandler(MethodArgumentNotValidException::class)
	fun handleValidationException(exception: MethodArgumentNotValidException): ProblemDetail {
		val errors = exception.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "invalid") }

		return ProblemDetail
			.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
				"Validation failed for one or more fields",
			).apply {
				setProperty("errors", errors)
			}
	}
}
