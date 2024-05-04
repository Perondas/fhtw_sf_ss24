<template>
    <div class="DefaultLayout text-white bg-slate-900 h-screen">
        <SmallHeader/>
        <slot />
        <Footer class="footer" />
    </div>
</template>

<style>
.footer {
    position: sticky;
    bottom: 0;
    width: 100%;
}
</style>