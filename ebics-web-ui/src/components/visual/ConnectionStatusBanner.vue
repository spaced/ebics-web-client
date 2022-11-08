<template>
  <q-banner
    v-if="connectionStatusObject &&
      (connectionStatusObject.healthStatus == HealthStatusType.Error ||
        connectionStatusObject.healthStatus == HealthStatusType.Warning)
    "
    inline-actions
    class="bg-grey-3"
  >
    <template v-slot:avatar>
      <q-icon
        v-if="connectionStatusObject.healthStatus == HealthStatusType.Error"
        name="error"
        color="red"
      />
      <q-icon
        v-if="connectionStatusObject.healthStatus == HealthStatusType.Warning"
        name="warning"
        color="warning"
      />
    </template>
    <q-item-section>
      <q-item-label v-if="connectionStatusObject.healthStatus == HealthStatusType.Warning"
        >The selected bank connection has some issues</q-item-label
      >
      <q-item-label v-if="connectionStatusObject.healthStatus == HealthStatusType.Error"
        >The selected bank connection is erroneus</q-item-label
      >
      <q-item-label caption v-if="connectionStatusObject.totalCount < 10">
        Out of {{ connectionStatusObject.totalCount }} request(s) have
        {{ connectionStatusObject.errorCount }} failed, and {{ connectionStatusObject.okCount }} was OK.
      </q-item-label>
      <q-item-label caption v-else>
        Out of {{ connectionStatusObject.totalCount }} request(s) have {{ connectionStatusObject.errorRate }}%
        failed, and {{ connectionStatusObject.okRate }}% was OK.
      </q-item-label>
      <error-message-label v-if="connectionStatusObject.lastError" v-model="connectionStatusObject.lastError" />
    </q-item-section>
    <!-- template v-slot:action>
      <q-btn flat color="primary" label="Latest errors" @click="showLatestErrors = true"/>
      <q-dialog v-model="showLatestErrors" class="absolute-top" >
        <response-statistics-alert-dialog :bankConnectionId="bankConnectionId"/>
      </q-dialog>
    </template-->
  </q-banner>
</template>

<script lang="ts">
import { defineComponent, computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ConnectionStatus } from 'components/models/connection-status';
import { HealthStatusType, ConnectionStatusObject } from 'components/models/allivenes-health-status';
import ErrorMessageLabel from 'components/visual/ErrorMessageLabel.vue';

export default defineComponent({
  name: 'ConnectionStatus',
  components: { ErrorMessageLabel },
  props: {
    connectionStatus: {
      type: Object,
      required: true,
    },
    bankConnectionId: {
      type: Number,
      required: true,
    },
  },
  emits: ['update:modelValue', 'update:connectionStatus'],
  setup(props, { emit }) {
    const router = useRouter();

    const bankConnectionStatusVal = computed<ConnectionStatus>({
      get() {
        return props.connectionStatus as ConnectionStatus;
      },
      set(value) {
        emit('update:connectionStatus', value);
      },
    });

    const bankConnectionId = computed(() => { 
      return props.bankConnectionId; 
    });

    /**
     * Route to statistics page
     * bankConnectionId
     *  - if given then will be routed with 'bankConnectionId' parameter 
     *  - if undefined will be routed without 'bankConnectionId' parameter
     */
    const routeToResponseStatisticsPage = async():Promise<void> => {
      const routeParams = bankConnectionId.value === undefined ? undefined : { bankConnectionId: bankConnectionId.value };
      const routeName = 'response-statistics';
      await router.push({
        name: routeName,
        params: routeParams,
      });
    }

    const showLatestErrors = ref(false);
    const togleShowLatestErrors = ():void => { 
      showLatestErrors.value = true; 
    }

    const connectionStatusObject = computed((): ConnectionStatusObject => {
      return bankConnectionStatusVal.value?.frontendStatus ? bankConnectionStatusVal.value?.frontendStatus : bankConnectionStatusVal.value?.backendStatus;
    }); 

    return { HealthStatusType, routeToResponseStatisticsPage, showLatestErrors, togleShowLatestErrors, bankConnectionStatusVal, connectionStatusObject };
  },
});
</script>
