<template>
  <q-select
    filled
    use-input
     @filter="filterTemplates"
    v-model="fileTemplateVal"
    :options="filteredFileTemplates"
    :option-label="fileTemplateLabel"
    clearable
    label="Select the template here.."
    hint="Select file template you would like to upload"
  >
    <template v-slot:append>
      <q-btn
        round
        dense
        flat
        icon="settings"
        @click.stop="configureOrdertypesDropdown = true"
      >
        <q-tooltip> Adjust filter to display/hide templates.. </q-tooltip>
      </q-btn>
    </template>

    <q-dialog v-model="configureOrdertypesDropdown">
      <q-card>
        <q-card-section>
          <div class="text-h6">Adjust upload templates filter settings</div>
        </q-card-section>

        <user-preferences sectionFilter="FileTemplateSettings" />

        <q-card-actions align="right">
          <q-btn
            flat
            label="Save"
            color="primary"
            v-close-popup
            @click="saveUserSettings()"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
    <template v-slot:option="fileTemplatesOpt">
      <q-item v-bind="fileTemplatesOpt.itemProps">
        <q-item-section>
          <q-item-label v-html="fileTemplateLabel(fileTemplatesOpt.opt)" />
          <file-template-tags :tags="fileTemplatesOpt.opt.templateTags" />
        </q-item-section>
      </q-item>
    </template>
  </q-select>
</template>

<script lang="ts">
import { defineComponent, computed, PropType, ref } from 'vue';
import { FileTemplate } from 'components/models/file-template';
import useFileTemplateAPI from 'src/components/file-templates-api';
import useUserSettingsAPI from 'components/user-settings';
import UserPreferences from 'components/visual/UserPreferences.vue';
import FileTemplateTags from 'components/visual/FileTemplateTags.vue';

export default defineComponent({
  name: 'FileTemplateSelect',
  components: { UserPreferences, FileTemplateTags },
  props: {
    fileTemplate: {
      type: Object as PropType<FileTemplate>,
      required: false,
    },
  },
  emits: ['update:fileTemplate'],
  setup(props, { emit }) {
    const configureOrdertypesDropdown = ref(false);

    const { saveUserSettings } = useUserSettingsAPI();
    const { displayedFileTemplates: fileTemplates } = useFileTemplateAPI();

    const fileTemplateVal = computed<FileTemplate>({
      get() {
        return props.fileTemplate as FileTemplate;
      },
      set(value) {
        emit('update:fileTemplate', value);
      },
    });

    const fileTemplateLabel = (
      fileTemplate: FileTemplate | undefined
    ): string => {
      return fileTemplate ? fileTemplate.templateName : '';
    };

    const filteredFileTemplates = ref<FileTemplate[] | unknown>([]);
    
    const filterTemplates = (
      val: string,
      update: (fn: () => void) => void
    ) => {
      update(() => {
        if (val === '') {
          filteredFileTemplates.value = fileTemplates.value;
        } else {
          const needle = val.toLowerCase();
          filteredFileTemplates.value = fileTemplates.value?.filter(template => {
            return template.templateName.toLowerCase().includes(needle) || 
                  template.templateTags.toLowerCase().includes(needle);
          });
        }
      });
    };

    return {
      fileTemplateVal,
      configureOrdertypesDropdown,
      saveUserSettings,
      fileTemplates,
      filteredFileTemplates,
      filterTemplates,
      fileTemplateLabel,
    };
  },
});
</script>
