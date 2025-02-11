<template>
  <q-page class="justify-evenly">
    <div class="q-pa-md">
      <div class="q-pa-md">
        <q-form @submit="onSubmit" @reset="onCancel" class="q-gutter-md">
          <q-input
            :disable="!fileTemplate.canBeEdited"
            filled
            v-model="fileTemplate.templateName"
            label="File template name"
            hint="User defined name for this file uplaod template"
            lazy-rules
            :rules="[
              (val) =>
                (val && val.length >= 10) ||
                'Template name must be at least 10 characters',
            ]"
          />

          <q-select
            :disable="!fileTemplate.canBeEdited"
            filled
            label="File template tags"
            hint="Template tags used for easier identification of template"
            v-model="fileTemplateTagsArray"
            use-input
            use-chips
            multiple
            input-debounce="0"
            @new-value="createFileTemplateTag"
            :options="filteredFileTemplateTags"
            @filter="filterFileTemplateTags"
          ></q-select>

          <q-select
            :disable="!fileTemplate.canBeEdited"
            filled
            label="Template file format"
            hint="Template format used for highligting &amp; template placeholder substitutions"
            v-model="fileTemplate.fileFormat"
            :options="Object.values(FileFormat)"
          ></q-select>

          <q-item tag="label" v-ripple :disable="!fileTemplate.canBeEdited">
            <q-item-section avatar>
              <q-checkbox
                :disable="!fileTemplate.canBeEdited"
                v-model="fileTemplate.guestAccess"
              />
            </q-item-section>
            <q-item-section>
              <q-item-label>Share this file upload template</q-item-label>
              <q-item-label caption
                >If enabled then this template will be available to every
                user</q-item-label
              >
            </q-item-section>
          </q-item>

          <v-ace-editor
            ref="contentEditor"
            v-model:value="fileTemplate.fileContentText"
            :lang="editorLang"
            theme="clouds"
            style="height: 300px"
            :printMargin="false"
          />

          <div>
            <q-btn
              v-if="action == 'create'"
              label="Add"
              type="submit"
              color="primary"
            />
            <q-btn
              v-if="action == 'edit'"
              label="Update"
              type="submit"
              color="primary"
            />
            <q-btn
              v-if="action == 'copy'"
              label="Copy"
              type="submit"
              color="primary"
            />
            <q-btn
              label="Cancel"
              type="reset"
              color="primary"
              flat
              class="q-ml-sm"
              icon="undo"
            />
          </div>
        </q-form>
      </div>
    </div>
  </q-page>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import useFileTemplateAPI from 'components/file-template-api';
import useFileTemplatesAPI from 'components/file-templates-api';
import { FileFormat } from 'components/models/file-format';

import { VAceEditor } from 'vue3-ace-editor';
import 'ace-builds/src-noconflict/mode-xml';
import 'ace-builds/src-noconflict/theme-clouds';

export default defineComponent({
  name: 'UploadTemplate',
  components: { VAceEditor },
  props: {
    id: {
      type: Number,
      required: false,
      default: undefined,
    },
    action: {
      type: String,
      required: true,
    },
  },
  methods: {},
  setup(props) {
    const router = useRouter();
    const { fileTemplate, fileTemplateTagsArray, createOrUpdateFileTemplate } =
      useFileTemplateAPI(props.id, props.action == 'copy');
    const { allFileTemplateTagsArray } = useFileTemplatesAPI();
    const onCancel = (): void => {
      router.go(-1);
    };
    const onSubmit = async () => {
      if (await createOrUpdateFileTemplate()) {
        router.go(-1);
      }
    };

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
        const existingTagName = allFileTemplateTagsArray?.value.find(
          (tag) => tag.toUpperCase() == val.toUpperCase()
        );

        if (!existingTagName) {
          allFileTemplateTagsArray.value?.push(val);
        }

        const tagName = existingTagName ? existingTagName : val;

        done(tagName, 'add-unique');
      }
    };

    const editorLang = computed((): string => {
      if (fileTemplate.value?.fileFormat == FileFormat.XML) return 'xml';
      if (fileTemplate.value?.fileFormat == FileFormat.SWIFT) return 'text';
      else return 'xml';
    });

    return {
      allFileTemplateTagsArray,
      fileTemplateTagsArray,
      filterFileTemplateTags,
      filteredFileTemplateTags,
      createFileTemplateTag,

      editorLang,
      fileTemplate,
      onCancel,
      onSubmit,

      FileFormat,
    };
  },
});
</script>
