<template>
  <div v-if="bankConnectionVal" class="q-gutter-sm">
    <q-radio
      v-model="bankConnectionVal.ebicsVersion"
      :disable="
        !isEbicsVersionAllowedForUse(
          bankConnectionVal.partner.bank,
          EbicsVersion.H004
        )
      "
      val="H004"
      label="EBICS 2.5 (H004)"
    />
    <q-radio
      v-model="bankConnectionVal.ebicsVersion"
      :disable="
        !isEbicsVersionAllowedForUse(
          bankConnectionVal.partner.bank,
          EbicsVersion.H005
        )
      "
      val="H005"
      label="EBICS 3.0 (H005)"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, computed, PropType } from 'vue';
import { BankConnection } from 'components/models/ebics-bank-connection';
import useBanksAPI from 'components/banks';
import { EbicsVersion } from 'components/models/ebics-version';

export default defineComponent({
  name: 'EbicsVersionRadios',
  props: {
    bankConnection: {
      type: Object as PropType<BankConnection>,
      required: false,
    },
  },
  emits: ['update:bankConnection'],
  setup(props, { emit }) {
    const { isEbicsVersionAllowedForUse } = useBanksAPI(true);
    const bankConnectionVal = computed<BankConnection>({
      get() {
        return props.bankConnection as BankConnection;
      },
      set(value) {
        emit('update:bankConnection', value);
      },
    });
    return { bankConnectionVal, isEbicsVersionAllowedForUse, EbicsVersion };
  },
});
</script>
