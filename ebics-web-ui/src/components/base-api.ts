import { ApiError, EbicsApiError } from 'components/models/ebics-api-error';
import { useQuasar } from 'quasar';
import { AxiosError } from 'axios';
import { ConnectionStatus } from './models/connection-status';
import useHealthAllivenessStatusAPI from './health-status-api';

function isAxiosError<T>(error: unknown): error is AxiosError<T> {
  return (error as AxiosError).isAxiosError !== undefined;
}

export function isEbicsApiError(error: ApiError): error is EbicsApiError {
  return (error as EbicsApiError).timestamp !== undefined;
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
      if (error.config.responseType == 'arraybuffer') {
        //In this case we requesed arraybuffer (not JSON object) so the data must be first extracted
        return arrayBufferToObject<EbicsApiError>(error.response?.data as unknown as ArrayBuffer) as EbicsApiError
      } else {
        //We already have proper object EbicsApiError
        return ebicsApiError;
      }
    } else { 
      //There is no real response data, but it is still axios error
      return {timestamp: new Date().toISOString(), error: 'Axios error without response data', message: JSON.stringify(error.message)} as ApiError
    } 
  } else {
    //Non axios error
    return {timestamp: new Date().toISOString(), error: 'Generall error', message: JSON.stringify(error)} as ApiError
  }
}

export function getFormatedErrorMessage(ebicsApiError: ApiError | EbicsApiError): string {
  console.log('formating error message');
  if (isEbicsApiError(ebicsApiError)) {
    const message = ebicsApiError.message;
    if (ebicsApiError.description && !ebicsApiError.description.includes(ebicsApiError.message))
      return `message: ${message} description: ${ebicsApiError.description}`;
    else 
      return ebicsApiError.message;
  } else {
    return ebicsApiError.message;
  }
}

export function getFormatedErrorCategory(apiError: ApiError | EbicsApiError): string {  
  if (isEbicsApiError(apiError)) {
    return `HTTP ${apiError.status} ${apiError.error}` 
  } else {
    return apiError.error;
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
