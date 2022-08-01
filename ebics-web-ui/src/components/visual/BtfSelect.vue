<template>
  <q-select
    v-if="bankConnection?.ebicsVersion == 'H005'"
    filled
    v-model="btfTypeVal"
    :options="btfTypes"
    :option-label="(t) => btfTypeLabel(t)"
    label="Business Transaction Format"
    hint="Select EBICS Business Transaction Format (BTF)"
    lazy-rules
    :rules="[(val) => val || 'Please select valid EBICS BTF']"
  >
    <template v-slot:append>
      <q-btn
        round
        dense
        flat
        icon="refresh"
        @click.stop="emit('click:refreshBtfTypes')"
      />
    </template>
    <template v-slot:option="scope">
      <q-item v-bind="scope.itemProps">
        <q-item-section>
          <q-item-label v-html="btfTypeLabel(scope.opt)" />
          <q-item-label caption>{{
            scope.opt.description
          }}</q-item-label>
        </q-item-section>
      </q-item>
    </template>
  </q-select>
</template>

<script lang="ts">
import { defineComponent, computed, PropType } from 'vue';
import { BankConnection } from 'components/models/ebics-bank-connection';
import { BTFType } from 'components/models/ebics-order-type';
import useOrderTypeLabelAPI from 'components/order-type-label';

export default defineComponent({
  name: 'OrderTypeSelect',
  props: {
    btfType: {
      type: Object as PropType<BTFType>,
      required: false,
    },
    btfTypes: {
      type: Object,
      required: true,
    },
    bankConnection: {
      type: Object as PropType<BankConnection>,
      required: false,
    },
  },
  emits: ['update:btfType', 'click:refreshBtfTypes'],
  setup(props, { emit }) {
    const { btfTypeLabel } = useOrderTypeLabelAPI();
    const btfTypeVal = computed<BTFType>({
      get() {
        return props.btfType as BTFType;
      },
      set(value) {
        emit('update:btfType', value);
      },
    });
    return { btfTypeVal, btfTypeLabel, emit };
  },
});
</script>
