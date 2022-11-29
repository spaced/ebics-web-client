<template>
  <div>
    <q-item-label caption v-if="ebicsApiErrorVal">
      Last API error: {{ formatedErrorMessage }} (HTTP {{ebicsApiErrorVal.httpStatusCode}} {{ ebicsApiErrorVal.message }})
    </q-item-label>
    <q-item-label caption v-else>
      Last error: {{ formatedErrorMessage }} ({{ apiError.message }})
    </q-item-label>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue';
import { ApiError, EbicsApiError } from 'components/models/ebics-api-error';
import { isEbicsApiError, getFormatedErrorMessage } from 'components/base-api'

export default defineComponent({
  name: 'ErrorMessageLabel',
  props: {
    apiError: {
      type: Object,
      required: true,
    },
  },
  emits: ['update:apiError'],
  setup(props, { emit }) {

    const apiErrorVal = computed<ApiError>({
      get() {
        return props.apiError as ApiError;
      },
      set(value) {
        emit('update:apiError', value);
      },
    });

    const ebicsApiErrorVal = computed<EbicsApiError | undefined>(() => {
      if (isEbicsApiError(apiErrorVal.value))
        return apiErrorVal.value;
      else
        return undefined;
    })

    const formatedErrorMessage = computed<string>(() => {
      console.log('Api error :' + JSON.stringify(apiErrorVal));
      return getFormatedErrorMessage(apiErrorVal.value);
    })

    return {apiErrorVal, ebicsApiErrorVal, formatedErrorMessage};
  }
});
</script>
