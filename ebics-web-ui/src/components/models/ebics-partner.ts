import { Bank } from 'components/models/ebics-bank';

export interface Partner {
    id: number;
    partnerId: string;
    bank: Bank;
  }

