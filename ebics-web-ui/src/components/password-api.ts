import { ref, Ref } from 'vue';
import { useQuasar } from 'quasar';
import { BankConnection } from 'components/models/ebics-bank-connection'
import useBaseAPI from 'components/base-api';

/**
 * Passwords entered on actual session for the given @param user.id
 * @returns
 *  promptCertPassword getting the certificate password, if not yet entered in this session by user, then asking for it via dialog
 *  resetCertPassword resetting the actual certificate password for given user in this session (used if is wrong password used)
 */
const tempPasswords: Ref<Map<number, string | undefined>> = ref<Map<number, string | undefined>>(new Map());

export default function usePasswordAPI() {
  const q = useQuasar();

  const passwordDialog = (user: BankConnection, createPass: boolean): Promise<string> => {
    return new Promise<string>((resolve, reject) => {
      q.dialog({
        title: createPass ? 'Create new password' : 'Enter password',
        message: createPass
          ? 'Create new password for your user certificate'
          : `Enter your password for user certificate for bank connection: '${user.name}'`,
        prompt: {
          model: '',
          type: 'password',
        },
        cancel: true,
        persistent: true,
      })
        .onOk((data: unknown) => {
            const pwd = data as string
            tempPasswords.value.set(user.id, pwd)
          console.log(`Entered password: ${pwd} for user ${user.id}`);
          resolve(pwd);
        })
        .onCancel(() => {
          reject('Password entry canceled');
        })
        .onDismiss(() => {
          reject('Password entry dismissed');
        });
    });
  };

  /**
   * Asking for user certificate password, if required
   * createPass=true in order to ask for new password
   * createPass=false in order to ask for existing password
   */
  const promptCertPassword = (user: BankConnection, createPass: boolean): Promise<string> => {
    return new Promise<string>((resolve) => {
      if (!user.usePassword) {
        console.log('No pass required');
        resolve(''); //No password required
      } else {
        //Password required, did we stored some already?
        const pwd = tempPasswords.value.get(user.id);
        if (pwd !== undefined) {
          console.log(`Temp pass used ${pwd}`);
          resolve(pwd);
        } else {
          //We will ask user for password
          resolve(passwordDialog(user, createPass));
        }
      }
    });
  };

  const resetCertPassword = (user: BankConnection): void => {
    tempPasswords.value.set(user.id, undefined)
  }

  const {apiOkHandler, apiErrorHandler} = useBaseAPI();

  const pwdApiOkHandler = (bankConnection: BankConnection, msg: string, silent?: boolean): void => {
    apiOkHandler(msg, bankConnection, silent);
  }

  const pwdApiErrorHandler = (
    bankConnection: BankConnection,
    msg: string,
    error: unknown,
    silent?: boolean,
  ): void => {
    apiErrorHandler(msg, error, bankConnection, (errorMessage) => {
        if (errorMessage?.includes('wrong password')) {
          //In case of error 'wrong password' we have to reset temporary stored password in order to ask for new one
          resetCertPassword(bankConnection)
        }
    }, silent)
  }

  return { pwdApiOkHandler, pwdApiErrorHandler, resetCertPassword, promptCertPassword };
}
