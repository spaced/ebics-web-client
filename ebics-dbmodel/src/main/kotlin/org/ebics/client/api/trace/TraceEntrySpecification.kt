package org.ebics.client.api.trace

import org.ebics.client.api.bank.Bank
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.model.EbicsVersion
import org.springframework.data.jpa.domain.Specification
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.metamodel.SingularAttribute

/**
 * Add equals predicate to existing expressions
 * If the given value to be searched is not null
 */
fun <T, X> Predicate.orEqualsIfNotNull(builder: CriteriaBuilder, path: Path<X>, attributeName: String, value: T?): Predicate {
    if (value != null) {
        return builder.or(this,builder.attributeEquals(path, attributeName, value) )
    }
    return this
}
fun <T, X> Predicate.andEqualsIfNotNull(builder: CriteriaBuilder, path: Path<X>, attributeName: String, value: T?): Predicate {
    if (value != null) {
        return builder.and(this,builder.attributeEquals(path, attributeName, value) )
    }
    return this
}

fun <T, X> CriteriaBuilder.attributeEquals(path: Path<X>, attributeName: String, value: T): Predicate {
    return equal(path.get<SingularAttribute<X, T>>(attributeName), value)
}

fun <T, X> CriteriaBuilder.attributeIsNull(path: Path<X>, attributeName: String): Predicate {
    return isNull(path.get<SingularAttribute<X, T>>(attributeName))
}

fun <T, X> CriteriaBuilder.attributeIsNotNull(path: Path<X>, attributeName: String): Predicate {
    return isNotNull(path.get<SingularAttribute<X, T>>(attributeName))
}

fun bankConnectionEquals(bankConnection: BankConnectionEntity, useSharedPartnerData: Boolean = true): Specification<TraceEntry> {
    return Specification<TraceEntry> { root, _, builder ->
        var p = builder.disjunction()
        val userAttr = root.get<SingularAttribute<TraceEntry, BankConnectionEntity>>("bankConnection")
        if (bankConnection.id != null)
            p = builder.or(p, builder.equal(userAttr.get<SingularAttribute<BankConnectionEntity,String>>("id"),bankConnection.id))
        if (useSharedPartnerData) {
            val partnerAttr = userAttr.get<SingularAttribute<TraceEntry, String>>("partner").get<SingularAttribute<String,String>>("id")
            if (bankConnection.partner.id != null)
                p = builder.or(p, builder.equal(partnerAttr, bankConnection.partner.id))
        }
        p
    }
}

fun bankEquals(bank: Bank): Specification<TraceEntry> {
    return Specification<TraceEntry> { root, _, builder ->
        val p = builder.disjunction()
        val userAttr = root.get<SingularAttribute<TraceEntry, String>>("bank")
        p.orEqualsIfNotNull(builder, userAttr, "id", bank.id)
    }
}

fun creatorEquals(creator: String): Specification<TraceEntry> {
    return Specification<TraceEntry> { root, _, builder ->
        builder.attributeEquals(root, "creator", creator)
    }
}


fun orderTypeEquals(orderType: ITraceOrderTypeDefinition): Specification<TraceEntry> {
    return Specification<TraceEntry> { root, _, builder ->
        var p = builder.conjunction()

        with(orderType) {
            val orderTypeAttr = root.get<SingularAttribute<TraceEntry, String>>("orderType")
            p= p.andEqualsIfNotNull(builder, orderTypeAttr, "adminOrderType", adminOrderType)
            p= p.andEqualsIfNotNull(builder, orderTypeAttr, "businessOrderType", businessOrderType)
            val ebicsService = ebicsServiceType
            if (ebicsService != null) {
                with(ebicsService) {
                    val serviceTypeAttr = orderTypeAttr.get<SingularAttribute<TraceEntry, String>>("ebicsServiceType")
                    p= p.andEqualsIfNotNull(builder, serviceTypeAttr, "serviceName", serviceName)
                    p= p.andEqualsIfNotNull(builder, serviceTypeAttr, "serviceOption", serviceOption)
                    p= p.andEqualsIfNotNull(builder, serviceTypeAttr, "scope", scope)
                    p= p.andEqualsIfNotNull(builder, serviceTypeAttr, "containerType", containerType)
                    with(message) {
                        val messageAttr = serviceTypeAttr.get<SingularAttribute<TraceEntry, String>>("message")
                        p= p.andEqualsIfNotNull(builder, messageAttr, "messageName", messageName)
                        p = p.andEqualsIfNotNull(builder, messageAttr, "messageNameFormat", messageNameFormat)
                        p = p.andEqualsIfNotNull(builder, messageAttr, "messageNameVariant", messageNameVariant)
                        p = p.andEqualsIfNotNull(builder, messageAttr, "messageNameVersion", messageNameVersion)
                    }
                }
            }
        }
        p
    }
}

fun sessionIdEquals(sessionId: String): Specification<TraceEntry> {
    return Specification<TraceEntry> { root, _, builder ->
        builder.attributeEquals(root, "sessionId", sessionId)
    }
}

fun ebicsVersionEquals(ebicsVersion: EbicsVersion): Specification<TraceEntry> {
    return Specification<TraceEntry> { root, _, builder ->
        builder.attributeEquals(root, "ebicsVersion", ebicsVersion)
    }
}

fun uploadEquals(upload: Boolean): Specification<TraceEntry> {
    return Specification<TraceEntry> { root, _, builder ->
        builder.attributeEquals(root, "upload", upload)
    }
}

fun traceTypeEquals(traceType: TraceType): Specification<TraceEntry> {
    return Specification<TraceEntry> { root, _, builder ->
        builder.attributeEquals(root, "traceType", traceType)
    }
}

fun traceCategoryEquals(traceCategory: TraceCategory): Specification<TraceEntry> {
    return Specification<TraceEntry> { root, _, builder ->
        builder.attributeEquals(root, "traceCategory", traceCategory)
    }
}

fun traceCategoryIsNull(): Specification<TraceEntry> {
    return Specification<TraceEntry> { root, _, builder ->
        builder.attributeIsNull<String, TraceEntry>(root, "traceCategory")
    }
}

fun traceMessageBodyIsNotEmpty(): Specification<TraceEntry> {
    return Specification<TraceEntry> { root, _, builder ->
        builder.attributeIsNotNull<String, TraceEntry>(root,"textMessageBody")
    }.or { root, _, builder ->
        builder.attributeIsNotNull<String, TraceEntry>(root, "binaryMessageBody")
    }
}

fun fileDownloadFilter(creator: String, orderType: ITraceOrderTypeDefinition, user: BankConnectionEntity, ebicsVersion: EbicsVersion, useSharedPartnerData: Boolean = true): Specification<TraceEntry> {
    return creatorEquals(creator)
        .and(orderTypeEquals(orderType))
        .and(bankConnectionEquals(user, useSharedPartnerData))
        .and(ebicsVersionEquals(ebicsVersion))
        .and(uploadEquals(false))
        .and(traceTypeEquals(TraceType.Content))
        .and(traceCategoryIsNull())
        .and(traceMessageBodyIsNotEmpty())
}
