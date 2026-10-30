package com.example.paymentservice.payment.domain.port.outgoing

import com.example.paymentservice.payment.domain.model.PaymentEvent

interface PaymentOutboxPublisherPort {
	fun publish(event: PaymentEvent)
}
