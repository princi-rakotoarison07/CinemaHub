<script setup>
import Header from '../components/layout/Header.vue'
import Sidebar from '../components/layout/Sidebar.vue'
import Footer from '../components/layout/Footer.vue'
import ToastContainer from '../components/common/ToastContainer.vue'
</script>

<template>
  <Header />
  <Sidebar />

  <ToastContainer />

  <main id="main" class="main">
    <router-view />
  </main>

  <Footer />

  <a href="#" class="back-to-top d-flex align-items-center justify-content-center">
    <i class="bi bi-arrow-up-short"></i>
  </a>
</template>
