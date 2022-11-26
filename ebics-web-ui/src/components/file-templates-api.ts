import { ref, onMounted, computed } from 'vue';
import { api } from 'boot/axios';
import useBaseAPI from './base-api';
import { FileTemplate } from 'components/models/file-template';
import useDialogs from 'components/dialogs';

export default function useFileTemplateAPI() {
  const { apiErrorHandler } = useBaseAPI();
  const { confirmDialog } = useDialogs();

  const fileTemplates = ref<FileTemplate[]>();

  const loadFileTemplates = async (): Promise<void> => {
    try {
      const response = await api.get<FileTemplate[]>('filetemplate');
      fileTemplates.value = response.data;
    } catch (error) {
      apiErrorHandler('Loading of file templates failed', error);
    }
  };

  const deleteTemplate = async (
    templateId: number,
    templateName: string,
    askForConfimation = true
  ): Promise<void> => {
    try {
      const canDelete = askForConfimation
        ? await confirmDialog(
            'Confirm deletion',
            `Do you want to realy delete template: '${templateName}'`
          )
        : true;
      if (canDelete) {
        await api.delete<FileTemplate>(`filetemplate/${templateId}`);
        await loadFileTemplates();
      }
    } catch (error) {
      apiErrorHandler('Deleting of template failed', error);
    }
  };

  const allFileTemplateTagsArray = computed(() => {
    const nonUniqueTags = fileTemplates.value?.flatMap((t) =>
      t.templateTags.split(',')
    );
    return [...new Set(nonUniqueTags)];
  });

  onMounted(loadFileTemplates);

  return {
    fileTemplates, deleteTemplate, allFileTemplateTagsArray
  };
}
