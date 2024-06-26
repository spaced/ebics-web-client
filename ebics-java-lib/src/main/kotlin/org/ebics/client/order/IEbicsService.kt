package org.ebics.client.order

import org.ebics.client.order.h005.ContainerType

interface IEbicsService {
    val serviceName: String
    val serviceOption: String?
    val scope: String?
    val containerType: ContainerType?
    val message: IEbicsMessage
}