// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  devtools: { enabled: true },
  modules: ["@nuxt/ui"],
  plugins: [
    '~/plugins/myPlugins.ts',
    // '~/plugins/db.ts',
    // {src: '~/plugins/v-calender.ts', ssr: false},
  //   { src: '~/plugins/google-maps.ts'}
  ],
  runtimeConfig: {
    googleMapsApi: 'AIzaSyDabFN22vNeqQryTHEFXMRNvCW_yDG4PtA',
    apiSecret: '1234',
    public: {
      apiBase: '/api'
    }
  }
})