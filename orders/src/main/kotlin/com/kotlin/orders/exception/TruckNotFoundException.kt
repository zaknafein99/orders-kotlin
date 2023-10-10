package com.kotlin.orders.exception

import java.lang.RuntimeException

class TruckNotFoundException(message: String) : RuntimeException(message)
