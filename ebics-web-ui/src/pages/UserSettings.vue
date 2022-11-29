<template>
  <q-page class="justify-evenly">
    <div class="q-pa-md">
      <div class="q-pa-md" style="max-width: 400px">
        <user-preferences saveButton/>
      </div>
    </div>
  </q-page>
</template>

<script lang="ts">
import { defineComponent, onMounted } from 'vue';
import UserPreferences from 'components/visual/UserPreferences.vue'
import useUserSettingsAPI from 'src/components/user-settings';

export default defineComponent({
  name: 'UserContext',
  components: { UserPreferences },
  setup() {
    const { loadUserSettings } = useUserSettingsAPI();
    onMounted(loadUserSettings);
    return { };
  },
});
</script>
