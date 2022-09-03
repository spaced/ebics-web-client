/**
 * Generic error (must not be from the REST)
 */
export interface ApiError {
  timestamp: string;
  error: string;
  message: string;
}

/**
 * Any error from the EWC backend
 */
export interface EbicsApiError extends ApiError {
  causeMessage?: string;
  exceptionClass: string;
  status: number;
}

/**
 * Ebics Server error (either HTTP or EBICS error)
 */
export interface EbicsServerApiError extends EbicsApiError {
  errorCode: string;
  errorCodeText: string;
}