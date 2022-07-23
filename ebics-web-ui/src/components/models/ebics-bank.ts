import { EbicsVersion } from 'components/models/ebics-version';

export interface EbicsVersionSettings {
    version: EbicsVersion;
    isSupportedByBank: boolean;
    isSupportedByClient: boolean;
    isAllowedForUse: boolean;
    isPreferredForUse: boolean;
  }
  
export interface Bank {
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