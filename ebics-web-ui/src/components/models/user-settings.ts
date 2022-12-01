export interface AutoAdjustmentsPain00x {
  msgId: boolean;
  pmtInfId: boolean;
  instrId: boolean;
  endToEndId: boolean;
  uetr: boolean;
  reqdExctnDt: boolean;
  creDtTm: boolean;
  nbOfTrxsCalc: boolean;
  ctrlSumCalc: boolean;
  idPrefix: string;
}

export interface AutoAdjustmentsSwift {
  uetr: boolean;
  f20: boolean;
  f21: boolean;
  f30: boolean;
  idPrefix: string;
  randomIds: boolean;
}

export interface UserSettings {
  uploadOnDrop: boolean;
  testerSettings: boolean;
  adjustmentOptions: {
    applyAutomatically: boolean;
    pain00x: AutoAdjustmentsPain00x;
    swift: AutoAdjustmentsSwift;
  };
  displayAdminTypes: boolean;
  displaySharedBankConnections: boolean;
  displayErroneousConnections: boolean;
  displaySharedTemplates: boolean;
  displayPredefinedTemplates: boolean;
}
