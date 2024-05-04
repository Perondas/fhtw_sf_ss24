<script setup lang="ts">
const route = useRoute()

const links = [{
  label: 'Home',
  to: `/`
}, {
  label: 'Map',
  to: '/map'
}, {
  label: 'Compare',
  to: '/compare'
}, {
  label: 'Meteo Scraper',
  to: '/meteoScraper'
}, {
  label: 'Geosphere',
  to: '/geosphere'
}, {
  label: 'Open Weather',
  to: '/openWeather'
}]
</script>

<template>
  <UHorizontalNavigation :links="links" class="border-b border-gray-200 dark:border-gray-800" >
    <!-- <template #default="{ link }">
      <span class="group-hover:text-primary relative">{{ link.label }}</span>
    </template> -->
  </UHorizontalNavigation>
</template>