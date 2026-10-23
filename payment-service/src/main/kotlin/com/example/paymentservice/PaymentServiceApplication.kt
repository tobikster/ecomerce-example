package com.example.paymentservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PaymentServiceApplication

fun main(args: Array<String>) {
	@Suppress("SpreadOperator")
	runApplication<PaymentServiceApplication>(*args)
}
