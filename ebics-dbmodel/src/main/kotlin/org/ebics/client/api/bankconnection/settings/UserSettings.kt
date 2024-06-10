package org.ebics.client.api.bankconnection.settings

import jakarta.persistence.*

data class UserSettingsData(
    val uploadOnDrop: Boolean,
    val testerSettings: Boolean,
    val adjustmentOptions: AdjustmentOptions,
    val displayAdminTypes: Boolean,
    val displaySharedBankConnections: Boolean,
    val displayErroneousConnections: Boolean,
    val displaySharedTemplates: Boolean,
    val displayPredefinedTemplates: Boolean,
)

/**
    User Settings class
 */
@Entity
class UserSettings(
    @Id
    val userId: String,

    val uploadOnDrop: Boolean,
    val testerSettings: Boolean,

    @Embedded
    val adjustmentOptions: AdjustmentOptions,

    val displayAdminTypes: Boolean,
    val displaySharedBankConnections: Boolean,
    val displayErroneousConnections: Boolean,
    @Column(columnDefinition = "boolean default true")
    val displaySharedTemplates: Boolean,
    @Column(columnDefinition = "boolean default true")
    val displayPredefinedTemplates: Boolean,
) : UserSettingsAccessRightsController {
    override fun getOwnerName(): String = userId
    override fun getObjectName(): String = "User settings of '$userId'"
}

@Embeddable
class AdjustmentOptions(
    val applyAutomatically: Boolean,

    @Embedded
    val pain00x: AdjustmentsOptionsPain00x,

    @Embedded
    val swift: AdjustmentsOptionsSwift,
)

@Embeddable
class AdjustmentsOptionsPain00x(
    val msgId: Boolean,
    val pmtInfId: Boolean,
    val instrId: Boolean,
    val endToEndId: Boolean,
    val uetr: Boolean,
    val reqdExctnDt: Boolean,
    val creDtTm: Boolean,
    val nbOfTrxsCalc: Boolean,
    val ctrlSumCalc: Boolean,
    val idPrefix: String,
)

@Embeddable
class AdjustmentsOptionsSwift(
    val uetr: Boolean,
    val f20: Boolean,
    val f21: Boolean,
    val randomIds: Boolean,
    val f30: Boolean,
    val idPrefix: String,
)
