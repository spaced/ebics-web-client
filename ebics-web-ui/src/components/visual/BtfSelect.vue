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
    <template v-slot:option="btfTypeOpt">
      <q-item v-bind="btfTypeOpt.itemProps">
        <q-item-section>
          <q-item-label>{{btfTypeLabel(btfTypeOpt.opt)}}</q-item-label>
          <q-item-label caption>{{
            btfTypeOpt.opt.description
          }}</q-item-label>
        </q-item-section>
      </q-item>
    </template>
  </q-select>
</template>

<script lang="ts">
import { defineComponent, computed, PropType, ref } from 'vue';
import { BankConnection } from 'components/models/ebics-bank-connection';
import { BTFType } from 'components/models/ebics-order-type';
import useOrderTypeLabelAPI from 'components/order-type-label';
import useUserSettingsAPI from 'components/user-settings';
import UserPreferences from 'components/visual/UserPreferences.vue'

export default defineComponent({
  name: 'OrderTypeSelect',
  components: { UserPreferences, },
  props: {
    btfType: {
      type: Object as PropType<BTFType>,
      required: false,
    },
    btfTypes: {
      type: Object as PropType<BTFType[]>,
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
  emits: ['update:btfType', 'click:refreshBtfTypes'],
  setup(props, { emit }) {
    const { btfTypeLabel } = useOrderTypeLabelAPI();
    const configureOrdertypesDropdown = ref(false);
    const { saveUserSettings } = useUserSettingsAPI();
    const btfTypeVal = computed<BTFType>({
      get() {
        return props.btfType as BTFType;
      },
      set(value) {
        emit('update:btfType', value);
      },
    });
    const showSettingsButtonVal = computed<boolean>(() => props.showSettingsButton)
    return { btfTypeVal, btfTypeLabel, emit, configureOrdertypesDropdown, saveUserSettings, showSettingsButtonVal };
  },
});
</script>
