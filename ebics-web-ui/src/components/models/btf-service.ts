
export interface BtfService {
  serviceName: string;
  serviceOption?: string;
  scope?: string;
  containerType?: string;
  message: BtfMessage;
}

export interface BtfMessage {
  messageName: string;
  messageNameVariant?: string;
  messageNameVersion?: string;
  messageNameFormat?: string;
}
