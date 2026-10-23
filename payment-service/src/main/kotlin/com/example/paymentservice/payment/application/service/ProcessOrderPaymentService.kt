package com.example.paymentservice.payment.application.service

import com.example.paymentservice.account.domain.model.AccountException
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
			try {
				val account = withdraw(WithdrawCommand(command.customerId, command.amount))
				Payment.completed(command.orderId, account.customerId, command.amount)
			} catch (e: AccountException.InsufficientFunds) {
				Payment.failed(command.orderId, command.customerId, command.amount, e.message)
			}

		paymentRepository.save(payment)
	}
}
