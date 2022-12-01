import { ConnectionStatusObject } from 'components/models/allivenes-health-status';


export interface ConnectionStatus {
    backendStatus: ConnectionStatusObject;
    frontendStatus: ConnectionStatusObject;
}
