package com.example.paymentservice.infrastructure.rest.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.util.Currency
import kotlin.reflect.KClass

@Target(
	AnnotationTarget.FIELD,
	AnnotationTarget.PROPERTY_GETTER,
	AnnotationTarget.VALUE_PARAMETER,
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [ValidCurrencyValidator::class])
annotation class ValidCurrencyCode(
	@Suppress("unused")
	val message: String = "{com.example.paymentservice.infrastructure.rest.validation.ValidCurrencyCode.message}",
	@Suppress("unused")
	val groups: Array<KClass<*>> = [],
	@Suppress("unused")
	val payload: Array<KClass<out Payload>> = [],
)

class ValidCurrencyValidator : ConstraintValidator<ValidCurrencyCode, String> {
	override fun isValid(
		value: String?,
		context: ConstraintValidatorContext,
	): Boolean {
		val isValid = value?.runCatching { Currency.getInstance(value) }?.isSuccess ?: true

		if (!isValid) {
			context.apply {
				disableDefaultConstraintViolation()
				buildConstraintViolationWithTemplate("The value $value is not a valid ISO 4217 currency code")
					.addConstraintViolation()
			}
		}
		return isValid
	}
}
