import { ApiError, EbicsApiError, EbicsServerApiError } from 'components/models/ebics-api-error';
import { useQuasar } from 'quasar';
import { AxiosError } from 'axios';
import { ConnectionStatus } from 'components/models/connection-status';
import useHealthAllivenessStatusAPI from 'components/health-status-api';

function isAxiosError<T>(error: unknown): error is AxiosError<T> {
  return (error as AxiosError).isAxiosError !== undefined;
}

export function isEbicsApiError(error: ApiError): error is EbicsApiError {
  return (error as EbicsApiError).exceptionClass !== undefined;
}

export function isEbicsServerApiError(error: ApiError): error is EbicsServerApiError {
  return (error as EbicsServerApiError).errorCode !== undefined;
}

function arrayBufferToObject<T>(ab: ArrayBuffer): T | undefined {
  const text = String.fromCharCode.apply(null, Array.from(new Uint8Array(ab)));
  if (!text) return undefined;
  return JSON.parse(text) as T;
}

/**
 * Parse the ApiError or EbicsApiError out of axios reponse object
 * @param error axios response object
 * @returns EbicsApiError if it was returned, otherwise ApiError
 */
function errorResponseToApiError(error: unknown): ApiError {
  if (isAxiosError<EbicsApiError>(error)) {
    if (error.response) {
      const ebicsApiError = error.response?.data;
      if (error.config?.responseType == 'arraybuffer') {
        //In this case we requesed arraybuffer (not JSON object) so the data must be first extracted
        return arrayBufferToObject<EbicsApiError>(error.response?.data as unknown as ArrayBuffer) as EbicsApiError
      } else {
        //We already have proper object EbicsApiError
        return ebicsApiError;
      }
    } else {
      //There is no real response data, but it is still axios error
      return {timestamp: new Date().toISOString(), message: JSON.stringify(error.message)} as ApiError
    }
  } else {
    //Non axios error
    return {timestamp: new Date().toISOString(), message: JSON.stringify(error)} as ApiError
  }
}

export function getFormatedErrorMessage(apiError: ApiError | EbicsApiError | EbicsServerApiError): string {
  if (isEbicsServerApiError(apiError)) {
    if (apiError.causeMessage)
      return `${apiError.message} caused by: ${apiError.causeMessage} of type: ${apiError.exceptionClass}`
    else
    return `${apiError.message} of type: ${apiError.exceptionClass}`
  } else if (isEbicsApiError(apiError)) {
    if (apiError.causeMessage)
      return `${apiError.message} caused by: ${apiError.causeMessage} of type: ${apiError.exceptionClass}`
    else
    return `${apiError.message} of type: ${apiError.exceptionClass}`
  } else {
    return apiError.message;
  }
}

export function getFormatedErrorCategory(apiError: ApiError | EbicsApiError): string {
  if (isEbicsApiError(apiError)) {
    return `HTTP ${apiError.httpStatusCode} ${apiError.httpStatusResonPhrase}`
  } else {
    //Its not from REST API, it comes from the frontend itself, for example backend is available
    return 'Frontend error';
  }
}

export default function useBaseAPI() {
  const q = useQuasar();
  const { updateHealthStatus } = useHealthAllivenessStatusAPI();

  const apiOkHandler = (msg: string, connectionStatus?: ConnectionStatus, silent?: boolean,): void => {
    updateHealthStatus(connectionStatus);
    if (silent != true) {
      q.notify({
        color: 'positive',
        position: 'bottom-right',
        message: msg,
        icon: 'gpp_good',
      });
    }
  };

  /**
   * REST API Error Handler
   * - Log the whole error in console
   * - Notify user with some readable error message
   * @param msg context message, for example 'user A initialization'
   * @param error REST API call error
   * @param apiErrorCallback optional callback overload handling of business error if needed
   */
  const apiErrorHandler = (
    msg: string,
    error: unknown,
    connectionStatus?: ConnectionStatus,
    apiErrorCallback: undefined | ((errorMessage: string) => void) = undefined,
    silent?: boolean,
  ): void => {
    console.error('API Exception: ' + JSON.stringify(error));
    const apiError = errorResponseToApiError(error);
    if (isTheApiErrorActuallyOk(apiError)) {
      apiOkHandler(remapMsgForOkErrors(apiError, msg), connectionStatus, silent);
    } else {
      updateHealthStatus(connectionStatus, error, apiError);
      if (silent != true) {
        const errorMessage = getFormatedErrorMessage(apiError);
        if (apiErrorCallback) apiErrorCallback(errorMessage);
        q.notify({
          color: 'negative',
          position: 'bottom-right',
          message: `${msg} '${errorMessage}'`,
          closeBtn: true,
          icon: 'report_problem',
          timeout: 10000,
        });
      }
    }
  };

  /**
   * Indicates if the Error should be threated as OK
   * @param apiError
   * @returns
   */
  const isTheApiErrorActuallyOk = (apiError: ApiError): boolean => {
    return (apiError.message.includes('EBICS_NO_DOWNLOAD_DATA_AVAILABLE'));
  }

  /**
   * Return new msg for error which was assesed as OK by isTheApiErrorActuallyOk
   * @param apiError
   * @param msg
   * @returns
   */
  const remapMsgForOkErrors = (apiError: ApiError, msg: string): string => {
    if (apiError.message.includes('EBICS_NO_DOWNLOAD_DATA_AVAILABLE'))
      return 'No download data available on the EBICS server (EBICS_NO_DOWNLOAD_DATA_AVAILABLE)';
    else
      return msg;
  }

  return {
    apiOkHandler,
    apiErrorHandler,
  };
}
