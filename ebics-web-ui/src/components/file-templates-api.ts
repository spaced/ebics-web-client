import { ref, onMounted, computed } from 'vue';
import { api } from 'boot/axios';
import useBaseAPI from './base-api';
import { FileTemplate } from 'components/models/file-template';
import useDialogs from 'components/dialogs';
import useUserSettingsAPI from 'components/user-settings';

export default function useFileTemplatesAPI() {
  const { apiErrorHandler } = useBaseAPI();
  const { confirmDialog } = useDialogs();
  
  //Used for filtering of displayedFileTemplates
  const { userSettings } = useUserSettingsAPI(); 

  const fileTemplates = ref<FileTemplate[]>();

  const displayedFileTemplates = computed<FileTemplate[] | undefined>(() => 
  {
    return fileTemplates.value?.filter(fileTemplate => {
      return (userSettings.value?.displayPredefinedTemplates || fileTemplate.custom) &&
             (userSettings.value?.displaySharedTemplates || !fileTemplate.guestAccess);
    });
  })

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

  const allUsedFileTemplateTagsArray = computed(() => {
    const nonUniqueTags = fileTemplates.value?.flatMap((t) =>
      t.templateTags.split(',')
    );
    return [...new Set(nonUniqueTags)];
  });

  const allPredefinedTags = ref<string[]>([
    'SEPA',
    'SWIFT',
    'Payment',
    'DirectDebit',
    'QRR',
    'SCOR',
    'CH',
    'SPS',
    'DK',
    'EPC',
    'CGI',
  ]);

  const allFileTemplateTagsArray = computed(() => {
    const allTags = allPredefinedTags.value.concat(allUsedFileTemplateTagsArray.value);
    return [...new Set(allTags)];
  });

  onMounted(loadFileTemplates);

  return {
    fileTemplates,
    deleteTemplate,
    allFileTemplateTagsArray,
    displayedFileTemplates,
  };
}