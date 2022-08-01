<template>
  <q-select
    v-if="
      bankConnection?.ebicsVersion == 'H003' ||
      bankConnection?.ebicsVersion == 'H004'
    "
    filled
    v-model="orderTypeVal"
    :options="orderTypes"
    :option-label="(t) => orderTypeLabel(t)"
    label="EBICS Order Type"
    hint="Select EBICS Order Type"
    lazy-rules
    :rules="[(val) => val || 'Please select valid EBICS Order Type']"
  >
    <template v-slot:append>
      <q-btn
        round
        dense
        flat
        icon="refresh"
        @click.stop="emit('click:refreshOrderTypes')"
      />
    </template>
    <template v-slot:option="scope">
      <q-item v-bind="scope.itemProps">
        <q-item-section>
          <q-item-label v-html="orderTypeLabel(scope.opt)" />
          <q-item-label caption>{{ scope.opt.description }}</q-item-label>
        </q-item-section>
      </q-item>
    </template>
  </q-select>
</template>

<script lang="ts">
import { defineComponent, computed, PropType } from 'vue';
import { BankConnection } from 'components/models/ebics-bank-connection';
import { OrderType } from 'components/models/ebics-order-type';
import useOrderTypeLabelAPI from 'components/order-type-label';

export default defineComponent({
  name: 'OrderTypeSelect',
  props: {
    orderType: {
      type: Object as PropType<OrderType>,
      required: false,
    },
    orderTypes: {
      type: Object,
      required: true,
    },
    bankConnection: {
      type: Object as PropType<BankConnection>,
      required: false,
    },
  },
  emits: ['update:orderType', 'click:refreshOrderTypes'],
  setup(props, { emit }) {
    const { orderTypeLabel } = useOrderTypeLabelAPI();
    const orderTypeVal = computed<OrderType>({
      get() {
        return props.orderType as OrderType;
      },
      set(value) {
        emit('update:orderType', value);
      },
    });
    return { orderTypeVal, orderTypeLabel, emit };
  },
});
</script>
