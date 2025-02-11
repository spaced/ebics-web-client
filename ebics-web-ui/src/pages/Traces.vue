<template>
  <q-page class="justify-evenly">
    <div class="q-pa-md">
      <q-table
        title="Traces"
        :filter="customFilterInput"
        :rows="traces"
        :columns="columns"
        row-key="id"
        selection="single"
        v-model:selected="selectedTraceList"
        :filter-method="customFilterFunction"
        class="q-ma-sm"
      >
        <template v-slot:top-right>
          <div class="bg-grey-2 q-pa-sm q-mr-sm rounded-borders">
            <q-option-group
              v-model="transferTypeValue"
              :options="transferTypeOptions"
              color="green"
              type="checkbox"
              inline
              dense
            />
          </div>
          <div class="bg-grey-2 q-pa-sm rounded-borders">
            <q-option-group
              v-model="traceTypeValue"
              :options="traceTypeOptions"
              color="green"
              type="checkbox"
              inline
              dense
            />
          </div>
        </template>
        <template v-slot:header="props">
          <q-tr :props="props">
            <q-th
              v-for="col in props.cols.filter((c) => c.name != 'messageBody')"
              :key="col.name"
              :props="props"
            >
              {{ col.label }}
            </q-th>
          </q-tr>
        </template>
        <template v-slot:body="props">
          <q-tr :props="props" @click="props.selected = !props.selected">
            <!-- props.selected = !props.selected -->
            <q-td
              v-for="col in props.cols.filter((c) => c.name != 'messageBody')"
              :key="col.name"
              :props="props"
            >
              {{ col.value }}
            </q-td>
          </q-tr>
        </template>
        <template v-slot:top-left>
          <q-input
            borderless
            dense
            debounce="300"
            v-model="customFilterInput.freeTextSearch"
            placeholder="Search"
          >
            <template v-slot:append>
              <q-icon name="search" />
            </template>
          </q-input>
        </template>
      </q-table>
      <v-ace-editor
        v-bind:value="selectedTrace?.textMessageBody ?? ''"
        lang="xml"
        theme="clouds"
        style="height: 300px"
        :printMargin="false"
        class="q-ma-sm"
      />
    </div>
  </q-page>
</template>

<script lang="ts">
import { defineComponent, ref, Ref, computed } from 'vue';
import useTracesAPI from 'components/traces';
import { OrderTypeDefinition, TraceType, TraceEntry, TraceCategory } from 'components/models/trace'
import { TransferType } from 'components/models/ebics-order-type'
import useOrderTypeLabelAPI from 'components/order-type-label';
import { VAceEditor } from 'vue3-ace-editor';
import 'ace-builds/src-noconflict/mode-xml';
import 'ace-builds/src-noconflict/theme-clouds';
import { EbicsVersion } from 'src/components/models/ebics-version';
import { QTableColumn } from 'quasar';

interface CustomFilterInput {
  traceTypeValue: Ref<TraceType[]>,
  transferTypeValue: Ref<TransferType[]>,
  freeTextSearch: Ref<string>,
}

interface CustomFilterInputNR {
  traceTypeValue: TraceType[],
  transferTypeValue: TransferType[],
  freeTextSearch: string,
}

