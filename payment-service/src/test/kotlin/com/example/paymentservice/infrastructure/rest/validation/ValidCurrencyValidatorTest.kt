package com.example.paymentservice.infrastructure.rest.validation

import io.kotest.matchers.shouldBe
import jakarta.validation.ConstraintValidatorContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ValidCurrencyValidatorTest {
	private val validator = ValidCurrencyValidator()
	private val context: ConstraintValidatorContext = mock()
	private val violationBuilder: ConstraintValidatorContext.ConstraintViolationBuilder = mock()

	@BeforeEach
	fun setUp() {
		whenever(context.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder)
		whenever(violationBuilder.addConstraintViolation()).thenReturn(context)
	}

	@ParameterizedTest
	@ValueSource(strings = ["PLN", "EUR", "USD", "GBP", "CHF", "JPY"])
	fun `should return true for valid ISO 4217 currency codes`(currencyCode: String) {
		validator.isValid(currencyCode, context) shouldBe true
	}

	@Test
	fun `should return true for null value`() {
		validator.isValid(null, context) shouldBe true
	}

	@ParameterizedTest
	@ValueSource(strings = ["", "   ", "XYZ", "123", "PL", "PLNN"])
	fun `should return false and register violation for invalid currency codes`(invalidCode: String) {
		val result = validator.isValid(invalidCode, context)

		result shouldBe false
		verify(context).disableDefaultConstraintViolation()
		verify(context).buildConstraintViolationWithTemplate("The value $invalidCode is not a valid ISO 4217 currency code")
		verify(violationBuilder).addConstraintViolation()
	}
}
