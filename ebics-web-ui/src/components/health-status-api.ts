import {
  ConnectionStatus,
  ConnectionStatusObject,
  HealthStatusType,
} from './models/allivenes-health-status';
import {
  ApiResponse,
  ApiResponseType,
  RelatedObjectReference,
  RelatedObjectType,
} from './models/api-response';
import { BankConnection } from './models/ebics-bank-connection';
import { Bank } from './models/ebics-bank';

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

function calculateConnectionStatus(
  relatedObjectReference: RelatedObjectReference,
  minOkRate = 90,
  minErrorRate = 50,
  notOlderThanMinutes = 30
): ConnectionStatusObject {
  const apiResponses = apiResponsesCache.get(
    referenceToKey(relatedObjectReference)
  );
  if (apiResponses) {
    const notOlderThanMilis = notOlderThanMinutes * 60 * 1000;
    const actualTimeStampMilis = new Date().getTime();
    const latestResponses = apiResponses.filter(
      (apiResponse) =>
        Math.abs(apiResponse.timeStamp.getTime() - actualTimeStampMilis) <
        notOlderThanMilis
    );
    const okCount = latestResponses.filter(
      (apiResponse) => apiResponse.status == ApiResponseType.Ok
    ).length;
    const errorCount = latestResponses.filter(
      (apiResponse) => apiResponse.status == ApiResponseType.Error
    ).length;
    const totalCount = okCount + errorCount;
    let healthStatus = HealthStatusType.Unknown;
    if (totalCount == 0) {
      return { health: HealthStatusType.Unknown } as ConnectionStatusObject;
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
        health: healthStatus,
        okCount: okCount,
        okRate: okRate,
        errorCount: errorCount,
        errorRate: errorRate,
        totalCount: totalCount,
      } as ConnectionStatusObject;
      console.log(JSON.stringify(status));
      return status;
    }
  } else {
    return { health: HealthStatusType.Unknown } as ConnectionStatusObject;
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
    error?: unknown
  ): void => {
    console.log('Updating connection status');
    const relatedObjectReferences: RelatedObjectReference[] =
      connectionStatusToRelatedObjectReference(connectionStatus);
    const apiResponse = {
      status: error ? ApiResponseType.Error : ApiResponseType.Ok,
      error: error,
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
        const newConnectionStatus = calculateConnectionStatus(
          relatedObjectReference
        );
        if (connectionStatus) {
          connectionStatus.status = newConnectionStatus;
        }
      }
    );
  };

  return {
    updateHealthStatus,
  };
}
