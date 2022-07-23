import { EbicsVersion } from 'components/models/ebics-version';
import { Partner } from 'components/models/ebics-partner';

export interface BankConnection {
    id: number;
    ebicsVersion: EbicsVersion;
    userId: string;
    name: string;
    dn: string;
    userStatus: string;
    partner: Partner;
    keyStore: string;
    usePassword: boolean;
    guestAccess: boolean;
    creator: string;
    securityMedium: string;
    useCertificate: boolean;
  }

  export interface UserPartnerBank {
    ebicsVersion: EbicsVersion;
    userId: string;
    name: string;
    dn: string;
    partnerId: string;
    bankId: number;
    guestAccess: boolean;
    useCertificate: boolean;
  }

  export enum BankConnectionAccess {
    READ = 'READ',
    WRITE = 'WRITE',
    USE = 'USE',
  }