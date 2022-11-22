import { ref, Ref } from 'vue';
import { api } from 'boot/axios';
import useBaseAPI from './base-api';
import { BankConnectionProperty } from 'components/models/ebics-bank-connection';

/**
 * 
 * @returns bank connection properties related API
 */
export default function useBankConnectionPropertiesAPI(bankConnectionId: Ref<number | undefined>) {
  const { apiErrorHandler, apiOkHandler } = useBaseAPI();

  const bankConnectionProperties = ref<BankConnectionProperty[] | undefined>()

  const loadBankConnectionProperties = async (): Promise<void> => {
    try {
      if (bankConnectionId.value) {
        const response = await api.get<BankConnectionProperty[]>(`bankconnections/${bankConnectionId.value}/properties`);
        bankConnectionProperties.value = response.data;
        console.log('Bank connection properties loaded');
      } else {
        console.warn('No connection id provided for loading of bank connection properties');
      }
    } catch (error) {
      apiErrorHandler('Loading of bank connection properties failed', error);
    }
  };

  const saveBankConnectionProperties = async (): Promise<void> => {
    try {
      if (bankConnectionId.value) {
        console.log('Saving the bank connection properties: ' + JSON.stringify(bankConnectionProperties.value));
        const response = await api.post<BankConnectionProperty[]>(`bankconnections/${bankConnectionId.value}/properties`, bankConnectionProperties.value);
        bankConnectionProperties.value = response.data
        console.log('The bank connection properties saved: ' + JSON.stringify(bankConnectionProperties.value));
        apiOkHandler('Bank connection properties successfully saved')
      } else {
        console.error('Existing bankConnectionId must be provided in order to save the bank connection properties');
      }
    } catch (error) {
      apiErrorHandler('Saving of bank connection properties failed', error);
    }
  };

  return { bankConnectionProperties, loadBankConnectionProperties, saveBankConnectionProperties };
}
