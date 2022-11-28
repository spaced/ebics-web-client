<template>
  <div>
    <q-list bordered padding>
      <div v-if="displaySection('General')">
        <q-item-label header>General Settings</q-item-label>
        <boolean-option
          label="Tester settings"
          hint="Enable smart adjustments of uploaded files"
          v-model="userSettings.testerSettings"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label="Adjust file authomatically"
          hint="Apply smart adjustmets as bellow for every uploaded file automatically, if disabled you can still apply adjustmets explicitelly"
          v-model="userSettings.adjustmentOptions.applyAutomatically"
        />
        <q-separator spaced />
      </div>
      <div v-if="displaySection('ContentOptions.Pain.00x')">
        <q-item-label header>Smart adjustments for Pain.00x</q-item-label>
        <boolean-option
          :disable="!userSettings.testerSettings"
          label="msgId"
          hint="unique id based on current timestamp and user id"
          v-model="userSettings.adjustmentOptions.pain00x.msgId"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label="pmtInfId"
          hint="unique id based on current timestamp, user id and B-Level"
          v-model="userSettings.adjustmentOptions.pain00x.pmtInfId"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label="endToEndId"
          hint="unique id based on current timestamp, user id and B/C-Level"
          v-model="userSettings.adjustmentOptions.pain00x.endToEndId"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label="instrId"
          hint="unique id based on current timestamp, user id and B/C-Level"
          v-model="userSettings.adjustmentOptions.pain00x.instrId"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label="UETR for GPI"
          :hint="`unique UETR id based on random seed: ${this.uetr()}`"
          v-model="userSettings.adjustmentOptions.pain00x.uetr"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label="creDtTm"
          :hint="`actual date-time in ISO format: ${new Date().toISOString()}`"
          v-model="userSettings.adjustmentOptions.pain00x.creDtTm"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label="reqdExctnDt"
          :hint="`actual date in YYYY-MM-DD format: ${this.currentDate()}`"
          v-model="userSettings.adjustmentOptions.pain00x.reqdExctnDt"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label="nbOfTrxs"
          hint="recalculates number of transaction based on C-Levels"
          v-model="userSettings.adjustmentOptions.pain00x.nbOfTrxsCalc"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label="ctrlSum"
          hint="recalculates control sum based on C-Level amouths"
          v-model="userSettings.adjustmentOptions.pain00x.ctrlSumCalc"
        />
      </div>
      <q-separator
        spaced
        v-if="
          displaySection('ContentOptions.Pain.00x') &&
          displaySection('ContentOptions.Swift')
        "
      />
      <div v-if="displaySection('ContentOptions.Swift')">
        <q-item-label header>Smart adjustments for MT101</q-item-label>
        <boolean-option
          :disable="!userSettings.testerSettings"
          label=":20 (Message ID)"
          hint="unique id based on prefix and current timestamp (or random)"
          v-model="userSettings.adjustmentOptions.swift.f20"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label=":21 (Transaction ID)"
          hint="unique id based on prefix and current timestamp (or random)"
          v-model="userSettings.adjustmentOptions.swift.f21"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label=":30 (Requested Execution Date)"
          hint="Todays date"
          v-model="userSettings.adjustmentOptions.swift.f30"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label="UETR for GPI"
          :hint="`unique UETR id based on random seed: ${this.uetr()}`"
          v-model="userSettings.adjustmentOptions.swift.uetr"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label="Random IDs"
          :hint="`IDs for fields :20 and :21 are based on random generator: ${makeUniqueId(
            16
          )}`"
          v-model="userSettings.adjustmentOptions.swift.randomIds"
        />
      </div>
      <q-separator
        spaced
        v-if="
          displaySection('ContentOptions.Swift') &&
          displaySection('OrderTypesSettings') 
        "
      />
      <div v-if="displaySection('OrderTypesSettings') || displaySection('BankConnectionsSettings')">
        <q-item-label v-if="displaySection('OrderTypesSettings')" header>Order type settings</q-item-label>
        <boolean-option v-if="displaySection('OrderTypesSettings')"
          :disable="!userSettings.testerSettings"
          label="Display administrative order types for Download"
          hint="Admin order types like HTD, HAA, HKD,.. will be included into list of downloadable order types"
          v-model="userSettings.displayAdminTypes"
        />
        <q-item-label v-if="displaySection('BankConnectionsSettings')" header>Bank Connections Settings</q-item-label>
        <boolean-option v-if="displaySection('BankConnectionsSettings')"
          label="Display shared bank connections"
          hint="If enabled, the shared connections are listed as well, If
                  disabled, only your private connections are listed."
          v-model="userSettings.displaySharedBankConnections"
        />
        <boolean-option v-if="displaySection('BankConnectionsSettings')"
          label="Display errorneous bank connections"
          hint="If enabled, the errorneous bank connections are listed as well, If
                  disabled, only OK connections are listed."
          v-model="userSettings.displayErroneousConnections"
        />
      </div>
      <!-- FileTemplateSettings -->
      <div v-if="displaySection('FileTemplateSettings')">
        <q-item-label v-if="displaySection('FileTemplateSettings')" header>Upload templates setting</q-item-label>
        <boolean-option v-if="displaySection('FileTemplateSettings')"
          :disable="!userSettings.testerSettings"
          label="Display shared templates"
          hint="Shared templates from other users will be displayed, if disable only own templates are shown"
          v-model="userSettings.displaySharedTemplates"
        />
        <boolean-option v-if="displaySection('FileTemplateSettings')"
          :disable="!userSettings.testerSettings"
          label="Display predefined templates"
          hint="Predefined templates from the EBICS Web Client will be displayed, if disable only the user defined templates are shown"
          v-model="userSettings.displayPredefinedTemplates"
        />
      </div>
    </q-list>
    <div class="q-ma-md">
      <q-btn
        v-if="saveButton"
        label="Save settings"
        color="primary"
        @click="saveUserSettings"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import BooleanOption from 'components/visual/BooleanOption.vue';
import useTextUtils from 'components/text-utils';
import useUserSettingsAPI from 'components/user-settings';
import { uuid } from 'vue-uuid';

export default defineComponent({
  name: 'ContentAdjustmenOption',
  components: { BooleanOption },
  props: {
    sectionFilter: {
      type: String,
      requred: false,
      default: '',
    },
    saveButton: {
      type: Boolean,
      requred: false,
      default: false,
    },
  },
  setup(props) {
    const uetr = (): string => {
      return uuid.v4();
    }
    const displaySection = (sectionName: string): boolean => {
      return (
        props.sectionFilter == '' || sectionName.includes(props.sectionFilter)
      );
    }
    const { saveUserSettings, userSettings } = useUserSettingsAPI();
    const { currentDate, makeUniqueId } = useTextUtils();
    return { currentDate, makeUniqueId, userSettings, saveUserSettings, uetr, displaySection };
  },
});
</script>
