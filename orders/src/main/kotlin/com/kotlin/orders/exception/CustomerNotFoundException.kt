package com.kotlin.orders.exception

import java.lang.RuntimeException

class CustomerNotFoundException(message: String) : RuntimeException(message)
