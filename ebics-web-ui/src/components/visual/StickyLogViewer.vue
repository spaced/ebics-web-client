<template>
  <q-page-sticky position="bottom-right" :offset="[0, 0]">
    <!-- div  class="rounded-borders bg-deep-orange-11" style="max-width: 350px; width: 500px; height: 200px">Text</div-->
    <div class="q-pa-md" style="max-width: 350px">
      <q-list bordered class="bg-grey-11">
        <q-expansion-item
          expand-separator
          icon="verified"
          label="Log (Errors: 0) Oks: 5"
          header-class="text-positive"
        >
          <q-card>
            <q-card-section>
              Lorem ipsum dolor sit amet, consectetur adipisicing elit. Quidem, eius reprehenderit eos corrupti
              commodi magni quaerat ex numquam, dolorum officiis modi facere maiores architecto suscipit iste
              eveniet doloribus ullam aliquid.
              <q-item clickable v-ripple>
                <q-item-section avatar>
                  <q-avatar color="teal" text-color="white" icon="bluetooth" />
                </q-item-section>

                <q-item-section>Avatar-type icon</q-item-section>
              </q-item>

              <q-item clickable v-ripple>
                <q-item-section avatar>
                  <q-avatar rounded color="purple" text-color="white" icon="bluetooth" />
                </q-item-section>

                <q-item-section>Rounded avatar-type icon</q-item-section>
              </q-item>
            </q-card-section>
          </q-card>
        </q-expansion-item>
      </q-list>
    </div>
  </q-page-sticky>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'StickyLogViewer',
  props: {
    disable: {
      type: Boolean,
      default: false,
    },
  },
});
</script>
