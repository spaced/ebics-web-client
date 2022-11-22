<template>
  <div>
    <q-card flat bordered class="my-card">
      <q-card-section>
        <div class="text-caption text-weight-light">
          Bank Connection properties
        </div>
      </q-card-section>

      <q-card-section class="q-pt-none">
        <q-list v-if="propertiesArray" dense>
          <q-item v-for="(property, index) in propertiesArray" :key="property">
            <q-input dense label="name" v-model="property.key" />
            <q-input dense label="value" v-model="property.value" />
            <q-btn
              flat
              dense
              color="primary"
              icon="clear"
              @click="deleteExistingProperty(index)"
            />
          </q-item>
          <q-item>
            <q-btn
              flat
              dense
              color="primary"
              label="add"
              icon="add"
              @click="addNewProperty"
            />
          </q-item>
        </q-list>
      </q-card-section>

      <q-card-section>
        <div class="q-field__bottom">
          The property names &amp; values are used by uploading of templates
        </div>
      </q-card-section>
    </q-card>
  </div>
</template>

<script lang="ts">
import {
  defineComponent,
  computed,
  PropType,
  ref,
  watch,
} from 'vue';
import useUserSettingsAPI from 'components/user-settings';
import { BankConnectionProperty } from 'components/models/ebics-bank-connection';

export default defineComponent({
  name: 'BankConnectionProperties',
  props: {
    properties: {
      type: Object as PropType<BankConnectionProperty[]>,
      required: false,
    },
  },
  emits: ['update:properties'],
  setup(props, { emit }) {
    const columns = [
      {
        name: 'name',
        required: true,
        label: 'Property name',
        align: 'left',
        field: (row: BankConnectionProperty) => row.key,
        sortable: true,
      },
      {
        name: 'Value',
        align: 'right',
        label: 'Value',
        field: (row: BankConnectionProperty) => row.value,
        sortable: true,
      },
    ];

    const configureOrdertypesDropdown = ref(false);
    const { saveUserSettings } = useUserSettingsAPI();

    const propertiesArray = computed(() => props.properties as BankConnectionProperty[])

    /*const propertiesArray: Ref<BankConnectionProperty[]> = ref([
      { key: 'test1', value: 'tv1' } as BankConnectionProperty,
      { key: 'test2', value: 'tv2' } as BankConnectionProperty,
    ]);*/

    const deleteExistingProperty = (propertyIndex: number): void => {
      propertiesArray.value?.splice(propertyIndex, 1);
    };
    const addNewProperty = (): void => {
      propertiesArray.value?.push({ key: '', value: '' } as BankConnectionProperty);
    };
    const propertyChanged = (): void => {
      console.log('changed');
    };

    watch(propertiesArray, propertyChanged, { deep: true });

    return {
      configureOrdertypesDropdown,
      saveUserSettings,
      columns,
      propertiesArray,
      deleteExistingProperty,
      addNewProperty,
      propertyChanged,
    };
  },
});
</script>
