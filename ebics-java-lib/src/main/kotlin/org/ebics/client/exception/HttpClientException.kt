package org.ebics.client.exception

class HttpClientException(message: String, exception: Exception) : EbicsException(message, exception)