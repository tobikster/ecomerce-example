package com.example.paymentservice.account.domain.port.incoming

import com.example.paymentservice.account.domain.model.Account
import com.example.paymentservice.common.domain.model.CustomerId

interface GetAccountsUseCase {
	operator fun invoke(query: GetAccountsQuery): List<Account>
}

data class GetAccountsQuery(
	val customerId: CustomerId,
)
