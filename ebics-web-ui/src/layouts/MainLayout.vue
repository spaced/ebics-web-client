<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
      <q-toolbar>
        <q-btn
          flat
          dense
          round
          icon="menu"
          aria-label="Menu"
          @click="toggleLeftDrawer"
        />

        <q-toolbar-title>
          EBICS Web Client
        </q-toolbar-title>

        <div>Quasar v{{ $q.version }}</div>
      </q-toolbar>
    </q-header>

    <q-drawer
      v-model="leftDrawerOpen"
      show-if-above
      bordered
      class="bg-grey-1"
    >
      <q-scroll-area style="height: calc(100% - 0px); margin-top: 0px; border-right: 1px solid #ddd">
        <q-list padding>
          <q-item 
            to="/banks"
            exact
            clickable 
            v-ripple>
            <q-item-section avatar>
              <q-icon name="bank" />
            </q-item-section>

            <q-item-section>
              Banks
            </q-item-section>
          </q-item>

          <q-item 
            to="/users"
            exact
            clickable 
            v-ripple>
            <q-item-section avatar>
              <q-icon name="user" />
            </q-item-section>

            <q-item-section>
              Users
            </q-item-section>
          </q-item>

          <q-item 
            to="/upload"
            exact
            clickable 
            v-ripple>
            <q-item-section avatar>
              <q-icon name="list" />
            </q-item-section>

            <q-item-section>
              Upload file
            </q-item-section>
          </q-item>

          <q-item 
            to="/help"
            exact
            clickable 
            v-ripple>
            <q-item-section avatar>
              <q-icon name="help" />
            </q-item-section>

            <q-item-section>
              Help
            </q-item-section>
          </q-item>            
        </q-list>
      </q-scroll-area>
    </q-drawer>

    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script lang="ts">



import { defineComponent, ref } from 'vue'

export default defineComponent({
  name: 'MainLayout',

  components: {
    
  },

  setup () {
    const leftDrawerOpen = ref(false)

    return {
      leftDrawerOpen,
      toggleLeftDrawer () {
        leftDrawerOpen.value = !leftDrawerOpen.value
      }
    }
  }
})
</script>