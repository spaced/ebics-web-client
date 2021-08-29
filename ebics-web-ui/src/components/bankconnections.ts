import { ref, onMounted, computed } from 'vue';
import { User } from 'components/models';
import { api } from 'boot/axios';
import useBaseAPI from './base-api';
import { useQuasar } from 'quasar';

/**
 * Bank Connections composition API for bank connection list operations with backend REST API
 * @returns
 *  bankConnections synchronized list of bank connections
 *  loadBankConnections function to trigger refreshing of bank connections
 *  deleteBankConnection function to delete bank connection
 */
export default function useBankConnectionsAPI() {
  const { apiErrorHandler } = useBaseAPI();
  const q = useQuasar();

  const bankConnections = ref<User[]>();

  const loadBankConnections = async (): Promise<void> => {
    try {
      const response = await api.get<User[]>('bankconnections');
      bankConnections.value = response.data;
    } catch (error) {
      apiErrorHandler('Loading of bank data failed', error);
    }
  };

  const activeBankConnections = computed<User[] | undefined>(() => {
    return bankConnections.value?.filter(bc => bc.userStatus == 'READY')
  });

  const hasActiveConnections = computed<boolean>(() => {
    return activeBankConnections.value != null && activeBankConnections.value?.length > 0;
  });

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
          reject('Password entry canceled');
        })
        .onDismiss(() => {
          reject('Password entry dismissed');
        });
    });
  };

  const deleteBankConnection = async (bcId: number, bcName: string, askForConfimation = true): Promise<void> => {
    try {
      const canDelete = askForConfimation ? await confirmDialog('Confirm deletion', `Do you want to realy delete bank connection: '${bcName}'`) : true
      if (canDelete) {
        await api.delete<User>(`bankconnections/${bcId}`);
        await loadBankConnections();
      }
    } catch (error) {
      apiErrorHandler('Deleting of user data failed', error);
    }
  };

  onMounted(loadBankConnections);

  return { bankConnections, activeBankConnections, hasActiveConnections, loadBankConnections, deleteBankConnection };
}
