<template>
  <q-page class="justify-evenly">
    <div class="q-pa-md">
      <q-table
        title="File uplaod templates"
        :filter="filter"
        :rows="fileTemplates"
        :columns="columns"
        row-key="id"
        selection="single"
        :pagination="{rowsPerPage: 10}"
      >
        <template v-slot:header="props">
          <q-tr :props="props">
            <q-th v-for="col in props.cols.filter(col => col.name != 'canBeEdited')" :key="col.name" :props="props">
              {{ col.label }}
            </q-th>
            <q-th auto-width></q-th>
          </q-tr>
        </template>
        <template v-slot:body="props">
          <q-tr :props="props">
            <q-td v-for="col in props.cols.filter(col => col.name != 'canBeEdited')" :key="col.name" :props="props">
              <div v-if="col.name != 'name'">
                {{ col.value }}
              </div>
              <div v-else>
                {{ col.value }}
                <file-template-tags :tags="props.row.templateTags" />
              </div>
            </q-td>
            <q-td :style="{ width: '270px' }">
              <div v-if="props.cols[0].value" class="q-gutter-sm">
                <q-btn 
                  size="sm"
                  color="primary"
                  label="Edit"
                  icon-right="edit"
                  no-caps
                  @click="routeToTemplatePage(Number(props.key))"
                />
                <q-btn 
                  size="sm"
                  label="Copy"
                  color="accent"
                  icon-right="content_copy"
                  @click="routeToTemplatePage(Number(props.key), true);"
                />
                <q-btn 
                  size="sm"
                  label="Delete"
                  color="accent"
                  icon-right="delete"
                  @click="deleteTemplate(Number(props.key),props.cols[0].value);"
                />
              </div>
              <div v-else class="q-gutter-sm">
                <q-btn 
                  size="sm"
                  color="primary"
                  label="View"
                  icon-right="description"
                  no-caps
                  @click="routeToTemplatePage(Number(props.key))"
                />
                <q-btn 
                  size="sm"
                  label="Copy"
                  color="accent"
                  icon-right="content_copy"
                  @click="routeToTemplatePage(Number(props.key), true);"
                />
              </div>
            </q-td>
          </q-tr>
        </template>
        <template v-slot:top-left>
          <q-input
            borderless
            dense
            debounce="300"
            v-model="filter"
            placeholder="Search"
          >
            <template v-slot:append>
              <q-icon name="search" />
            </template>
          </q-input>
        </template>
        <template v-slot:top-right>
          <div class="q-pa-md q-gutter-sm">
            <q-btn
              color="primary"
              label="Add Template"
              icon-right="add"
              no-caps
              @click="routeToTemplatePage()"
            />
          </div>
        </template>
      </q-table>
    </div>
  </q-page>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useQuasar } from 'quasar';
import { FileTemplate } from 'components/models/file-template';
import useFileTemplateAPI from 'components/file-templates-api';
import FileTemplateTags from 'components/visual/FileTemplateTags.vue';

export default defineComponent({
  name: 'Templates',
  components: {FileTemplateTags},
  setup() {
    const router = useRouter();
    const q = useQuasar();
    const filter = ref('');
    const columns = [
      { //The first column canBeEdited is not diplayed
        name: 'canBeEdited',
        required: true,
        label: 'Can be edited',
        align: 'left',
        field: (row: FileTemplate) => row.canBeEdited,
        sortable: true,
      },
      {
        name: 'name',
        required: true,
        label: 'Template name',
        align: 'left',
        field: (row: FileTemplate) => row.templateName,
        sortable: true,
      },
      /*{
        name: 'tags',
        required: true,
        label: 'Template tags',
        align: 'left',
        field: (row: FileTemplate) => row.templateTags,
        sortable: true,
      },*/
      {
        name: 'creator',
        required: true,
        label: 'Creator',
        align: 'left',
        field: (row: FileTemplate) => row.creatorUserId,
        sortable: true,
      },
      {
        name: 'fileFormat',
        required: true,
        label: 'Format',
        align: 'left',
        field: (row: FileTemplate) => row.fileFormat,
        sortable: true,
      },
    ];
    /**
     * Route to Template page
     * templateId
     *  - if given then will be routed with 'id' parameter to edit page
     *  - if undefined will be routed without 'id' parameter to create page
     */
    const routeToTemplatePage = async (templateId?: number, copy?: boolean) => {
      const routeParams = templateId === undefined ? undefined : { id: templateId };
      
      if (templateId === undefined) {
        await router.push({
          name: 'template/create',
          params: routeParams,
          query: { action: 'create' },
        });
      } else if (copy) {
        await router.push({
          name: 'template/copy',
          params: routeParams,
          query: { action: 'copy' },
        });
      } else {
        await router.push({
          name: 'template/edit',
          params: routeParams,
          query: { action: 'edit' },
        });
      }
    }
    const exportTable = () => {
      // naive encoding to csv format
      q.notify({
        color: 'positive',
        position: 'bottom-right',
        message: 'Exporting table data',
        icon: 'report_info',
      });
    }

    const  { fileTemplates, deleteTemplate } = useFileTemplateAPI();

    return { columns, fileTemplates, deleteTemplate, routeToTemplatePage, exportTable, filter };
  },
});
</script>
