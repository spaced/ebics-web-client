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

import org.ebics.client.interfaces.ContentFactory
import java.lang.Exception
import kotlin.reflect.KFunction

/**
 * A mean to make EBICS transfer logged by saving
 * requests and responses from the EBICS bank server.
 */
interface TraceManager {
    /**
     * Saves the content in the traces,
     * and annotate it with all information in TraceSession
     *
     * @param element the element to trace
     * @param traceRequest all the trace request attributes
     *
     * @see Configuration.isTraceEnabled
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
    fun <T> callAndTraceException(traceSession: IBaseTraceSession, function: KFunction<T>, vararg params: Any?):T {
        return try {
            function.call(params)
        } catch (exception: Exception) {
            traceException(exception, traceSession)
            throw exception
        }
    }

    fun <T> callAndTraceException(traceSession: IBaseTraceSession, function: () -> T):T {
        return try {
            function().apply {  }
        } catch (exception: Exception) {
            traceException(exception, traceSession)
            throw exception
        }
    }

    /**
     * Enables or disables the trace feature
     * @param enabled is trace enabled?
     */
    fun setTraceEnabled(enabled: Boolean)
}