export default defineComponent({
  components: { VAceEditor },
  // eslint-disable-next-line vue/multi-word-component-names
  name: 'Traces',
  setup() {
    const { btfServiceLabel } = useOrderTypeLabelAPI();

    const orderTypeLabel = (orderType: OrderTypeDefinition): string => {
      if (orderType.businessOrderType) return orderType.businessOrderType;
      else if (orderType.ebicsServiceType)
        return btfServiceLabel(orderType.ebicsServiceType);
      else return orderType.adminOrderType;
    };

    const columns: QTableColumn<TraceEntry>[] = [
      {
        name: 'traceCategory',
        label: 'Category',
        align: 'left',
        field: (row: TraceEntry) => row.traceCategory,
        sortable: true,
      },
      {
        name: 'dateTime',
        required: true,
        label: 'Date/Time',
        align: 'left',
        field: (row: TraceEntry) => row.dateTime,
        sortable: true,
      },
      {
        name: 'ebicsUser',
        required: true,
        label: 'EBICS User',
        align: 'left',
        field: (row: TraceEntry) => row.bankConnection?.userId,
        sortable: true,
      },
      {
        name: 'ebicsCustomer',
        required: true,
        label: 'EBICS Customer',
        align: 'left',
        field: (row: TraceEntry) => row.bankConnection?.partner?.partnerId,
        sortable: true,
      },
      {
        name: 'creator',
        required: true,
        label: 'User',
        align: 'left',
        field: (row: TraceEntry) => row.creator,
        sortable: true,
      },
      {
        name: 'messageBody',
        label: 'Message Body',
        align: 'left',
        field: (row: TraceEntry) => row.textMessageBody,
        sortable: true,
      },
      {
        name: 'ebicsVersion',
        label: 'Version',
        align: 'left',
        field: (row: TraceEntry) => row.ebicsVersion,
        sortable: true,
      },
      {
        name: 'ot',
        label: 'Order Type',
        align: 'left',
        field: (row: TraceEntry) => orderTypeLabel(row.orderType),
        sortable: true,
      },
      {
        name: 'on',
        label: 'Order Number',
        align: 'left',
        field: (row: TraceEntry) => row.orderNumber,
        sortable: true,
      },
      {
        name: 'sessionId',
        label: 'Session',
        align: 'left',
        field: (row: TraceEntry) => row.sessionId,
        sortable: true,
      },
    ];
    const { traces } = useTracesAPI();
    const selectedTraceList = ref<TraceEntry[]>([]);
    const selectedTrace = computed(() => selectedTraceList.value.at(0))
    const traceTypeValue = ref([TraceType.Content]);
    const traceTypeOptions = [
      {
        label: 'EBICS Envelope',
        value: TraceType.EbicsEnvelope,
      },
      {
        label: 'Content',
        value: TraceType.Content,
      },
    ];

    const transferTypeValue = ref([TransferType.Upload]);
    const transferTypeOptions = [
      {
        label: 'Upload',
        value: TransferType.Upload,
      },
      {
        label: 'Download',
        value: TransferType.Download,
      },
    ];

    const freeTextSearch = ref('');
    const customFilterInput = {
      traceTypeValue,
      transferTypeValue,
      freeTextSearch,
    } as CustomFilterInput

    const transferTypeFilterPredicate = (traceEntry: TraceEntry): boolean => {
      if (customFilterInput.transferTypeValue.value) {
        if (customFilterInput.transferTypeValue.value.includes(TransferType.Upload) &&
          customFilterInput.transferTypeValue.value.includes(TransferType.Download))
          return true;
        else if (customFilterInput.transferTypeValue.value.includes(TransferType.Upload) && traceEntry.upload)
          return true;
        else if (customFilterInput.transferTypeValue.value.includes(TransferType.Download) && !traceEntry.upload)
          return true;
        else
          return false;
      } else {
        return true;
      }
    };

    const traceTypeFilterPredicate = (traceEntry: TraceEntry): boolean => {
      if (customFilterInput.traceTypeValue.value) {
        return customFilterInput.traceTypeValue.value.includes(traceEntry.traceType);
      } else {
        return true;
      }
    };

    const fullTextFilterPredicate = (traceEntry: TraceEntry, lowerCaseSearchCriteriaInput: string): boolean => {
      if (lowerCaseSearchCriteriaInput) {

      const lowCaseFieldValues:string[] = [
        traceEntry.textMessageBody,
        traceEntry.creator,
        traceEntry.sessionId,
        traceEntry.orderNumber,
        traceEntry.bankConnection?.userId,
        traceEntry.bankConnection?.name,
        traceEntry.bankConnection?.partner.partnerId,
        traceEntry.bankConnection?.partner.bank.hostId,
        traceEntry.bank?.hostId,
        traceEntry.bank?.bankURL,
        traceEntry.bank?.name,
        traceEntry.orderType?.adminOrderType,
        traceEntry.orderType?.businessOrderType,
        traceEntry.orderType?.ebicsServiceType?.serviceName,
        traceEntry.orderType?.ebicsServiceType?.serviceOption,
        traceEntry.orderType?.ebicsServiceType?.scope,
        traceEntry.orderType?.ebicsServiceType?.containerType,
        traceEntry.orderType?.ebicsServiceType?.message?.messageName,
        traceEntry.orderType?.ebicsServiceType?.message?.messageNameVariant,
        traceEntry.orderType?.ebicsServiceType?.message?.messageNameVersion,
        traceEntry.orderType?.ebicsServiceType?.message?.messageNameFormat]
              .filter(field => field) //Filter out null values
              .map((field) => String(field).toLowerCase());

        return lowCaseFieldValues.find(fieldText => fieldText.includes(lowerCaseSearchCriteriaInput)) != undefined
      } else {
        return true;
      }
    };

    const customFilterFunction = (rows:TraceEntry[], filterCriteria:CustomFilterInputNR):TraceEntry[] => {
      // rows contain the entire data
      // terms contains whatever is actual filter criteria
      console.log(filterCriteria)

      const filteredRowsByOptions = rows.filter((row) => transferTypeFilterPredicate(row) && traceTypeFilterPredicate(row))

      if (filterCriteria.freeTextSearch && filterCriteria.freeTextSearch.length > 0) {
        const lowerSearch = filterCriteria.freeTextSearch ? filterCriteria.freeTextSearch.toLowerCase() : ''
        return filteredRowsByOptions.filter((row) => fullTextFilterPredicate(row, lowerSearch));
      } else {
        return filteredRowsByOptions;
      }
    }

    return {
      columns,
      traces,
      selectedTrace,
      selectedTraceList,

      transferTypeValue,
      transferTypeOptions,
      traceTypeValue,
      traceTypeOptions,
      customFilterInput,
      customFilterFunction,

      EbicsVersion,
      TraceCategory,
    };
  },
});
</script>
