<template>
  <q-page class="justify-evenly">
    <div v-if="loadingOrderTypes || loadingBankConnections" class="q-pa-md">
      <q-banner class="bg-grey-3">
        <template v-slot:avatar>
          <q-spinner-dots color="primary" size="2em" />
        </template>
        The bank connection are being loaded..
      </q-banner>
    </div>
    <div v-else>
      <div v-if="hasActiveConnections" class="q-pa-md">
        <!-- style="max-width: 400px" -->
        <div class="q-pa-md">
          <q-form
            ref="uploadForm"
            @submit="processDownload"
            class="q-gutter-md"
          >
            <bank-connection-select
              v-model:bankConnection="bankConnection"
              :bankConnections="activeDisplayedBankConnections"
            />

            <connection-status-banner
              v-if="bankConnection"
              :connectionStatus="bankConnection"
              :bankConnectionId="bankConnection.id"
            />

            <ebics-version-radios v-model:bankConnection="bankConnection" />

            <order-type-select
              v-model:orderType="orderType"
              :orderTypes="orderTypes"
              :bankConnection="bankConnection"
              @click:refreshOrderTypes="refreshOrderTypes(bankConnection)"
            />
            <btf-select
              v-model:btfType="orderType"
              :btfTypes="orderTypes"
              :bankConnection="bankConnection"
              @click:refreshOrderTypes="refreshBtfTypes(bankConnection)"
            />

            <!--div v-if="bankConnection" class="q-gutter-sm">
              <q-checkbox
                v-model="historicDownload"
                label="Historical download"
              />
              <q-input filled v-if="historicDownload" v-model="historicDateRange">
                <template v-slot:append>
                  <q-icon name="event" class="cursor-pointer">
                    <q-popup-proxy ref="qDateProxy" transition-show="scale" transition-hide="scale">
                      <q-date v-model="historicDateRange" range >
                        <div class="row items-center justify-end">
                          <q-btn v-close-popup label="Close" color="primary" flat />
                        </div>
                      </q-date>
                    </q-popup-proxy>
                  </q-icon>
                </template>
              </q-input>
            </div-->

            <div class="q-pa-md q-gutter-sm">
              <q-btn label="Download" type="submit" color="primary" />
            </div>
          </q-form>
        </div>
      </div>
      <div v-else class="q-pa-md">
        <q-banner class="bg-grey-3">
          <template v-slot:avatar>
            <q-icon name="signal_wifi_off" color="primary" />
          </template>
          You have no initialized bank connection. Create and initialize one
          bank connection in order to download files.
          <template v-slot:action>
            <q-btn
              flat
              color="primary"
              label="Manage bank connections"
              to="/bankconnections"
            />
          </template>
        </q-banner>
      </div>
    </div>
  </q-page>
</template>

<script lang="ts">
import {
  DownloadRequest,
  DownloadRequestH004,
  DownloadRequestH005,
} from 'components/models/ebics-request-response';
import {
  BankConnection,
  BankConnectionAccess,
} from 'components/models/ebics-bank-connection';
import { EbicsVersion } from 'components/models/ebics-version';
import { FileFormat } from 'components/models/file-format';
import {
  BTFType,
  OrderTypeFilter,
  OrderType,
} from 'components/models/ebics-order-type';
import { defineComponent } from 'vue';
import { ref, toRef } from 'vue';
import { exportFile } from 'quasar';

//Composition APIs
import useBankConnectionsAPI from 'components/bankconnections';
import useFileTransferAPI from 'components/filetransfer';
import useTextUtils from 'components/text-utils';
import useUserSettings from 'components/user-settings';
import useOrderTypesAPI from 'components/order-types';
import useOrderTypeLabelAPI from 'components/order-type-label';
import useBanksAPI from 'components/banks';

//Components
import ConnectionStatusBanner from 'components/visual/ConnectionStatusBanner.vue';
import BankConnectionSelect from 'components/visual/BankConnectionSelect.vue';
import OrderTypeSelect from 'components/visual/OrderTypeSelect.vue';
import BtfSelect from 'components/visual/BtfSelect.vue';
import EbicsVersionRadios from 'components/visual/EbicsVersionRadios.vue';

