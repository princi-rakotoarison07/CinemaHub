 <script setup>
 const baseUrl = import.meta.env.BASE_URL
 const toggleSidebar = () => {
   document.body.classList.toggle('toggle-sidebar')
 }
 </script>
 
<template>
  <header id="header" class="header fixed-top d-flex align-items-center">
    <div class="d-flex align-items-center justify-content-between">
      <a href="#" class="logo d-flex align-items-center">
        <img :src="`${baseUrl}assets/img/logo.png`" alt="" />
        <span class="d-none d-lg-block">CinemaHub</span>
      </a>
      <i class="bi bi-list toggle-sidebar-btn" @click="toggleSidebar"></i>
    </div>

    <nav class="header-nav ms-auto">
      <ul class="d-flex align-items-center"></ul>
    </nav>
  </header>
</template>
