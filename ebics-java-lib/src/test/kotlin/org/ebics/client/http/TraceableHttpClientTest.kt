package org.ebics.client.http

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.ebics.client.api.trace.IBaseTraceSession
import org.ebics.client.api.trace.TraceCategory
import org.ebics.client.api.trace.TraceManager
import org.ebics.client.exception.HttpClientException
import org.ebics.client.exception.HttpServerException
import org.ebics.client.http.client.SimpleHttpClient
import org.ebics.client.http.client.TraceableHttpClient
import org.ebics.client.http.client.request.HttpClientRequest
import org.ebics.client.io.ByteArrayContentFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import java.net.URL

@ExtendWith(MockKExtension::class)
class TraceableHttpClientTest {
    @MockK
    lateinit var simpleHTTPClient: SimpleHttpClient

    @Test
    fun sendOkRequestAndReceiveOkResponse_thenTraceItAndUpdateRequestTraceAsOk() {
        val traceManager = mockk<TraceManager>()
        val traceSession = mockk<IBaseTraceSession>()
        every { simpleHTTPClient.send(any()) } returns ByteArrayContentFactory("test-response".toByteArray())
        every { traceManager.trace(any(), traceSession, any()) } just runs
        every { traceManager.updateLastTrace(any(), any(), any()) } just runs

        val client = TraceableHttpClient(simpleHTTPClient, traceManager)
        val requestContent = ByteArrayContentFactory("test-request".toByteArray())
        val response = client.sendAndTrace(
            HttpClientRequest(
                URL("http://not.existing.url.com.xx"), requestContent),
            traceSession
        )
        verify(exactly = 1) { traceManager.trace(requestContent, traceSession, true) }
        verify(exactly = 1) { traceManager.updateLastTrace(traceSession, TraceCategory.RequestOk) }
        Assertions.assertArrayEquals("test-response".toByteArray(), response.content.readBytes())
    }

    @Test
    fun sendRequestAndReceiveHttpClientException_thenTraceItAndUpdateRequestTraceAsError() {
        val traceManager = mockk<TraceManager>()
        val traceSession = mockk<IBaseTraceSession>()
        val exception = HttpClientException("testException", IllegalAccessException("test"))
        every { simpleHTTPClient.send(any()) } throws exception
        every { traceManager.trace(any(), traceSession, any()) } just runs
        every { traceManager.updateLastTrace(any(), any(), any()) } just runs

        val client = TraceableHttpClient(simpleHTTPClient, traceManager)
        val requestContent = ByteArrayContentFactory("test-request".toByteArray())
        try {
            val response = client.sendAndTrace(
                HttpClientRequest(
                    URL("http://not.existing.url.com.xx"), requestContent
                ),
                traceSession
            )
            fail("HttpClientException Exception expected but not thrown")
        } catch (ex: HttpClientException) {
            Assertions.assertEquals(ex, exception)
        }
        verify(exactly = 1) { traceManager.trace(requestContent, traceSession, true) }
        verify(exactly = 1) { traceManager.updateLastTrace(traceSession, TraceCategory.RequestError, exception) }
    }

    @Test
    fun sendRequestAndReceiveNONHttpClientException_thenTraceItAndCreateNewExceptionInTrace() {
        val traceManager = mockk<TraceManager>()
        val traceSession = mockk<IBaseTraceSession>()
        val exception = HttpServerException("00","test other exception than HTTP Client Ex", "msg")
        every { simpleHTTPClient.send(any()) } throws exception
        every { traceManager.trace(any(), traceSession, any()) } just runs
        every { traceManager.traceException(any(), any()) } just runs

        val client = TraceableHttpClient(simpleHTTPClient, traceManager)
        val requestContent = ByteArrayContentFactory("test-request".toByteArray())
        try {
            val response = client.sendAndTrace(
                HttpClientRequest(
                    URL("http://not.existing.url.com.xx"), requestContent
                ),
                traceSession
            )
            fail("HttpServerException Exception expected but not thrown")
        } catch (ex: HttpServerException) {
            Assertions.assertEquals(ex, exception)
        }
        verify(exactly = 1) { traceManager.trace(requestContent, traceSession, true) }
        verify(exactly = 1) { traceManager.traceException(exception, traceSession) }
    }
}