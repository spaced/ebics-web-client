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
      <!-- q-btn
        round
        dense
        flat
        icon="library_add"
        @click.stop="saveTemplateDialog = true"
      >
        <q-tooltip> Save the content as template.. </q-tooltip>
      </q-btn-->
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
    <!-- 
          <div class="q-pa-md">
      <div class="q-pa-md" style="max-width: 400px">        
    -->
    <q-dialog v-model="saveTemplateDialog">
      <q-card class="q-pa-md" style="min-width: 500px">
        <q-card-section>
          <div class="text-h6">Save template</div>
        </q-card-section>

        <q-input
          filled
          v-model="fileTemplateVal.templateName"
          label="Template name"
          hint="Template name which describes the purpose"
          lazy-rules
          :rules="[
            (val) =>
              (val && val.length > 3) || 'Please enter valid template name',
          ]"
        />
        <q-select
          filled
          v-model="selectedFileTemplateTags"
          use-input
          use-chips
          multiple
          input-debounce="0"
          @new-value="createFileTemplateTag"
          :options="filteredFileTemplateTags"
          @filter="filterFileTemplateTags"
        ></q-select>

        <q-card-actions align="right">
          <q-btn
            flat
            label="Ovewrite selected"
            color="primary"
            v-close-popup
            @click="saveUserSettings()"
          />
          <q-btn
            flat
            label="Add new template"
            color="primary"
            v-close-popup
            @click="saveUserSettings()"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
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
          <!-- q-item-label caption>{{
            fileTemplatesOpt.opt.templateTags
          }}</q-item-label-->
          <q-item-label>
            <q-chip
              v-for="tag in fileTemplatesOpt.opt.templateTags.split(',')"
              v-bind:key="tag"
              size="xs"
              color="primary"
              text-color="white"
              icon="euro"
            >
              {{ tag }}
            </q-chip>
          </q-item-label>
        </q-item-section>
        <!--
        <q-item-section v-if="fileTemplatesOpt.opt.canBeEdited" avatar>
          <q-btn
            flat
            dense
            rounded
            icon="edit"
            label="edit"
            
            @click.stop="saveTemplateDialog = true"
          />
        </q-item-section>
        <q-item-section v-if="fileTemplatesOpt.opt.canBeEdited" avatar>
          <q-btn
            flat
            dense
            rounded
            icon="delete"
            label="delete"
            @click="deleteTemplate(fileTemplatesOpt.opt)"
          />
        </q-item-section>-->
      </q-item>
    </template>
    <!-- template v-slot:after-options>
      <q-item>
        <q-item-section avatar>
          <q-btn
            flat
            dense
            rounded
            icon="add"
            label="add custom template"
            @click="addTemplate()"
          />
        </q-item-section>
      </q-item>
    </template -->
  </q-select>
</template>

<script lang="ts">
import { defineComponent, computed, PropType, ref } from 'vue';
import { FileTemplate } from 'components/models/file-template';
import useFileTemplateAPI from 'src/components/file-templates-api';
import useUserSettingsAPI from 'components/user-settings';
import UserPreferences from 'components/visual/UserPreferences.vue';

export default defineComponent({
  name: 'FileTemplateSelect',
  components: { UserPreferences },
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
