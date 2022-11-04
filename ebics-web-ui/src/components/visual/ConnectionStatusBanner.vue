<template>
  <q-banner
    v-if="value.frontendStatus &&
      (value.frontendStatus.healthStatus == HealthStatusType.Error ||
        value.frontendStatus.healthStatus == HealthStatusType.Warning)
    "
    inline-actions
    class="bg-grey-3"
  >
    <template v-slot:avatar>
      <q-icon
        v-if="value.frontendStatus.healthStatus == HealthStatusType.Error"
        name="error"
        color="red"
      />
      <q-icon
        v-if="value.frontendStatus.healthStatus == HealthStatusType.Warning"
        name="warning"
        color="warning"
      />
    </template>
    <q-item-section>
      <q-item-label v-if="value.frontendStatus.healthStatus == HealthStatusType.Warning"
        >The selected bank connection has some issues</q-item-label
      >
      <q-item-label v-if="value.frontendStatus.healthStatus == HealthStatusType.Error"
        >The selected bank connection is erroneus</q-item-label
      >
      <q-item-label caption v-if="value.frontendStatus.totalCount < 10">
        Out of {{ value.frontendStatus.totalCount }} request(s) have
        {{ value.frontendStatus.errorCount }} failed, and {{ value.frontendStatus.okCount }} was OK.
      </q-item-label>
      <q-item-label caption v-else>
        Out of {{ value.frontendStatus.totalCount }} request(s) have {{ value.frontendStatus.errorRate }}%
        failed, and {{ value.frontendStatus.okRate }}% was OK.
      </q-item-label>
      <error-message-label v-if="value.lastError" v-model="value.lastError" />
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
import { HealthStatusType } from 'components/models/allivenes-health-status';
import ErrorMessageLabel from 'components/visual/ErrorMessageLabel.vue';

export default defineComponent({
  name: 'ConnectionStatus',
  components: { ErrorMessageLabel },
  props: {
    modelValue: {
      type: Object,
      required: true,
    },
    bankConnectionId: {
      type: Number,
      required: true,
    },
  },
  emits: ['update:modelValue'],
  computed: {
    value: {
      get(): ConnectionStatus {
        return this.modelValue as ConnectionStatus;
      },
      set(value: ConnectionStatus) {
        this.$emit('update:modelValue', value);
      },
    },
  },
  setup(props) {
    const router = useRouter();

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
      console.log('0'); 
      }

    return { HealthStatusType, routeToResponseStatisticsPage, showLatestErrors, togleShowLatestErrors };
  },
});
</script>
