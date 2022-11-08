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
import { defineComponent, computed } from 'vue';
import { BankConnection } from 'components/models/ebics-bank-connection';
import { bankConnectionLabel } from 'components/bankconnections';
import { ConnectionStatusObject, HealthStatusType } from 'components/models/allivenes-health-status';

interface LabelTextAndClass {
  labelText: string,
  labelClass: string,
}

export default defineComponent({
  name: 'BankConnectionSelect',
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
            return {labelText: 'Status: The connection is OK.', labelClass: 'text-positive' };
          case HealthStatusType.Error:
            return {labelText: 'Status: The connection is erroneous.', labelClass: 'text-negative' };
          case HealthStatusType.Warning:
            return {labelText: `Status: The connection has some issues - ${connectionStatus.okRate}% OK rate.`, labelClass: 'text-warning' };
          default:
            return {labelText: 'Status: The connection is Unknown!!.', labelClass: 'text-warning' };
        }
      } else {
        return {labelText: 'Bank connection not given', labelClass: 'text-warning' };
      }
    } 
    return { bankConnectionLabel, bankConnectionVal, HealthStatusType, getBankConnectionStatusLabel };
  },
});
</script>
