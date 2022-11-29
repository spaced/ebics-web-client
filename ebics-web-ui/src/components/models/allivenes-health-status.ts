import { ApiError } from 'components/models/ebics-api-error';

export enum AllivenesStatusType {
    Unknown,
    RespondingImmediatelly,
    RespondingSlowly,
    NeverResponding,
}

export enum HealthStatusType {
    Unknown = 'Unknown',
    Ok = 'Ok',
    Warning = 'Warning',
    Error = 'Error',
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
    lastError: ApiError,
    lastOkTimestamp: string,
    lastErrorTimestamp: string,
    actualStatisticsFrom: string,
    actualStatisticsTo: string,
    lastErrorOkStatisticsFrom: string,
    lastErrorOkStatisticsTo: string,
}


