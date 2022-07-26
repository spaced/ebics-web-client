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

export enum HealthStatusErrorType {
    HTTP,
    EBICS
}

export interface ConnectionStatusObject {
    allivenes: AllivenesStatusType,
    health: HealthStatusType,
    healthErrorType: HealthStatusErrorType,
    errorCount: number,
    errorRate: number,
    okCount: number,
    okRate: number,
    totalCount: number,
}

export interface ConnectionStatus {
    status: ConnectionStatusObject,
}

