export interface ApiError {
  timestamp: string;
  error: string;
  message: string;
}

export interface EbicsApiError extends ApiError {
  description: string;

  /**
   * HTTP Code
   */
  status: number;
}
