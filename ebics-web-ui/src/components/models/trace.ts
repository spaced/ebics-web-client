import { EbicsVersion } from 'components/models/ebics-version';
import { BankConnectionId } from 'components/models/ebics-bank-connection';
import { BtfService } from 'src/components/models/btf-service';
import { Bank } from './ebics-bank';


export enum TraceType {
  EbicsEnvelope = 'EbicsEnvelope',
  Content = 'Content'
}

export enum TraceCategory {
  Request = 'Request',
  HttpResponseOk = 'HttpResponseOk',
  HttpResponseError = 'HttpResponseError',
  EbicsResponseOk = 'EbicsResponseOk',
  EbicsResponseError = 'EbicsResponseError',
  GeneralError = 'GeneralError',
}

export interface OrderTypeDefinition {
  adminOrderType: string;
  ebicsServiceType?: BtfService;
  businessOrderType?: string;
}

export interface TraceEntry {
  id: number;
  textMessageBody: string;
  bankConnection?: BankConnectionId;
  bank?: Bank;
  creator: string;
  dateTime: Date;
  sessionId: string;
  orderNumber?: string;
  ebicsVesion: EbicsVersion;
  upload: boolean;
  request: boolean;
  traceType: TraceType;
  orderType: OrderTypeDefinition;
  traceCategory?: TraceCategory;
  errorCode?: string,
  errorCodeText?: string,
  errorMessage?: string,
  errorStackTrace?: string,
}
