<template>
  <q-select
    filled
    v-model="fileTemplateVal"
    :options="fileTemplates"
    :option-label="fileTemplateLabel"
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
          <div class="text-h6">Adjust order types filter settings</div>
        </q-card-section>

        <user-preferences sectionFilter="OrderTypesSettings" />

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
    const { fileTemplates } = useFileTemplateAPI();

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

    const saveTemplateDialog = ref(false);
    const saveAsNew = ref<boolean>(false);
    const selectedFileTemplateTags = ref<string[]>();
    const allFileTemplateTagsArray = computed(() => {
      const nonUniqueTags = fileTemplates.value?.flatMap((t) =>
        t.templateTags.split(',')
      );
      return [...new Set(nonUniqueTags)];
    });
    const filteredFileTemplateTags = ref<string[]>();
    const filterFileTemplateTags = (
      val: string,
      update: (fn: () => void) => void
    ) => {
      update(() => {
        if (val === '') {
          filteredFileTemplateTags.value = allFileTemplateTagsArray.value;
        } else {
          const needle = val.toLowerCase();
          filteredFileTemplateTags.value =
            allFileTemplateTagsArray.value?.filter(
              (v) => v.toLowerCase().indexOf(needle) > -1
            );
        }
      });
    };
    const createFileTemplateTag = (
      val: string,
      done: (val: string, res: string) => void
    ): void => {
      // Calling done(var) when new-value-mode is not set or "add", or done(var, "add") adds "var" content to the model
      // and it resets the input textbox to empty string
      // ----
      // Calling done(var) when new-value-mode is "add-unique", or done(var, "add-unique") adds "var" content to the model
      // only if is not already set
      // and it resets the input textbox to empty string
      // ----
      // Calling done(var) when new-value-mode is "toggle", or done(var, "toggle") toggles the model with "var" content
      // (adds to model if not already in the model, removes from model if already has it)
      // and it resets the input textbox to empty string
      // ----
      // If "var" content is undefined/null, then it doesn't tampers with the model
      // and only resets the input textbox to empty string

      if (val.length > 0) {
        if (!allFileTemplateTagsArray?.value?.includes(val)) {
          allFileTemplateTagsArray.value?.push(val);
        }
        done(val, 'toggle');
      }
    };

    const editTemplate = (fileTemplate: FileTemplate): void => {
      console.log('edit');
    };

    const deleteTemplate = (fileTemplate: FileTemplate): void => {
      console.log('delete');
    };

    const addTemplate = (): void => {
      console.log('add');
    };

    return {
      editTemplate,
      deleteTemplate,
      addTemplate,

      fileTemplateVal,
      configureOrdertypesDropdown,
      saveUserSettings,
      fileTemplates,
      fileTemplateLabel,
      saveTemplateDialog,
      saveAsNew,
      selectedFileTemplateTags,
      filteredFileTemplateTags,
      filterFileTemplateTags,
      createFileTemplateTag,
    };
  },
});
</script>
