import { ref, onMounted, computed } from 'vue';
import { api } from 'boot/axios';
import { AxiosResponse } from 'axios';
import useBaseAPI from 'components/base-api';
import { FileTemplate } from 'components/models/file-template';

/**
 * File Template composition API for file template operations with backend REST API
 * @returns
 *  file template data synchronized with REST backend
 *  loadFileTemplate function to trigger refreshing of file template
 *  createOrUpdateFileTemplate function to trigger saving of file template
 */
export default function useFileTemplateAPI(fileTemplateId: number | undefined, copy?: boolean) {
  const { apiErrorHandler, apiOkHandler } = useBaseAPI();

  const fileTemplate = ref<FileTemplate>({
    id: fileTemplateId,
    templateName: '',
    templateTags: '',
    fileContentText: '',
    custom: true,
    guestAccess: false,
    canBeEdited: true,
  } as FileTemplate);

  const loadFileTemplate = async (): Promise<void> => {
    try {
      if (fileTemplate.value.id != undefined) {
        const response = await api.get<FileTemplate>(`filetemplate/${fileTemplate.value.id}`);
        fileTemplate.value = response.data;
        if (copy) {
          fileTemplate.value.id = undefined;
          fileTemplate.value.creatorUserId = '';
        }
      }
    } catch (error) {
      apiErrorHandler('Loading of file template failed', error);
    }
  };

  const fileTemplateTagsArray = computed<string[]>({
    get() {
      return fileTemplate.value?.templateTags?.split(',')?.filter(tagName => tagName.trim().length > 0)
    },
    set(value) {
      if (fileTemplate.value)
        fileTemplate.value.templateTags = value.join(',')
    },
  });

  const createOrUpdateFileTemplate = async ():Promise<boolean> => {
    if (fileTemplate.value.id === undefined) {
      try {
        const response = await api.post<FileTemplate, AxiosResponse<number>>(
          'filetemplate',
          fileTemplate.value
        );
        fileTemplate.value.id = response.data; //Store id of the file template
        console.log(`File template created id=${fileTemplate.value?.id}`);
        apiOkHandler('File template created');
        return true;
      } catch (error) {
        apiErrorHandler('File template creation failed', error);
        return false;
      }
    } else {
      try {
        await api.put<FileTemplate>(`filetemplate/${fileTemplate.value.id}`, fileTemplate.value);
        apiOkHandler('File template updated');
        return true;
      } catch (error) {
        apiErrorHandler('File template update failed', error);
        return false;
      }
    }
  };

  onMounted(async () => {
    await loadFileTemplate();
  });

  return {
    fileTemplate,
    fileTemplateTagsArray,
    loadFileTemplate,
    createOrUpdateFileTemplate,
  };
}
