import { ref, onMounted } from 'vue';
import { api } from 'boot/axios';
import useBaseAPI from './base-api';
import { FileTemplate } from './models/file-template';

export default function useFileTemplateAPI() {
  const { apiErrorHandler } = useBaseAPI();

  const fileTemplates = ref<FileTemplate[]>();

  const loadFileTemplates = async (): Promise<void> => {
    try {
      const response = await api.get<FileTemplate[]>('filetemplate');
      fileTemplates.value = response.data;
    } catch (error) {
      apiErrorHandler('Loading of file templates failed', error);
    }
  };

  onMounted(loadFileTemplates);

  return {
    fileTemplates,
  };
}
