import { BtfService } from 'components/models/btf-service';

export enum AdminOrderType {
  INI = 'INI',
  HIA = 'HIA',
  HPB = 'HPB',
  SPR = 'SPR',
}

export enum AuthorisationLevel {
  T, A, B, E
}
/**
 * H005 order type description
 */

export interface BTFType {
  adminOrderType: string;
  service?: BtfService;
  description?: string;
  authorizationLevel?: AuthorisationLevel;
  numSigRequired?: BigInteger;
}
/**
 * H003, H004 order type description
 */

export interface OrderType {
  adminOrderType: string;
  orderType: string;
  transferType?: TransferType;
  description?: string;
  authorizationLevel?: AuthorisationLevel;
  numSigRequired?: BigInteger;
}

export enum TransferType {
  Upload, Download
}

export enum OrderTypeFilter {
  UploadOnly,
  DownloadOnly,
  All
}
