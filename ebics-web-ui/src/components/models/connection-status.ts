import { ApiError } from './ebics-api-error';
import { ConnectionStatusObject } from './allivenes-health-status';


export interface ConnectionStatus {
    backendStatus: ConnectionStatusObject;
    frontendStatus: ConnectionStatusObject;
    lastError?: ApiError;
}
