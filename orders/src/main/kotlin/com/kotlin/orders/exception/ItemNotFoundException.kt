package com.kotlin.orders.exception

import java.lang.RuntimeException

class ItemNotFoundException(message: String) : RuntimeException(message) {

}