import { HealthStatusType } from 'components/models/allivenes-health-status';

export default defineComponent({
  name: 'FileDownload',
  components: {
    ConnectionStatusBanner,
    BankConnectionSelect,
    OrderTypeSelect,
    EbicsVersionRadios,
    BtfSelect,
  },
  setup() {
    //Selected bank connection
    const bankConnection = ref<BankConnection>();
    const replaceMsgId = ref(true);
    const {
      activeBankConnections,
      activeDisplayedBankConnections,
      hasActiveConnections,
      hasActivePrivateConnections,
      hasActiveSharedConnections,
      hasErrorneousConnections,
      bankConnectionLabel,
      loading: loadingBankConnections,
    } = useBankConnectionsAPI(BankConnectionAccess.USE);
    const { ebicsDownloadRequest } = useFileTransferAPI();
    const {
      applySmartAdjustments,
      detectFileFormat,
      getFileExtension,
      currentDate,
    } = useTextUtils();
    const { isEbicsVersionAllowedForUse } = useBanksAPI(true);
    const { userSettings } = useUserSettings();
    const { orderTypeLabel, btfTypeLabel } = useOrderTypeLabelAPI();
    const {
      btfTypes,
      orderTypes,
      refreshOrderTypes,
      refreshBtfTypes,
      loading: loadingOrderTypes,
    } = useOrderTypesAPI(
      bankConnection,
      activeBankConnections,
      ref(OrderTypeFilter.DownloadOnly),
      toRef(userSettings.value, 'displayAdminTypes')
    );

    const historicDownload = ref(false);
    const historicDateRange = ref({ from: currentDate(), to: currentDate() });

    const orderType = ref<OrderType>();
    const btfType = ref<BTFType>();

    const getDownloadRequest = (): DownloadRequest => {
      if (bankConnection.value?.ebicsVersion == 'H005') {
        return {
          adminOrderType: btfType.value?.adminOrderType,
          orderService: btfType.value?.service,
        } as DownloadRequestH005;
      } else {
        //H004, H003
        return {
          adminOrderType: orderType.value?.adminOrderType,
          orderType: orderType.value?.orderType,
          params: new Map(),
        } as DownloadRequestH004;
      }
    };

    const getDownloadOrderTypeFileName = (): string => {
      if (bankConnection.value?.ebicsVersion == 'H005') {
        if (btfType.value?.service) return btfType.value?.service.serviceName;
        else return btfType.value?.adminOrderType ?? 'XXX';
      } else {
        //H004, H003
        return orderType.value?.orderType ?? 'XXX';
      }
    };

    const processDownload = async (): Promise<void> => {
      if (bankConnection.value) {
        const fileData = await ebicsDownloadRequest(
          bankConnection.value,
          getDownloadRequest()
        );

        if (fileData) {
          const fileText = await fileData.text();
          const fileFormat = detectFileFormat(fileText);
          const fileExtension = getFileExtension(fileFormat);
          const fileName = getDownloadOrderTypeFileName();
          //, {mimeType: 'application/xml'}
          const status = exportFile(
            `download.${fileName}.${fileExtension}`,
            fileData,
            { mimeType: 'application/octet-stream' }
          );
          if (status !== true) {
            console.error(
              `Browser error by downloading of the file: ${JSON.stringify(
                status
              )}`
            );
          }
        }
      }
    };

    return {
      bankConnection,
      activeBankConnections,
      activeDisplayedBankConnections,
      hasActiveConnections,
      hasActivePrivateConnections,
      hasActiveSharedConnections,
      hasErrorneousConnections,
      bankConnectionLabel,

      isEbicsVersionAllowedForUse,
      EbicsVersion,

      historicDownload,
      historicDateRange,

      userSettings,
      replaceMsgId,
      applySmartAdjustments,
      detectFileFormat,

      //Commons order types
      orderType,
      orderTypes,
      orderTypeLabel,
      refreshOrderTypes,

      //Commons BTF types
      btfType,
      btfTypes,
      btfTypeLabel,
      refreshBtfTypes,

      //Loading
      loadingBankConnections,
      loadingOrderTypes,

      //Commons request flags
      HealthStatusType,
      FileFormat,
      processDownload,
    };
  },
});
</script>
