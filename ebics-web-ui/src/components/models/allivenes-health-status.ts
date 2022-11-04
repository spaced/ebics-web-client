export enum AllivenesStatusType {
    Unknown,
    RespondingImmediatelly,
    RespondingSlowly,
    NeverResponding,
}

export enum HealthStatusType {
    Unknown,
    Ok,
    Warning,
    Error
}

export interface EbicsException {
    message: string,
    cause: EbicsException,
    localizedMessage: string,
}

export interface ConnectionStatusObject {
    allivenes: AllivenesStatusType,
    healthStatus: HealthStatusType,
    errorCount: number,
    errorRate: number,
    okCount: number,
    okRate: number,
    totalCount: number,
    lastError: EbicsException,
    lastOkTimestamp: string,
    lastErrorTimestamp: string,
    actualStatisticsFrom: string,
    lastErrorOkStatisticsFrom: string,
}


