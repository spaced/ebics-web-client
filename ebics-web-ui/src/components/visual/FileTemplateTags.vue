<template>
  <div class="q-pa-sm q-gutter-sm">
    <q-badge v-for="tag in tagBadges" :key="tag.name" :color="tag.color" outline >
      {{ tag.name }}
      <q-icon :name="tag.iconName" />
    </q-badge>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed, ref } from 'vue';

interface TagBadge {
  name: string;
  iconName: string;
  color: string;
}

interface TemplateSubstitutionHelper {
  name: string,
  description: string,
  example: string,
}

export default defineComponent({
  name: 'TemplateTags',
  props: {
    tags: {
      type: String,
      required: true,
    },
  },
  setup(props) {
    const allTagBadges = ref<TagBadge[]>([
      {
        name: 'SEPA',
        iconName: 'euro',
        color: 'blue',
      } as TagBadge,
      {
        name: 'SWIFT',
        iconName: 'attach_money',
        color: 'blue',
      } as TagBadge,
      {
        name: 'Payment',
        iconName: 'payment',
        color: 'red',
      } as TagBadge,
      {
        name: 'DirectDebit',
        iconName: 'pix',
        color: 'red',
      } as TagBadge,
      {
        name: 'QRR',
        iconName: 'local_mall',
        color: 'green',
      } as TagBadge,
      {
        name: 'SCOR',
        iconName: 'sell',
        color: 'green',
      } as TagBadge,
      {
        name: 'CH',
        iconName: 'add_box',
        color: 'orange',
      } as TagBadge,
      {
        name: 'SPS',
        iconName: 'add_box',
        color: 'orange',
      } as TagBadge,
      {
        name: 'DK',
        iconName: 'euro_symbol',
        color: 'orange',
      } as TagBadge,
      {
        name: 'EPC',
        iconName: 'euro_symbol',
        color: 'orange',
      } as TagBadge,
      {
        name: 'CGI',
        iconName: 'public',
        color: 'orange',
      } as TagBadge,
    ]);

    const templateSubstitutionHelpers = ref<TemplateSubstitutionHelper[]>([
      {
        name: '%%IsoDt(+/-days)%%',
        description: 'Print ISO date in format dd-MM-yyyy',
        example: '%%IsoDt+3%%'
      } as TemplateSubstitutionHelper,
    ]);
    const tagBadges = computed<TagBadge[]>(() =>
      props.tags
        ?.split(',')
        .filter((tagName) => tagName.trim().length > 0)
        .map((tagName) => {
          const tagBadge = allTagBadges.value.find(
            (badge) => badge.name.toUpperCase() == tagName.toUpperCase()
          );
          if (tagBadge) {
            return tagBadge;
          } else {
            return {
              name: tagName,
              iconName: 'tag',
              color: 'grey',
            } as TagBadge;
          }
        })
    );
    return { tagBadges, allTagBadges, templateSubstitutionHelpers };
  },
});
</script>
