package com.example.paymentservice.account.domain.port.outgoing

import com.example.paymentservice.account.domain.model.Account
import com.example.paymentservice.common.domain.model.CustomerId
import java.util.Currency

interface AccountRepositoryPort {
	fun findByCustomerId(customerId: CustomerId): List<Account>

	fun findByCustomerIdAndCurrency(
		customerId: CustomerId,
		currency: Currency,
	): Account?

	fun save(account: Account): Account
}
