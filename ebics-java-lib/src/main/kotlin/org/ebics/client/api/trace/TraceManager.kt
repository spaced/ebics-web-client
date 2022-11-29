/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */
package org.ebics.client.api.trace

import org.ebics.client.exception.EbicsException
import org.ebics.client.exception.EbicsServerException
import org.ebics.client.interfaces.ContentFactory
import kotlin.reflect.KFunction

/**
 * A mean to make EBICS transfer logged by saving
 * requests and responses from the EBICS bank server.
 */
interface TraceManager {
    /**
     * Saves the content in the traces,
     * and annotate it with all information from TraceSession
     *
     * @param contentFactory the content to trace
     * @param traceSession trace session used
     *
     */
    fun trace(
        contentFactory: ContentFactory,
        traceSession: IBaseTraceSession,
        request:Boolean = traceSession.request,
    )

    fun traceException(
        exception: Exception,
        traceSession: IBaseTraceSession
    )



    /**
     * This is calling the provided function,
     * In case exception is thrown, the exception is traced and propagated further
     */
    fun <T> callAndUpdateLastTrace(traceSession: IBaseTraceSession, function: KFunction<T>, vararg params: Any?):T {
        return try {
            function.call(params)
        } catch (exception: Exception) {
            traceException(exception, traceSession)
            throw exception
        }
    }

    /**
     * This is used to call function which can throw EbicsServerException so that last incomplete trace message is updated
     * In case EbicsServerException is thrown, the last trace entry is updated, and exception is re-thrown
     * In case of no exception, the last trace entry is updated
     * In case of other exception than EbicsServerException, the exception is traced separately as new trace entry
     */
    fun <T> callAndUpdateLastTrace(traceSession: IBaseTraceSession, functionThrowingEbicsServerException: () -> T):T {
        try {
            val result = functionThrowingEbicsServerException()
            updateLastTrace(traceSession, TraceCategory.EbicsResponseOk)
            return result
        } catch (ebicsServerException: EbicsServerException) {
            updateLastTrace(traceSession, TraceCategory.EbicsResponseError, ebicsServerException)
            throw ebicsServerException
        } catch (exception: Exception) {
            traceException(exception, traceSession)
            throw exception
        }
    }

    /**
     * This will update last trace entry category & exception details if needed
     */
    fun updateLastTrace(
        traceSession: IBaseTraceSession,
        traceCategory: TraceCategory,
        exception: EbicsException? = null
    )

    /**
     * Enables or disables the trace feature
     * @param enabled is trace enabled?
     */
    fun setTraceEnabled(enabled: Boolean)

    /**
     * This updates the order number of all previous trace entries in the given @param traceSession
     */
    fun updateSessionOrderNumber(traceSession: IBankConnectionTraceSession, orderNumber: String)
}