<script setup lang="ts">
// const links = [{
//   label: 'Nuxt UI',
//   to: 'https://ui.nuxt.com/'
// }, {
//   label: 'Nuxt Docs',
//   to: 'https://nuxt.com'
// }, {
//   label: 'Nuxt Studio',
//   to: 'https://nuxt.studio'
// }]
</script>

<template>
  <UContainer>
    <UCard class="mt-10">
      <template #footer>
        <div class="flex justify-between">
          <h1>Copyright &copy; 2024 Software Frameworks</h1>
          <div class="flex justify-between">
            <NuxtLink class="nLink" to="/about">About</NuxtLink>
            <span class="hSpacer"></span>
            <NuxtLink class="nLink" to="/contributer">Contributer</NuxtLink>
          </div>
          <!-- <ColorScheme><USelect v-model="$colorMode.preference" :options="['system', 'light', 'dark']" /></ColorScheme> -->
        </div>
      </template>
    </UCard>
  </UContainer>
  <!-- <footer class="bg-gray-800 text-white p-4">
    <div class="div1 container mx-auto text-right">
      <p>Copyright &copy; 2024 My Website</p>
    </div>
    <div class="div2">
      <NuxtLink class="nLink" to="/about">About</NuxtLink>
      
      <NuxtLink class="nLink" to="/contact">Contact</NuxtLink>
    </div>
  </footer> -->
</template>

<style>
.nLink {
  margin: 20px, 20px;
}
.hSpacer{
  width: 60px;
}
</style>