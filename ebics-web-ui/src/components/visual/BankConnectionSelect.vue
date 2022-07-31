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
    <template v-slot:option="bankConnectionVal">
      <q-item v-bind="bankConnectionVal.itemProps">
        <q-item-section avatar>
          <q-icon v-if="bankConnectionVal.opt.guestAccess" name="lock_open" />
          <q-icon v-else name="lock" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ bankConnectionVal.opt.name }}</q-item-label>
          <q-item-label caption>{{
            `${bankConnectionVal.opt.userId} | ${bankConnectionVal.opt.partner?.partnerId}`
          }}</q-item-label>
          <q-item-label
            v-if="
              bankConnectionVal.opt?.status &&
              bankConnectionVal.opt?.status?.health == HealthStatusType.Error
            "
            caption
            class="text-negative"
          >
            Status: The connection is erroneous.
          </q-item-label>
          <q-item-label
            v-if="
              bankConnectionVal.opt?.status &&
              bankConnectionVal.opt?.status?.health == HealthStatusType.Warning
            "
            caption
            class="text-warning"
          >
            Status: The connection has some issues -
            {{ bankConnectionVal.opt.status.okRate }}% OK rate.
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
import { HealthStatusType } from 'components/models/allivenes-health-status';

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
    return { bankConnectionLabel, bankConnectionVal, HealthStatusType };
  },
});
</script>
