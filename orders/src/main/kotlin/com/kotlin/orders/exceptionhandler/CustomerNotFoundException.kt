package com.kotlin.orders.exceptionhandler

import java.lang.RuntimeException

class CustomerNotFoundException(message: String) : RuntimeException(message)
