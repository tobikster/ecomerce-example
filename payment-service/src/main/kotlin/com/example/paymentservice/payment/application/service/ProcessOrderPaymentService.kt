package com.example.paymentservice.payment.application.service

import com.example.paymentservice.account.domain.model.WithdrawResult
import com.example.paymentservice.account.domain.model.fold
import com.example.paymentservice.account.domain.port.incoming.WithdrawCommand
import com.example.paymentservice.account.domain.port.incoming.WithdrawUseCase
import com.example.paymentservice.payment.domain.model.Payment
import com.example.paymentservice.payment.domain.port.incoming.ProcessOrderPaymentCommand
import com.example.paymentservice.payment.domain.port.incoming.ProcessOrderPaymentUseCase
import com.example.paymentservice.payment.domain.port.outgoing.PaymentRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProcessOrderPaymentService(
	private val paymentRepository: PaymentRepositoryPort,
	private val withdraw: WithdrawUseCase,
) : ProcessOrderPaymentUseCase {
	@Transactional
	override fun invoke(command: ProcessOrderPaymentCommand) {
		if (paymentRepository.findByOrderId(command.orderId) != null) return

		val payment =
			withdraw(WithdrawCommand(command.customerId, command.amount))
				.fold(
					onSuccess = { (account) ->
						Payment.completed(command.orderId, account.customerId, command.amount)
					},
					onFailure = {
						val failureReason =
							when (it) {
								is WithdrawResult.Failure.CurrencyMismatch -> {
									"Currency mismatch; account currency: ${it.accountCurrency}, requestedCurrency: ${it.requestedCurrency}"
								}

								is WithdrawResult.Failure.InsufficientFunds -> {
									"Insufficient funds; requested ${it.requested}, but only ${it.available} is available"
								}
							}
						Payment.failed(command.orderId, command.customerId, command.amount, failureReason)
					},
				)

		paymentRepository.save(payment)
	}
}
