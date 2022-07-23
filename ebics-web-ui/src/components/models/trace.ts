import { EbicsVersion } from 'components/models/ebics-version';
import { BankConnection } from 'components/models/ebics-bank-connection';
import { BtfService } from 'src/components/models/btf-service';


export enum TraceType {
  EbicsEnvelope = 'EbicsEnvelope',
  Content = 'Content'
}

export interface OrderTypeDefinition {
  adminOrderType: string;
  ebicsServiceType?: BtfService;
  businessOrderType?: string;
}

export interface TraceEntry {
  id: number;
  messageBody: string;
  user: BankConnection;
  creator: string;
  dateTime: Date;
  sessionId: string;
  orderNumber?: string;
  ebicsVesion: EbicsVersion;
  upload: boolean;
  traceType: TraceType;
  orderType: OrderTypeDefinition;
}
