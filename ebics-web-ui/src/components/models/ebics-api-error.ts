/**
 * Generic error (not necesarly from the REST API)
 */
export interface ApiError {
  timestamp: string;
  message: string;
}

/**
 * Any error from the EWC backend
 */
export interface EbicsApiError extends ApiError {
  causeMessage?: string;
  exceptionClass: string;
  httpStatusCode: number;
  httpStatusResonPhrase: string;
}

/**
 * Ebics Server errors (either HTTP or EBICS server error)
 */
export interface EbicsServerApiError extends EbicsApiError {
  errorCode: string;
  errorCodeText: string;
}