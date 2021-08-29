<template>
  <q-page class="justify-evenly">
    <div class="q-pa-md">
      <h5>User initialization: {{user.name}}</h5>
      <q-stepper
        v-model="actualWizardStep"
        color="primary"
        ref="wizz"
        animated
        vertical
      >
        <q-step
          :name="UserIniWizzStep.CreateUserKeys"
          title="Create user keys"
          icon="forward_to_inbox"
          :done="isStepDone(UserIniWizzStep.CreateUserKeys)"
        >
          Continue in order to create user private public keys. You can optionally
          set PIN to protect access to your private keys.
          <q-checkbox
            v-model="user.usePassword"
            label="Protect your private keys with password (2FA)"
          />
          <q-stepper-navigation>
            <q-btn
              @click="createUserKeys()"
              color="primary"
              label="Continue"
            ></q-btn>
          </q-stepper-navigation>
        </q-step>

        <q-step
          :name="UserIniWizzStep.UploadUserKeys"
          title="Upload user keys to bank"
          icon="forward_to_inbox"
          :done="isStepDone(UserIniWizzStep.UploadUserKeys)"
        >
          Continue in order to create user keys and send them to the bank using
          the entered EBICS parameters (bank url, user, customer). For sending of
          the keys INI and HIA administrative ordertypes will be used.
          <q-checkbox
            v-model="user.usePassword"
            label="Protect your private keys with password (2FA)"
          />
          <q-stepper-navigation>
            <q-btn
              @click="uploadUserKeys()"
              color="primary"
              label="Continue"
            ></q-btn>
          </q-stepper-navigation>
        </q-step>

        <q-step
          :name="UserIniWizzStep.PrintUserLetters"
          title="Signing of user letters"
          caption="Optional"
          icon="email"
          :done="isStepDone(UserIniWizzStep.PrintUserLetters)"
        >
          In order to activate the this EBICS user you have to provide bellow
          generated hash keys to your bank. The bank will check provided hash keys
          and activate the EBICS user. 
          <q-list v-if="letters" bordered padding class="rounded-borders">
            <q-item  v-ripple>
              <q-item-section>
                <q-item-label lines="1">Signature (A005)</q-item-label>
                <q-item-label caption>{{letters.signature.hash}}</q-item-label>
              </q-item-section>
              <q-item-section side>
                <q-btn class="gt-xs" size="15px" flat dense  icon="content_copy"></q-btn>
              </q-item-section>
            </q-item>
            <q-item  v-ripple>
              <q-item-section>
                <q-item-label lines="1">Encryption (E002)</q-item-label>
                <q-item-label caption>{{letters.encryption.hash}}</q-item-label>
              </q-item-section>
              <q-item-section side>
                <q-btn class="gt-xs" size="15px" flat dense  icon="content_copy"></q-btn>
              </q-item-section>
            </q-item>
            <q-item  v-ripple>
              <q-item-section>
                <q-item-label lines="1">Authentication (X002)</q-item-label>
                <q-item-label caption>{{letters.authentication.hash}}</q-item-label>
              </q-item-section>
              <q-item-section side>
                <q-btn class="gt-xs" size="15px" flat dense  icon="content_copy"></q-btn>
              </q-item-section>
            </q-item>
          </q-list>
          
          <q-btn v-else
            @click="refreshUserLetters()"
            label="Refresh User Letters"
            color="primary"
            class="q-ml-sm"
            icon="print"
          ></q-btn>
          <q-stepper-navigation>
            <q-btn
              @click="printUserLetters()"
              color="primary"
              label="Continue"
            ></q-btn>
          </q-stepper-navigation>
        </q-step>

        <q-step
          :name="UserIniWizzStep.DownloadBankKeys"
          title="Download bank keys"
          icon="download"
          :done="isStepDone(UserIniWizzStep.DownloadBankKeys)"
        >
          Continue in order to download bank keys from your bank.
          <q-stepper-navigation>
            <q-btn
              @click="downloadBankKeys()"
              color="primary"
              label="Continue"
            ></q-btn>
          </q-stepper-navigation>
        </q-step>

        <q-step
          :name="UserIniWizzStep.VerifyBankKeys"
          title="Verify bank keys"
          icon="gpp_good"
          :done="isStepDone(UserIniWizzStep.VerifyBankKeys)"
        >
          Verify bellow downloaded bank keys with the one provided by your bank
          during onboarding. In case they not match, this connection can't be
          trussted - identity of the bank is not valid.
          <q-stepper-navigation>
            <q-btn
              @click="verifyBankKeys()"
              color="primary"
              label="Finish"
            ></q-btn>
          </q-stepper-navigation>
        </q-step>
      </q-stepper>
      <q-space />
      <q-card class="my-card">
        <q-card-section>
          <q-banner inline-actions class="text-white bg-red">
            Do you want to reset the user status?
            <template v-slot:action>
              <q-btn
                @click="resetUserStatus()"
                flat
                color="white"
                label="Reset"
              />
            </template>
          </q-banner>
        </q-card-section>
      </q-card>
    </div>
  </q-page>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { UserIniWizzStep, AdminOrderType, UserLettersResponse } from 'components/models';
