<template>
  <div>
    <q-item-label caption v-if="ebicsApiError">
      Last API error: {{ formatedErrorMessage }} (HTTP {{ebicsApiError.status}} {{ value.error }})
    </q-item-label>
    <q-item-label caption v-else>
      Last error: {{ formatedErrorMessage }} ({{ value.error }})
    </q-item-label>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { ApiError, EbicsApiError } from 'components/models/ebics-api-error';
import { isEbicsApiError, getFormatedErrorMessage } from 'components/base-api'

export default defineComponent({
  name: 'ErrorMessageLabel',
  props: {
    modelValue: {
      type: Object,
      required: true,
    },
  },
  emits: ['update:modelValue'],
  computed: {
    value: {
      get(): ApiError {
        return this.modelValue as ApiError;
      },
      set(value: ApiError) {
        this.$emit('update:modelValue', value);
      },
    },
    ebicsApiError(): EbicsApiError | undefined {
        if (isEbicsApiError(this.value))
          return this.value;
        else 
          return undefined;
    },
    formatedErrorMessage(): string {
      return getFormatedErrorMessage(this.value);
    }
  },
});
</script>
