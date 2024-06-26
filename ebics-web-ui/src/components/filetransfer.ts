import { BankConnection  } from 'components/models/ebics-bank-connection';
import { UploadRequest, UploadResponse, DownloadRequest, UserPassword } from 'components/models/ebics-request-response';
import { BTFType, OrderType } from 'components/models/ebics-order-type';
import { api } from 'boot/axios';
import usePasswordAPI from 'components/password-api';
import { AxiosResponse } from 'axios';

export default function useFileTransferAPI() {
  const { pwdApiOkHandler, pwdApiErrorHandler, promptCertPassword } =
    usePasswordAPI();

  /**
   * Executest EBICS upload file request
   * @param adminOrderType
   * @param pass
   * @returns
   */
  const ebicsUploadRequest = async (
    bankConnection: BankConnection,
    uploadRequest: UploadRequest,
    uploadFile: Blob,
    ebicsVersion: string = bankConnection.ebicsVersion,
  ): Promise<UploadResponse | undefined> => {
    try {
      uploadRequest.password = await promptCertPassword(bankConnection, false);
      console.log(JSON.stringify(uploadRequest));
      const formData = new FormData();
      formData.append(
        'uploadRequest',
        new Blob([JSON.stringify(uploadRequest)], { type: 'application/json' })
      );
      formData.append('uploadFile', uploadFile);
      const response = await api.post<UploadRequest, AxiosResponse<UploadResponse>>(
        `bankconnections/${bankConnection.id}/${ebicsVersion}/upload`,
        formData,
        { headers: { 'Content-Type': 'multipart/form-data' } }
      );
      console.log('Upload response: ' + JSON.stringify(response))
      pwdApiOkHandler(bankConnection,
        `File uploaded successfully for bank connection: ${bankConnection.name}, order number: ${response.data.orderNumber}`
      );
      return response.data;
    } catch (error) {
      pwdApiErrorHandler(bankConnection, 'File upload failed: ', error);
    }
  };

  /**
   * Executest EBICS download file request
   * @param downloadRequest
   * @param ebicsVersion
   * @returns
   */
     const ebicsDownloadRequest = async (
      bankConnection: BankConnection,
      downloadRequest: DownloadRequest,
      ebicsVersion: string = bankConnection.ebicsVersion,
    ): Promise<Blob | undefined> => {
      try {
        downloadRequest.password = await promptCertPassword(bankConnection, false);
        console.log(JSON.stringify(downloadRequest));
        const response = await api.post<DownloadRequest, AxiosResponse<string>>(
          `bankconnections/${bankConnection.id}/${ebicsVersion}/download`,
          downloadRequest, {responseType: 'arraybuffer'}
        );
        pwdApiOkHandler(bankConnection,
          `File downloaded successfully for bank connection: ${bankConnection.name}`
        );
        return new Blob([response.data]);
      } catch (error) {
        pwdApiErrorHandler(bankConnection, 'File download failed: ', error);
      }
    };

    /**
     * Executest EBICS HTD request in order to get avaialable order types  
     * @param bankConnection bank connection for this request
     * @param ebicsVersion ebics version used, if other that from bank connection default
     * @param useServerCache if true then try to find result in server cache first, otherwise goes directly for EBICS request without cache
     * @returns available order types, in case of error emtpty list
     */
    const ebicsOrderTypes = async (
      bankConnection: BankConnection,
      ebicsVersion: string = bankConnection.ebicsVersion,
      useServerCache: boolean,
    ): Promise<BTFType[] | OrderType[]> => {
      try {
        const password = await promptCertPassword(bankConnection, false);
        const response = await api.post<UserPassword, AxiosResponse<BTFType[]>>(
          `bankconnections/${bankConnection.id}/${ebicsVersion}/orderTypes`,
          {password: password, useCache: useServerCache},
        );
        console.debug(
          `Order types ${ebicsVersion} loaded for bank connection ${
            bankConnection.name
          }, types: ${JSON.stringify(response.data)}`
        );
        pwdApiOkHandler(bankConnection,
          `Ordertypes refreshed successfully for bank connection: ${bankConnection.name}`, true
        );
        return response.data;
      } catch (error) {
        pwdApiErrorHandler(bankConnection, `Order types refresh for bank connection ${bankConnection.name} and ${ebicsVersion} failed: `, error, true);
        return [];
      }
    };

  return {
    ebicsUploadRequest, ebicsDownloadRequest, ebicsOrderTypes,
  };
}
