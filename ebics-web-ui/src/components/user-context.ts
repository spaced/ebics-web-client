import { ref, onMounted, watch, computed } from 'vue';
import { useRouter } from 'vue-router';
import { UserContext, AuthenticationType, UserRole } from 'components/models/user-context';
import { useQuasar } from 'quasar';
import { api } from 'boot/axios';
import { AxiosBasicCredentials } from 'axios';

//Read authentication type from quasar env variables defined in config.
function getAuthTypeFromEnv():AuthenticationType {
  switch(process.env.AUTH_TYPE) {
    case 'SSO':
      return AuthenticationType.SSO;
    case 'HTTP_BASIC':
      return AuthenticationType.HTTP_BASIC;
    case 'SERVER':
      return AuthenticationType.SERVER;
    default:
      return AuthenticationType.SSO;
  }
}

function getAuthSSOBasicTypeFromEnv():boolean {
  return process.env.AUTH_TYPE_SSO_OVER_BASIC ? true : false;
}

const authenticationType = ref<AuthenticationType>(getAuthTypeFromEnv());
const refreshUserContextByMounth = true;
const ssoDevOverBasic = ref(getAuthSSOBasicTypeFromEnv());

//Reactive http basic credentials object available in browser session
//Initally no default password & username to force login, can be changed for dev purposes
const basicCredentials = ref<AxiosBasicCredentials>({
  username: '',
  password: '',
});

//Reactive authorization context updated from backend (in case of undefined no login call was done yet.)
const userContext = ref<UserContext | undefined>(undefined);

/**
 * Logged User composition API in order to get authorization&authetication context
 * @returns
 *  userContext - user authorization context, name and roles
 *  refreshUserContextData - function to refresh user context (login)
 */
export default function useUserContextAPI() {
  const q = useQuasar();
  const router = useRouter();

  const resetUserContextData = async () => {
    switch (authenticationType.value) {
      case AuthenticationType.SSO:
        await refreshUserContextData();
        break;
      case AuthenticationType.SERVER:
        await api.get('logout');
      default:
        basicCredentials.value = {username: '', password: ''};
        userContext.value = undefined;
        await router.push({path: 'login'});
        q.notify({
          color: 'positive',
          position: 'bottom-right',
          message: 'Logged out',
          icon: 'report_problem',
        });
    }
  };

  const hasCredentials = (): boolean =>
    basicCredentials.value.username !== '' &&
    basicCredentials.value.password !== '';

  const onSuccessUserContext = (uc: UserContext) => {
    userContext.value = uc;
    userContext.value.time = new Date().toISOString();
    q.notify({
      color: 'positive',
      position: 'bottom-right',
      message: 'Authentication successful',
      icon: 'report_problem',
    });
  }

  const onFailedUserContext = (error: unknown) => {
    userContext.value = undefined;
    q.notify({
      color: 'negative',
      position: 'bottom-right',
      message: `Authentication failed: ${JSON.stringify(error)}`,
      icon: 'report_problem',
    });
  }

  const login = async (): Promise<void> => {
    if (authenticationType.value == AuthenticationType.SERVER) {
      try {
        const formData = new FormData();
        formData.set('username', basicCredentials.value.username);
        formData.set('password', basicCredentials.value.password);
        const response = await api.post<UserContext>('login', formData, {headers: {'Content-Type': 'application/x-www-form-urlencoded'}});
        if (response.status == 200) {
          onSuccessUserContext(response.data);
        }
      } catch (error) {
        onFailedUserContext(error)
      }
    } else await refreshUserContextData()
  }


  const refreshUserContextData = async (): Promise<void> => {
    if (
      authenticationType.value == AuthenticationType.HTTP_BASIC &&
      !hasCredentials()
    ) {
      //In case we use http basic, and we dont have yet credential lets redirect to login
      await router.push({ path: 'login' });
    } else {
      //Simulate SSO via HTTP basic with given credentials
      if (
        ssoDevOverBasic.value &&
        authenticationType.value == AuthenticationType.SSO
      ) {
        console.warn(
          'Setting default HTTP basic credentials for dev mode only'
        );
        api.defaults.auth = { username: 'admin', password: 'pass' };
      }

      //We have credential, we do user API call to get principal and roles from backend
      try {
        const response = await api.get<UserContext>('user');
        onSuccessUserContext(response.data);
      } catch (error) {
        if (authenticationType.value == AuthenticationType.SERVER) {
          await router.push({ path: 'login' });
        } else onFailedUserContext(error);
        throw error;
      }
    }
  };

  //Check if the user has specific role
  const hasRole = (userRoleName: UserRole): boolean => {
    return (
      userContext.value !== undefined &&
      userContext.value.roles.some((roleName) =>
        roleName.includes(userRoleName)
      )
    );
  };

  const hasRoleGuest = computed(():boolean => {
    return hasRole(UserRole.GUEST);
  });
  const hasRoleUser = computed(():boolean => {
    return hasRole(UserRole.USER);
  });
  const hasRoleAdmin = computed(():boolean => {
    return hasRole(UserRole.ADMIN);
  });

  const onCredentialsChanged = () => {
    if (authenticationType.value == AuthenticationType.HTTP_BASIC) {
      api.defaults.auth = basicCredentials.value;
    }
  };

  //To keep axios default basic auth credential for all axios call in sync with reactive basicCredentials
  watch(basicCredentials.value, onCredentialsChanged);

  if (refreshUserContextByMounth) {
    //User data is refreshed by mounting if required
    onMounted(refreshUserContextData);
  }

  return {
    authenticationType,
    ssoDevOverBasic,
    userContext,
    basicCredentials,
    hasRole,
    hasRoleGuest,
    hasRoleUser,
    hasRoleAdmin,
    resetUserContextData,
    refreshUserContextData,
    login
  };
}