import { QStepper } from 'quasar';
import useUserDataAPI from 'components/bankconnection';
import useUserInitAPI from 'components/bankconnection-init';
import usePasswordAPI from 'components/password-api';

export default defineComponent({
  name: 'UserInitalizationWizard',
  props: {
    id: {
      type: Number,
      required: true,
    },
  },
  data() {
    return {
      //'importing enum' in order to be used in template
      UserIniWizzStep,
      letters: undefined as (UserLettersResponse | undefined)
    };
  },
  methods: {
    nextStep() {
      (this.$refs.wizz as QStepper).next();
    },
    async resetUserStatus() {
      try {
        //Get password for user certificates
        await this.resetUserStatusRequest();
        //Refresshing user status on success
        await this.refreshUserData();
      } catch (error) {
        console.log(error);
      }
    },
    /*
      Create EBICS User Keys
    */
    async createUserKeys() {
      try {
        //Upload user keys (INI and/or HIA) depending on user status
        await this.createUserKeysRequest();
        //Refresshing user status on success
        await this.refreshUserData();
      } catch (error) {
        console.log(error);
      }
    },
    async uploadUserKeys() {
      try {
        //Create user letters
        this.letters = await this.getUserLetters();
        //Execute INI request
        await this.ebicsAdminTypeRequest(AdminOrderType.INI);
        //Execute HIA request
        await this.ebicsAdminTypeRequest(AdminOrderType.HIA);
        //Refresh user data
        await this.refreshUserData();
      } catch (error) {
        console.log(error);
      }
    },
    printUserLetters() {
      this.nextStep();
    },
    async refreshUserLetters() {
      this.letters = await this.getUserLetters();
    },
    async downloadBankKeys() {
      try {
        //Download bank keys using HPB order type
        await this.ebicsAdminTypeRequest(AdminOrderType.HPB);
        //Refresshing user status on success
        await this.refreshUserData();
      } catch (error) {
        console.log(error);
      }
    },
    verifyBankKeys() {
      this.nextStep();
    },
  },
  setup(props) {
    const { user, refreshUserData } = useUserDataAPI(props.id);

    const { promptCertPassword } = usePasswordAPI();

    const {
      actualWizardStep,
      isStepDone,
      createUserKeysRequest,
      ebicsAdminTypeRequest,
      resetUserStatusRequest,
      getUserLetters,
    } = useUserInitAPI(user);

    return {
      user,
      refreshUserData,
      actualWizardStep,
      isStepDone,
      createUserKeysRequest,
      ebicsAdminTypeRequest,
      resetUserStatusRequest,
      getUserLetters,
      promptCertPassword,
    };
  },
});
</script>