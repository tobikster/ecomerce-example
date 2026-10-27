package com.example.paymentservice.payment.application.service

import com.example.paymentservice.account.domain.model.Account
import com.example.paymentservice.account.domain.model.WithdrawResult
import com.example.paymentservice.account.domain.port.incoming.WithdrawCommand
import com.example.paymentservice.account.domain.port.incoming.WithdrawUseCase
import com.example.paymentservice.common.domain.model.CustomerId
import com.example.paymentservice.common.domain.model.OrderId
import com.example.paymentservice.common.domain.model.of
import com.example.paymentservice.payment.domain.model.Payment
import com.example.paymentservice.payment.domain.model.PaymentStatus
import com.example.paymentservice.payment.domain.port.incoming.ProcessOrderPaymentCommand
import com.example.paymentservice.payment.domain.port.outgoing.PaymentRepositoryPort
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.then
import org.mockito.kotlin.verify
import org.mockito.kotlin.willReturn
import java.math.BigDecimal
import java.util.Currency
import java.util.UUID

class ProcessOrderPaymentServiceTest {
	private val paymentRepository: PaymentRepositoryPort = mock()
	private val withdraw: WithdrawUseCase = mock()
	private val service = ProcessOrderPaymentService(paymentRepository, withdraw)

	private val pln = Currency.getInstance("PLN")
	private val customerId = CustomerId("cust_123")
	private val orderId = OrderId(UUID.randomUUID())
	private val amount = BigDecimal("150.00") of pln

	@Test
	fun `should successfully process payment when funds are available`() {
		// Given
		val command = ProcessOrderPaymentCommand(orderId, customerId, amount)
		val account = Account.create(customerId, pln).deposit(BigDecimal("200.00") of pln)

		given { paymentRepository.findByOrderId(orderId) } willReturn { null }
		given { withdraw(WithdrawCommand(customerId, amount)) } willReturn { WithdrawResult.Success(account) }

		// When
		service(command)

		// Then
		verify(paymentRepository).save(
			check { payment ->
				payment.orderId shouldBe orderId
				payment.customerId shouldBe customerId
				payment.amount shouldBe amount
				payment.status shouldBe PaymentStatus.COMPLETED
				payment.failureReason.shouldBeNull()
			},
		)
	}

	@Test
	fun `should record failed payment when account has insufficient funds`() {
		// Given
		val command = ProcessOrderPaymentCommand(orderId, customerId, amount)

		given { paymentRepository.findByOrderId(orderId) } willReturn { null }
		given { withdraw(WithdrawCommand(customerId, amount)) } willReturn {
			WithdrawResult.Failure.InsufficientFunds(
				requested = amount,
				available = BigDecimal("50.00") of pln,
			)
		}

		// When
		service(command)

		// Then
		verify(paymentRepository).save(
			check { payment ->
				payment.orderId shouldBe orderId
				payment.customerId shouldBe customerId
				payment.amount shouldBe amount
				payment.status shouldBe PaymentStatus.FAILED
				payment.failureReason shouldContain "Insufficient funds"
			},
		)
	}

	@Test
	fun `should ignore event when payment for order already exists`() {
		// Given
		val command = ProcessOrderPaymentCommand(orderId, customerId, amount)
		val existingPayment = Payment.completed(orderId, customerId, amount)

		given { paymentRepository.findByOrderId(orderId) } willReturn { existingPayment }

		// When
		service(command)

		// Then
		then(withdraw).shouldHaveNoInteractions()
		verify(paymentRepository, never()).save(any())
	}
}
