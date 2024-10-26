import { ref, onMounted } from 'vue';
import { api } from 'boot/axios';
import useBaseAPI from 'components/base-api';
import { TraceEntry } from 'components/models/trace';

export default function useTracesAPI() {
  const { apiErrorHandler } = useBaseAPI();

  const traces = ref<readonly TraceEntry[]>([]);

  const loadTraces = async (): Promise<void> => {
    try {
      const response = await api.get<TraceEntry[]>('traces');
      traces.value = response.data;
    } catch (error) {
      apiErrorHandler('Loading of traces failed', error);
    }
  };

  onMounted(loadTraces);

  return {
    traces,
  };
}
