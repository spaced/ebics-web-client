import { ref, onMounted, computed } from 'vue';
import {
  BankConnection,
  BankConnectionAccess,
} from 'src/components/models/ebics-bank-connection';
import { api } from 'boot/axios';
import useBaseAPI from 'components/base-api';
import { useQuasar } from 'quasar';
import { HealthStatusType } from 'components/models/allivenes-health-status';
import useUserSettingsAPI from 'components/user-settings';

/**
 * Display label of the bankConnection
 */
export function bankConnectionLabel(
  bankConnection: BankConnection | undefined
): string {
  if (
    bankConnection &&
    bankConnection.userId.trim().length > 0 &&
    bankConnection.name.trim().length > 0
  ) {
    return `${bankConnection.userId} | ${bankConnection.name}`;
  } else {
    return '';
  }
}

/**
 * Bank Connections composition API for bank connection list operations with backend REST API
 * @returns
 *  bankConnections synchronized list of bank connections
 *  loadBankConnections function to trigger refreshing of bank connections
 *  deleteBankConnection function to delete bank connection
 */
export default function useBankConnectionsAPI(
  accessRight: BankConnectionAccess = BankConnectionAccess.READ
) {
  const { apiErrorHandler } = useBaseAPI();
  const q = useQuasar();
  const { userSettings } = useUserSettingsAPI();

  const bankConnections = ref<readonly BankConnection[]>([]);
  const loading = ref<boolean>(false);

  const loadBankConnections = async (): Promise<void> => {
    try {
      loading.value = true;
      const response = await api.get<BankConnection[]>(
        `bankconnections?permission=${accessRight}`
      );
      bankConnections.value = response.data
    } catch (error) {
      apiErrorHandler('Loading of bank data failed', error);
    } finally {
      loading.value = false;
    }
  };

  const activeBankConnections = computed(
    () => bankConnections.value.filter((bc) => bc.userStatus == 'READY')
  );

  const hasActiveConnections = computed(() => activeBankConnections.value.length > 0);

  const confirmDialog = (title: string, message: string): Promise<boolean> => {
    return new Promise<boolean>((resolve, reject) => {
      q.dialog({
        title: title,
        message: message,
        cancel: true,
        persistent: true,
      })
        .onOk(() => {
          resolve(true);
        })
        .onCancel(() => {
          reject('Deletion canceled');
        })
        .onDismiss(() => {
          reject('Deletion dismissed');
        });
    });
  };

  const deleteBankConnection = async (
    bcId: number,
    bcName: string,
    askForConfimation = true
  ): Promise<void> => {
    try {
      const canDelete = askForConfimation
        ? await confirmDialog(
            'Confirm deletion',
            `Do you want to realy delete bank connection: '${bcName}'`
          )
        : true;
      if (canDelete) {
        await api.delete<BankConnection>(`bankconnections/${bcId}`);
        await loadBankConnections();
      }
    } catch (error) {
      apiErrorHandler('Deleting of user data failed', error);
    }
  };

  const hasActivePrivateConnections = computed( () => activeBankConnections.value.some((bc) => !bc.guestAccess));

  const hasActiveSharedConnections = computed( () => activeBankConnections.value.some((bc) => bc.guestAccess));

  const isConnectionErrorneous = (bankConnection: BankConnection) =>
      bankConnection.backendStatus?.healthStatus == HealthStatusType.Error ||
      bankConnection.frontendStatus?.healthStatus == HealthStatusType.Error;

  const hasErrorneousConnections = computed( () => activeBankConnections.value.some(isConnectionErrorneous));

  /**
   * This method returns only the connections which should be displayed
   * The shared & errorneous connection are filtered out if relevant
   */
  const activeDisplayedBankConnections = computed(
    () => activeBankConnections.value.filter(
        (bc) =>
          (!bc.guestAccess || userSettings.value.displaySharedBankConnections) &&
          (!isConnectionErrorneous(bc) || userSettings.value.displayErroneousConnections)
      ));

  onMounted(loadBankConnections);

  return {
    bankConnections,
    activeBankConnections,
    activeDisplayedBankConnections,
    hasActiveConnections,
    hasActivePrivateConnections,
    hasActiveSharedConnections,
    hasErrorneousConnections,
    loadBankConnections,
    deleteBankConnection,
    bankConnectionLabel,
    loading,
  };
}
