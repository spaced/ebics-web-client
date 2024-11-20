import { ref, watch, Ref } from 'vue';
import {
  BankConnection
} from 'components/models/ebics-bank-connection';
import {
  EbicsVersion,
} from 'components/models/ebics-version';
import {
  BTFType,
  OrderTypeFilter,
  OrderType,
  TransferType
} from 'components/models/ebics-order-type';
import useFileTransferAPI from 'components/filetransfer';
import { CustomMap } from 'components/utils';
import usePasswordAPI from 'components/password-api';
import useBanksAPI from 'components/banks';
import useUserSettingsAPI from 'components/user-settings';

//Global internal cache of all OrderTypes for all active bank connections..
const orderTypeCache: CustomMap<number, OrderType[]> = new CustomMap<
  number,
  OrderType[]
>();

//Global internal cache of all BTF's for all active bank connections..
const btfTypeCache: CustomMap<number, BTFType[]> = new CustomMap<
  number,
  OrderType[]
>();

export default function useOrderTypesAPI(
  selectedBankConnection: Ref<BankConnection | undefined>,
  activeBankConnections: Ref<readonly BankConnection[]>,
  filterType: Ref<OrderTypeFilter>,
) {
  //BTF   types of selectedBankConnection filtered by filterType
  const outputBtfTypes: Ref<BTFType[]> = ref([]);
  //Order types of selectedBankConnection filtered by filterType
  const outputOrderTypes: Ref<OrderType[]> = ref([]);

  const loading = ref<boolean>(false);

  //Used for the displayAdminTypes filter
  const { userSettings } = useUserSettingsAPI();
  const { ebicsOrderTypes } = useFileTransferAPI();
  const { promptCertPassword } = usePasswordAPI();
  const { isEbicsVersionAllowedForUse } = useBanksAPI(true);

  /**
   * If the @param orderTypesCache is empty or refresh is forced,
   * Then download available ordertypes from EBICS server and store them to the cache
   * @param bankConnection
   * @param orderTypesCache used to store output
   * @param forceCashRefresh force refresh even if there is already cached result
   */
  const updateOrderTypesH004CacheForBankConnection = async (
    bankConnection: BankConnection,
    forceCashRefresh = false,
    useServerCache = true,
  ) => {
    const orderTypesCache: OrderType[] | undefined = orderTypeCache.get(bankConnection.id);

    if (
      isEbicsVersionAllowedForUse(
        bankConnection.partner.bank,
        EbicsVersion.H004
      ) &&
      (orderTypesCache === undefined || orderTypesCache.length == 0 || forceCashRefresh)
    ) {

      console.log(`order types H004 refresh attempt ${bankConnection.name}`)

      const orderTypesRefreshPromise = ebicsOrderTypes(
        bankConnection,
        EbicsVersion.H004,
        useServerCache
      ) as Promise<OrderType[]>;

      const loadedOrderTypes = await orderTypesRefreshPromise
      orderTypeCache.set(bankConnection.id, loadedOrderTypes)

      if (bankConnection.id == selectedBankConnection.value?.id)
        refreshOutputOrderTypes(bankConnection);
    }
  };

  /**
   * If the @param orderTypesCache is empty or refresh is forced,
   * Then download available ordertypes from EBICS server
   * @param bankConnection
   * @param orderTypeList used to store output
   * @param forceCashRefresh force refresh even if there is already cached result
   * @param useServerCache if false then request directly via EBICS, otherwise try to look into server cache first
   */
  const updateBtfTypesH005CacheForBankConnection = async (
    bankConnection: BankConnection,
    forceCashRefresh = false,
    useServerCache = true,
  ) => {

    const btfTypesCache: BTFType[] | undefined = btfTypeCache.get(bankConnection.id);

    if (
      isEbicsVersionAllowedForUse(
        bankConnection.partner.bank,
        EbicsVersion.H005
      ) &&
      (btfTypesCache === undefined || btfTypesCache.length == 0 || forceCashRefresh)
    ) {
      console.log(`btf types H005 refresh aptemt ${bankConnection.name}`)

      const orderTypesRefreshPromise = ebicsOrderTypes(
        bankConnection,
        EbicsVersion.H005,
        useServerCache
      ) as Promise<BTFType[]>;

      const loadedBtfTypes = await orderTypesRefreshPromise
      btfTypeCache.set(bankConnection.id, loadedBtfTypes);

      if (bankConnection.id == selectedBankConnection.value?.id)
        refreshOutputBtfTypes(bankConnection);
    }
  };

  /**
   * Refresh ordertypes & BTF types cache for given bank connection
   * @param bankConnection
   * @param forceCashRefresh force refreshing even if the cache already have the order types.
   */
  const updateOrderTypesCacheForBankConnection = async (
    bankConnection: BankConnection,
    forceCashRefresh = false,
    suppressLoadingStateChange = false,
  ) => {

    //For pasword protected connection ask first password
    //It prevents parallel poping of UI password dialog
    await promptCertPassword(bankConnection, false);

    if (!suppressLoadingStateChange) loading.value = true;

    //Now execute all update promisses
    //the password UI would not pop-up any more because of previous promptCertPassword
    await Promise.allSettled([
      updateOrderTypesH004CacheForBankConnection(
        bankConnection,
        forceCashRefresh
      ),
      updateBtfTypesH005CacheForBankConnection(
        bankConnection,
        forceCashRefresh
      ),
    ]);

    if (!suppressLoadingStateChange) loading.value = false;
  };

  const refreshOutputOrdertypesForSelectedBankConnection = () => {
    if (selectedBankConnection.value) {
      refreshOutputBtfTypes(selectedBankConnection.value);
      refreshOutputOrderTypes(selectedBankConnection.value);
    }
  };

  const downloadableAdminOrderTypes = ['HAC', 'HAA', 'HPD', 'HKD', 'HTD'];

  const refreshOutputBtfTypes = (bankConnection: BankConnection) => {
    const selectedTypes = btfTypeCache.get(bankConnection.id);
    if (selectedTypes) {
      if (filterType.value == OrderTypeFilter.All) {
        outputBtfTypes.value = selectedTypes;
      } else if (filterType.value == OrderTypeFilter.UploadOnly) {
        outputBtfTypes.value = selectedTypes.filter(
          (btf) => btf.adminOrderType == 'BTU'
        );
      } else if (filterType.value == OrderTypeFilter.DownloadOnly) {
        outputBtfTypes.value = selectedTypes.filter(
          (btf) =>
            btf.adminOrderType == 'BTD' ||
            (userSettings.value.displayAdminTypes &&
              downloadableAdminOrderTypes.includes(btf.adminOrderType))
        );
      }
    } else {
      console.warn(
        'No BTF types cached for given bank connection: ' +
          bankConnection.name
      );
    }
  };

  const refreshOutputOrderTypes = (bankConnection: BankConnection) => {
    const selectedTypes = orderTypeCache.get(bankConnection.id);
    if (selectedTypes) {
      if (filterType.value == OrderTypeFilter.All) {
        outputOrderTypes.value = selectedTypes;
      } else if (filterType.value == OrderTypeFilter.UploadOnly) {
        outputOrderTypes.value = selectedTypes.filter(
          (ot) => ot.adminOrderType == 'UPL' || ot.adminOrderType == 'FUL'
        );
      } else if (filterType.value == OrderTypeFilter.DownloadOnly) {
        outputOrderTypes.value = selectedTypes.filter(
          (ot) =>
            ot.adminOrderType == 'DNL' ||
            ot.adminOrderType == 'FDL' ||
            (userSettings.value.displayAdminTypes &&
              downloadableAdminOrderTypes.includes(ot.adminOrderType)) ||
            ot.transferType == TransferType.Download
        );
      }
    } else {
      console.warn(
        'No OrderTypes cached for given bank connection: ' +
        bankConnection.name
      );
    }
  };

  watch(
    selectedBankConnection,
    refreshOutputOrdertypesForSelectedBankConnection
  );

  watch(
    () => userSettings.value.displayAdminTypes,
    refreshOutputOrdertypesForSelectedBankConnection
  );

  const refreshOrderTypes = async(bankConnection?: BankConnection): Promise<void> => {
    if (bankConnection) await updateOrderTypesH004CacheForBankConnection(bankConnection, true, false)
  };

  const refreshBtfTypes = async(bankConnection?: BankConnection): Promise<void> => {
    if (bankConnection) await updateBtfTypesH005CacheForBankConnection(bankConnection, true, false)
  };

  return {
    btfTypes: outputBtfTypes,
    orderTypes: outputOrderTypes,
    //Can be used for forced refresh from UI
    refreshOrderTypes,
    refreshBtfTypes,
    updateOrderTypesCacheForBankConnection,
    loading,
  };
}
