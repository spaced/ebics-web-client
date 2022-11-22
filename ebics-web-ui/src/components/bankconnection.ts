import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { Bank } from 'components/models/ebics-bank';
import { Partner } from 'components/models/ebics-partner';
import { BankConnection, BankConnectionProperty, UserPartnerBank } from 'components/models/ebics-bank-connection';
import { api } from 'boot/axios';
import useBaseAPI from './base-api';
import { AxiosResponse } from 'axios';

/**
 * User composition API in order to keep user object in sync with backend REST API
 * @param bankConnectionId id of the given user in case of editing, undefined in case of the new user
 * @returns 
 *  user - reactive user data
 *  userPartnerBank - computed userPartnerBank data used for creating/updating
 *  refreshUserData - function to be called in order to refresh user data from REST API
 *  createOrUpdateUserData - function used for storing user data using API
 */
export default function useBankConnectionAPI(bankConnectionId: number | undefined) {
  const router = useRouter()
  const { apiErrorHandler, apiOkHandler } = useBaseAPI();

  const bankConnection = ref<BankConnection>({
    name: '',
    userId: '',
    partner: {
      partnerId: '',
      bank: {
        id: 0,
        name: '',
      } as Bank,
    } as Partner,
    ebicsVersion: 'H005',
    userStatus: 'CREATED',
    guestAccess: false,
    usePassword: false,
    useCertificate: true,
  } as BankConnection);

  const bankConnectionProperties = ref<BankConnectionProperty[] | undefined>([])

  const loadBankConnection = async () => {
    if (bankConnectionId !== undefined) {
      try {
        const response = await api.get<BankConnection>(`bankconnections/${bankConnectionId}`)
        bankConnection.value = response.data;
      } catch(error) {
        apiErrorHandler('Loading of user failed', error)
      }
    }
  };

  const loadBankConnectionProperties = async () => {
    try {
      if (bankConnectionId !== undefined) {
        const response = await api.get<BankConnectionProperty[]>(`bankconnections/${bankConnectionId}/properties`);
        bankConnectionProperties.value = response.data;
        console.log('Bank connection properties loaded');
      } else {
        console.warn('No connection id provided for loading of bank connection properties');
      }
    } catch (error) {
      apiErrorHandler('Loading of bank connection properties failed', error);
    }
  };

  /**
   * Converting input User entity from GET request to UserPartnerBank in order for storing/updating of user data.
   */
  const userPartnerBank = computed<UserPartnerBank>(() => {
    return {
      ebicsVersion: bankConnection.value.ebicsVersion,
      userId: bankConnection.value.userId,
      name: bankConnection.value.name,
      dn: bankConnection.value.dn,
      partnerId: bankConnection.value.partner.partnerId,
      bankId: bankConnection.value.partner.bank.id,
      guestAccess: bankConnection.value.guestAccess,
      usePassword: bankConnection.value.usePassword,
      useCertificate: bankConnection.value.useCertificate,
    } as UserPartnerBank;
  });

  const createOrUpdateBankConnection = async() => {
    if (bankConnectionId === undefined) {
      try {
        const response = await api.post<UserPartnerBank, AxiosResponse<number>>('bankconnections', userPartnerBank.value)
        bankConnection.value.id = response.data;
        bankConnectionId = response.data;
        router.go(-1);
        console.log(`Bank connection created, with id = ${bankConnection.value.id}, id:${bankConnectionId}`);
        apiOkHandler('Bank connection created');
      } catch(error) {
        apiErrorHandler('Bank connection creation failed', error)
      }
    } else {
      try {
        await api.put<UserPartnerBank>(`bankconnections/${bankConnectionId}`, userPartnerBank.value)
        router.go(-1);
        apiOkHandler('Bank connection updated')
      } catch(error) {
        apiErrorHandler('Bank connection update failed', error)
      }
    }  
  };

  const saveBankConnectionProperties = async (): Promise<void> => {
    try {
      console.log(`Saving bank connection properties, with id = ${bankConnection.value.id}, id:${bankConnectionId ? bankConnectionId : 'undef'}`);
      if (bankConnectionId) {
        const response = await api.post<BankConnectionProperty[]>(`bankconnections/${bankConnectionId}/properties`, bankConnectionProperties.value);
        bankConnectionProperties.value = response.data
        apiOkHandler('Saved props sucessfully')
      } else {
        console.error('Existing bankConnectionId must be provided in order to save the bank connection properties');
      }
    } catch (error) {
      apiErrorHandler('Saving of bank connection properties failed', error);
    }
  };

  const loadBankConnectionWithProperties = async () => {
    await loadBankConnection();
    await loadBankConnectionProperties();
  }

  /**
   * Composite saving function, the order is here important, to have first bankConnectionId
   * And then the properties can be saved
   */
  const saveBankConnectionWithProperties = async () => {
    await createOrUpdateBankConnection();
    await saveBankConnectionProperties();
  }

  //Bank connection data is refreshed by mounting
  onMounted(loadBankConnectionWithProperties);

  return { bankConnection, bankConnectionProperties, loadBankConnectionWithProperties, saveBankConnectionWithProperties };
}
