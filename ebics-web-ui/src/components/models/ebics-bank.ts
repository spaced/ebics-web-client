import { EbicsVersion } from 'components/models/ebics-version';
import { ConnectionStatus } from './allivenes-health-status';

export interface EbicsVersionSettings {
    version: EbicsVersion;
    isSupportedByBank: boolean;
    isSupportedByClient: boolean;
    isAllowedForUse: boolean;
    isPreferredForUse: boolean;
  }
  
export interface Bank extends ConnectionStatus {
    id: number;
    bankURL: string;
    name: string;
    hostId: string;
    keyStore: {
      id: number;
      e002DigestHex: string;
      x002DigestHex: string;
      e002CertDigestHex: string;
      x002CertDigestHex: string;
    }
    ebicsVersions: EbicsVersionSettings[];
    httpClientConfigurationName: string;
  }