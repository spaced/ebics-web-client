import {
  ConnectionStatusObject,
  HealthStatusType,
} from './models/allivenes-health-status';
import { ConnectionStatus } from './models/connection-status';
import {
  ApiResponse,
  ApiResponseType,
  RelatedObjectReference,
  RelatedObjectType,
} from './models/api-response';
import { BankConnection } from './models/ebics-bank-connection';
import { Bank } from './models/ebics-bank';
import { ApiError } from './models/ebics-api-error';

function isBankConnection(
  connectionStatus: ConnectionStatus
): connectionStatus is BankConnection {
  return (connectionStatus as BankConnection).dn !== undefined;
}

function isBank(connectionStatus: ConnectionStatus): connectionStatus is Bank {
  return (connectionStatus as Bank).bankURL !== undefined;
}

function connectionStatusToRelatedObjectReference(
  connectionStatus?: ConnectionStatus
): RelatedObjectReference[] {
  if (connectionStatus) {
    if (isBankConnection(connectionStatus)) {
      return [
        {
          id: connectionStatus.id,
          type: RelatedObjectType.BankConnection,
        },
        {
          id: connectionStatus.partner.bank.id,
          type: RelatedObjectType.Bank,
        },
      ];
    } else if (isBank(connectionStatus))
      return [
        {
          id: connectionStatus.id,
          type: RelatedObjectType.Bank,
        },
      ];
    else
      throw new Error(
        "The current implementation doesn't support this type of connection status" +
          JSON.stringify(connectionStatus)
      );
  } else {
    return [{ type: RelatedObjectType.Global }];
  }
}

/**
 * This method is updating the the backend statistics with the fresh frontend errors and OKs
 * @param relatedObjectReference 
 * @param minOkRate 
 * @param minErrorRate 
 * @param backendStatus 
 * @returns 
 */
function createFrontendConnectionStatus(
  relatedObjectReference: RelatedObjectReference,
  backendStatus: ConnectionStatusObject,
  minOkRate = 90,
  minErrorRate = 50,
): ConnectionStatusObject {
  const apiResponses = apiResponsesCache.get(
    referenceToKey(relatedObjectReference)
  );
  if (apiResponses) {
    const backendStatusTimestampMilis = new Date(backendStatus.actualStatisticsFrom).getTime();
    //Filter out the frontent events with the higher timestamps than backend (only those would be considered for update)
    const latestResponses = apiResponses.filter(
      (apiResponse) =>
        apiResponse.timeStamp.getTime() > backendStatusTimestampMilis
    );

    //Add the frontent numbers to backend numbers
    const okCount = latestResponses.filter(
      (apiResponse) => apiResponse.status == ApiResponseType.Ok
    ).length + backendStatus.okCount;
    const errorCount = latestResponses.filter(
      (apiResponse) => apiResponse.status == ApiResponseType.Error
    ).length + backendStatus.errorCount;

    const totalCount = okCount + errorCount;
    let healthStatus = HealthStatusType.Unknown;
    if (totalCount == 0) {
      return { healthStatus: HealthStatusType.Unknown } as ConnectionStatusObject;
    } else {
      const okRate = (okCount / totalCount) * 100;
      const errorRate = (errorCount / totalCount) * 100;
      if (okRate > minOkRate) {
        healthStatus = HealthStatusType.Ok;
      } else if (errorRate > minErrorRate) {
        healthStatus = HealthStatusType.Error;
      } else {
        healthStatus = HealthStatusType.Warning;
      }
      const status = {
        healthStatus: healthStatus,
        okCount: okCount,
        okRate: okRate,
        errorCount: errorCount,
        errorRate: errorRate,
        totalCount: totalCount,
      } as ConnectionStatusObject;
      console.log('Original backend status: ' + JSON.stringify(backendStatus))
      console.log('Updated frontend status: ' + JSON.stringify(status));
      return status;
    }
  } else {
    return { healthStatus: HealthStatusType.Unknown } as ConnectionStatusObject;
  }
}

//Global internal cache of all api responses for all active bank connections..
const apiResponsesCache: Map<string, ApiResponse[]> = new Map<
  string,
  ApiResponse[]
>();

function referenceToKey(
  relatedObjectReference: RelatedObjectReference
): string {
  return JSON.stringify(relatedObjectReference);
}

export default function useHealthAllivenessStatusAPI() {
  /**
   * This method updates the global cache of all api responses @apiResponse
   * And recalculate connectionStatus
   * @param connectionStatus connection status to be updated (referes to BankConnection or Bank or unknown for global)
   * @param error the error object if available, for ok reponses is empty
   */
  const updateHealthStatus = (
    connectionStatus?: ConnectionStatus,
    error?: unknown,
    apiError?: ApiError,
  ): void => {
    console.log('Updating connection status');
    const relatedObjectReferences: RelatedObjectReference[] =
      connectionStatusToRelatedObjectReference(connectionStatus);
    const apiResponse = {
      status: apiError ? ApiResponseType.Error : ApiResponseType.Ok,
      error: error,
      apiError: apiError,
      timeStamp: new Date(),
    } as ApiResponse;
    relatedObjectReferences.forEach(
      (relatedObjectReference: RelatedObjectReference) => {
        let apiResponseList = apiResponsesCache.get(
          referenceToKey(relatedObjectReference)
        );
        if (apiResponseList == undefined) {
          apiResponseList = [];
          apiResponsesCache.set(
            referenceToKey(relatedObjectReference),
            apiResponseList
          );
        }
        apiResponseList.push(apiResponse);
        if (connectionStatus) {
          const newConnectionStatus = createFrontendConnectionStatus(
            relatedObjectReference, connectionStatus.backendStatus
          );
          connectionStatus.frontendStatus = newConnectionStatus;
          connectionStatus.lastError = apiError;
        }
      }
    );
  };

  return {
    updateHealthStatus, apiResponsesCache, referenceToKey
  };
}
