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
      <q-btn v-if="showSettingsButtonVal"
        round
        dense
        flat
        icon="settings"
        @click.stop="configureOrdertypesDropdown = true;"
      > 
      <q-tooltip>
          Click to adjust filter to display/hide erroneous or shared connections..
        </q-tooltip>
      </q-btn>
    </template>
    <q-dialog v-model="configureOrdertypesDropdown">
      <q-card>
        <q-card-section>
          <div class="text-h6">Adjust order types filter settings</div>
        </q-card-section>

        <user-preferences sectionFilter="OrderTypesSettings"/>

        <q-card-actions align="right">
          <q-btn flat label="OK" color="primary" v-close-popup @click="saveUserSettings()"/>
        </q-card-actions>
      </q-card>
    </q-dialog>
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
import { defineComponent, computed, PropType, ref } from 'vue';
import { BankConnection } from 'components/models/ebics-bank-connection';
import { OrderType } from 'components/models/ebics-order-type';
import useOrderTypeLabelAPI from 'components/order-type-label';
import useUserSettingsAPI from 'components/user-settings';
import UserPreferences from 'components/visual/UserPreferences.vue';

export default defineComponent({
  name: 'OrderTypeSelect',
  components: { UserPreferences },
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
    showSettingsButton: {
      type: Boolean,
      required: false,
    }
  },
  emits: ['update:orderType', 'click:refreshOrderTypes'],
  setup(props, { emit }) {
    const { orderTypeLabel } = useOrderTypeLabelAPI();
    const configureOrdertypesDropdown = ref(false);
    const { saveUserSettings } = useUserSettingsAPI();
    const orderTypeVal = computed<OrderType>({
      get() {
        return props.orderType as OrderType;
      },
      set(value) {
        emit('update:orderType', value);
      },
    });
    const showSettingsButtonVal = computed<boolean>(() => props.showSettingsButton)
    return { orderTypeVal, orderTypeLabel, emit, configureOrdertypesDropdown, saveUserSettings, showSettingsButtonVal};
  },
});
</script>
