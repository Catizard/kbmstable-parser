package com.github.catizard.bms.table

class HttpException(val code: Int, message: String) : Exception(message)

class NotValidJsonException(message: String = "") : Exception("Not a JSON: $message")