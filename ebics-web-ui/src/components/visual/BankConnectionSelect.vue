<template>
  <q-select
    filled
    v-model="bankConnectionVal"
    :options="bankConnections"
    :option-label="bankConnectionLabel"
    label="EBICS Bank connection"
    hint="Select EBICS bank connection"
    lazy-rules
    :rules="[
      (val) => bankConnectionVal || 'Please select valid EBICS bank connection',
    ]"
  >
    <template v-slot:append>
      <q-btn
        round
        dense
        flat
        icon="settings"
        @click.stop="configureBankConnectionsDropdown = true;"
      > 
      <q-tooltip>
          Click to adjust filter to display/hide erroneous or shared connections..
        </q-tooltip>
      </q-btn>
    </template>
    <q-dialog v-model="configureBankConnectionsDropdown">
      <q-card>
        <q-card-section>
          <div class="text-h6">Adjust bank connection filter settings</div>
        </q-card-section>

        <user-preferences sectionFilter="BankConnectionsSettings"/>

        <q-card-actions align="right">
          <q-btn flat label="OK" color="primary" v-close-popup @click="saveUserSettings()"/>
        </q-card-actions>
      </q-card>
    </q-dialog>
    <template v-slot:option="bankConnectionItem">
      <q-item v-bind="bankConnectionItem.itemProps">
        <q-item-section avatar>
          <q-icon v-if="bankConnectionItem.opt.guestAccess" name="lock_open" />
          <q-icon v-else name="lock" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ bankConnectionItem.opt.name }}</q-item-label>
          <q-item-label caption>{{
            `${bankConnectionItem.opt.userId} | ${bankConnectionItem.opt.partner?.partnerId}`
          }}</q-item-label>
          <q-item-label
            v-if="bankConnectionItem.opt?.frontendStatus || bankConnectionItem.opt?.backendStatus"
            caption
            :class="getBankConnectionStatusLabel(bankConnectionItem.opt).labelClass"
          >
            {{ getBankConnectionStatusLabel(bankConnectionItem.opt).labelText }}
          </q-item-label>
        </q-item-section>
      </q-item>
    </template>
  </q-select>
</template>

<script lang="ts">
import { defineComponent, computed, ref } from 'vue';
import { BankConnection } from 'components/models/ebics-bank-connection';
import { bankConnectionLabel } from 'components/bankconnections';
import { ConnectionStatusObject, HealthStatusType } from 'components/models/allivenes-health-status';
import UserPreferences from 'components/visual/UserPreferences.vue'
import useUserSettingsAPI from 'components/user-settings';

interface LabelTextAndClass {
  labelText: string,
  labelClass: string,
}

export default defineComponent({
  name: 'BankConnectionSelect',
  components: { UserPreferences },
  props: {
    bankConnections: {
        type: Object,
        required: true,
    },
    bankConnection: {
        type: Object,
        required: false,
    },
  },
  emits: ['update:modelValue', 'update:bankConnection'],
  setup(props, { emit }) {
    const bankConnectionVal = computed<BankConnection>({
      get() {
        return props.bankConnection as BankConnection;
      },
      set(value) {
        emit('update:bankConnection', value);
      },
    });

    const getBankConnectionStatus = (bankConnection: BankConnection): ConnectionStatusObject => {
      return bankConnection.frontendStatus ? bankConnection.frontendStatus : bankConnection.backendStatus;
    } 
    const getBankConnectionStatusLabel = (bankConnection: BankConnection | undefined): LabelTextAndClass => {
      if (bankConnection) {
        const connectionStatus = getBankConnectionStatus(bankConnection);
        switch(connectionStatus.healthStatus) {
          case HealthStatusType.Ok:
            return {labelText: 'The connection status is OK.', labelClass: 'text-positive' };
          case HealthStatusType.Error:
            return {labelText: 'The connection is erroneous.', labelClass: 'text-negative' };
          case HealthStatusType.Warning:
            return {labelText: `The connection has some issues - ${connectionStatus.okRate.toFixed(0)}% OK rate.`, labelClass: 'text-warning' };
          default:
            return {labelText: 'The connection status is unknown.', labelClass: 'text-grey' };
        }
      } else {
        return {labelText: 'Bank connection not given', labelClass: 'text-warning' };
      }
    } 
    const { saveUserSettings } = useUserSettingsAPI();
    const configureBankConnectionsDropdown = ref(false);
    return { bankConnectionLabel, bankConnectionVal, HealthStatusType, getBankConnectionStatusLabel, configureBankConnectionsDropdown, saveUserSettings};
  },
});
</script>
