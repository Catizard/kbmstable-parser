package bms.table

class HttpException(val code: Int, message: String) : Exception("[$code] $message")

class NotValidJsonException(message: String = "") : Exception("Not a JSON: $message")