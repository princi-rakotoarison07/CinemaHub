<script setup>
import { useRoute } from 'vue-router'
import { computed } from 'vue'

const route = useRoute()

const isTestsRoute = computed(() => route.path.startsWith('/tests'))
</script>

<template>
  <aside id="sidebar" class="sidebar">
    <ul id="sidebar-nav" class="sidebar-nav">
      <li class="nav-item">
        <RouterLink
          :class="['nav-link', route.path === '/' ? '' : 'collapsed']"
          to="/"
        >
          <i class="bi bi-grid"></i>
          <span>Dashboard</span>
        </RouterLink>
      </li>

      <li class="nav-item">
        <a
          :class="['nav-link', isTestsRoute ? '' : 'collapsed']"
          data-bs-target="#tests-nav"
          data-bs-toggle="collapse"
          href="#"
        >
          <i class="bi bi-hdd-network"></i><span>Test</span
          ><i class="bi bi-chevron-down ms-auto"></i>
        </a>
        <ul
          id="tests-nav"
          :class="['nav-content', 'collapse', isTestsRoute ? 'show' : '']"
          data-bs-parent="#sidebar-nav"
        >
          <li>
            <RouterLink
              :class="[route.path === '/tests' ? 'active' : '']"
              to="/tests"
            >
              <i class="bi bi-circle"></i><span>Liste</span>
            </RouterLink>
          </li>
          <li>
            <RouterLink
              :class="[route.path === '/tests/new' ? 'active' : '']"
              to="/tests/new"
            >
              <i class="bi bi-circle"></i><span>Nouveau</span>
            </RouterLink>
          </li>
        </ul>
      </li>
    </ul>
  </aside>
</template>
