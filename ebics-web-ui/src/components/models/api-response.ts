export enum RelatedObjectType {
    Bank,
    BankConnection,
    Global
}

export interface RelatedObjectReference {
    type: RelatedObjectType,
    id?: number,
}

export enum ApiResponseType {
    Ok,
    Error,
}

export interface ApiResponse {
    status: ApiResponseType,
    error?: unknown,
    timeStamp: Date,
    responseTime: number,
}