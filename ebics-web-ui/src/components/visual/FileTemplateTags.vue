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
  order: number;
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
        order: 5
      } as TagBadge,
      {
        name: 'SWIFT',
        iconName: 'attach_money',
        color: 'blue',
        order: 5
      } as TagBadge,
      {
        name: 'Payment',
        iconName: 'payment',
        color: 'red',
        order: 1
      } as TagBadge,
      {
        name: 'DirectDebit',
        iconName: 'pix',
        color: 'red',
        order: 1
      } as TagBadge,
      {
        name: 'QRR',
        iconName: 'local_mall',
        color: 'green',
        order: 5
      } as TagBadge,
      {
        name: 'SCOR',
        iconName: 'sell',
        color: 'green',
        order: 5
      } as TagBadge,
      {
        name: 'CH',
        iconName: 'add_box',
        color: 'orange',
        order: 2
      } as TagBadge,
      {
        name: 'SPS',
        iconName: 'add_box',
        color: 'orange',
        order: 3
      } as TagBadge,
      {
        name: 'DK',
        iconName: 'euro_symbol',
        color: 'orange',
        order: 3
      } as TagBadge,
      {
        name: 'EPC',
        iconName: 'euro_symbol',
        color: 'orange',
        order: 3
      } as TagBadge,
      {
        name: 'CGI',
        iconName: 'public',
        color: 'orange',
        order: 3
      } as TagBadge,
      {
        name: 'Domestic',
        iconName: 'home',
        color: 'orange',
        order: 5
      } as TagBadge,
      {
        name: 'Multiple-B-Level',
        iconName: 'dns',
        color: 'orange',
        order: 7,
      } as TagBadge,
      {
        name: 'Multiple-C-Level',
        iconName: 'list',
        color: 'orange',
        order: 7,
      } as TagBadge,
      {
        name: 'ISOv2009',
        iconName: 'group_work',
        color: 'green',
        order: 8
      } as TagBadge,
      {
        name: 'ISOv2019',
        iconName: 'group_work',
        color: 'blue',
        order: 8
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
        }).sort((tagBadgeA, tagBadgeB) => tagBadgeA.order - tagBadgeB.order)
    );
    return { tagBadges, allTagBadges, templateSubstitutionHelpers };
  },
});
</script>
