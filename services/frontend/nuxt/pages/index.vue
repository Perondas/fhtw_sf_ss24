<script lang="ts" setup>
import { format } from "date-fns";
// import format from "date-fns/format";
// const format = require("date-fns/format");

const date = ref(new Date())
</script>

<template>
  <div>
    <h1>Home</h1>
    <ClientOnly>
      <UPopover :popper="{ placement: 'bottom-start' }">
        <UButton icon="i-heroicons-calendar-days-20-solid" :label="format(date, 'd MMM, yyy')" />

        <template #panel="{ close }">
          <DatePicker v-model="date" is-required @close="close" />
        </template>
      </UPopover>
    </ClientOnly> 
  </div>
</template>

<style>

</style>