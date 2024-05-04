<script setup>
import { GoogleMap, Marker, InfoWindow } from 'vue3-google-map';
// import { Loader } from '@googlemaps/js-api-loader';
// const config = useRuntimeConfig()

// const loader = new Loader({
//   apiKey: 'AIzaSyDabFN22vNeqQryTHEFXMRNvCW_yDG4PtA',//config.googleMapsApi, //process.env.GOOGLE_MAPS_API,
//   version: 'weekly',
//   libraries: ['places'],
// });

// const apiPromise = loader.load();

const center = { lat: 47.62, lng: 14.04 }
const markerOptions = { position: center, label: 'L', title: 'Land' }

</script>


<template>
  <GoogleMap
    api-key="AIzaSyDabFN22vNeqQryTHEFXMRNvCW_yDG4PtA"
    style="width: 90%; height: 1000px;"
    :center="center"
    :zoom="8.2"
  >
    <Marker :options="clickable=true" />
  </GoogleMap>
</template>

<style scoped>
.custom-btn {
  box-sizing: border-box;
  background: white;
  height: 40px;
  width: 40px;
  border-radius: 2px;
  border: 0px;
  margin: 10px;
  padding: 0px;
  font-size: 1.25rem;
  text-transform: none;
  appearance: none;
  cursor: pointer;
  user-select: none;
  box-shadow: rgba(0, 0, 0, 0.3) 0px 1px 4px -1px;
  overflow: hidden;
}
</style>