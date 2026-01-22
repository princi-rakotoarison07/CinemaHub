<script setup>
import { useRoute } from 'vue-router'
import { computed } from 'vue'

const route = useRoute()

const isTestsRoute = computed(() => route.path.startsWith('/tests'))
const isSallesRoute = computed(() => route.path.startsWith('/salles'))
const isFilmsRoute = computed(() => route.path.startsWith('/films'))
const isTarifsRoute = computed(() => route.path.startsWith('/tarifs'))
const isConfigurationTarifsRoute = computed(() => route.path.startsWith('/configuration-tarifs'))
const isPlacesRoute = computed(() => route.path.startsWith('/places'))
const isTicketsRoute = computed(() => route.path.startsWith('/tickets'))

const isSeancesRoute = computed(() => route.path.startsWith('/seances'))
const isClientsRoute = computed(() => route.path.startsWith('/clients'))
const isReservationsRoute = computed(() => route.path.startsWith('/reservations'))
const isPublicitesRoute = computed(() => route.path.startsWith('/publicites'))
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
          :class="['nav-link', isSallesRoute ? '' : 'collapsed']"
          data-bs-target="#salles-nav"
          data-bs-toggle="collapse"
          href="#"
        >
          <i class="bi bi-building"></i><span>Salles</span
          ><i class="bi bi-chevron-down ms-auto"></i>
        </a>
        <ul
          id="salles-nav"
          :class="['nav-content', 'collapse', isSallesRoute ? 'show' : '']"
          data-bs-parent="#sidebar-nav"
        >
          <li>
            <RouterLink :class="[route.path === '/salles' ? 'active' : '']" to="/salles">
              <i class="bi bi-circle"></i><span>Liste</span>
            </RouterLink>
          </li>
          <li>
            <RouterLink :class="[route.path === '/salles/new' ? 'active' : '']" to="/salles/new">
              <i class="bi bi-circle"></i><span>Nouveau</span>
            </RouterLink>
          </li>
        </ul>
      </li>

      <li class="nav-item">
        <a
          :class="['nav-link', isTarifsRoute ? '' : 'collapsed']"
          data-bs-target="#tarifs-nav"
          data-bs-toggle="collapse"
          href="#"
        >
          <i class="bi bi-cash-stack"></i><span>Tarifs</span
          ><i class="bi bi-chevron-down ms-auto"></i>
        </a>
        <ul
          id="tarifs-nav"
          :class="['nav-content', 'collapse', isTarifsRoute ? 'show' : '']"
          data-bs-parent="#sidebar-nav"
        >
          <li>
            <RouterLink :class="[route.path === '/tarifs' ? 'active' : '']" to="/tarifs">
              <i class="bi bi-circle"></i><span>Liste</span>
            </RouterLink>
          </li>
          <li>
            <RouterLink
              :class="[route.path === '/tarifs/grille' ? 'active' : '']"
              to="/tarifs/grille"
            >
              <i class="bi bi-circle"></i><span>Grille</span>
            </RouterLink>
          </li>
          <li>
            <RouterLink :class="[route.path === '/tarifs/new' ? 'active' : '']" to="/tarifs/new">
              <i class="bi bi-circle"></i><span>Nouveau</span>
            </RouterLink>
          </li>
        </ul>
      </li>

      <li class="nav-item">
        <a
          :class="['nav-link', isConfigurationTarifsRoute ? '' : 'collapsed']"
          data-bs-target="#configuration-tarifs-nav"
          data-bs-toggle="collapse"
          href="#"
        >
          <i class="bi bi-sliders"></i><span>Configuration tarifs</span
          ><i class="bi bi-chevron-down ms-auto"></i>
        </a>
        <ul
          id="configuration-tarifs-nav"
          :class="['nav-content', 'collapse', isConfigurationTarifsRoute ? 'show' : '']"
          data-bs-parent="#sidebar-nav"
        >
          <li>
            <RouterLink
              :class="[route.path === '/configuration-tarifs/grille' ? 'active' : '']"
              to="/configuration-tarifs/grille"
            >
              <i class="bi bi-circle"></i><span>Grille</span>
            </RouterLink>
          </li>
          <li>
            <RouterLink
              :class="[route.path === '/configuration-tarifs/new' ? 'active' : '']"
              to="/configuration-tarifs/new"
            >
              <i class="bi bi-circle"></i><span>Nouveau</span>
            </RouterLink>
          </li>
        </ul>
      </li>

      <li class="nav-item">
        <a
          :class="['nav-link', isPlacesRoute ? '' : 'collapsed']"
          data-bs-target="#places-nav"
          data-bs-toggle="collapse"
          href="#"
        >
          <i class="bi bi-grid-3x3-gap"></i><span>Places</span
          ><i class="bi bi-chevron-down ms-auto"></i>
        </a>
        <ul
          id="places-nav"
          :class="['nav-content', 'collapse', isPlacesRoute ? 'show' : '']"
          data-bs-parent="#sidebar-nav"
        >
          <li>
            <RouterLink :class="[route.path === '/places' ? 'active' : '']" to="/places">
              <i class="bi bi-circle"></i><span>Liste</span>
            </RouterLink>
          </li>
          <li>
            <RouterLink
              :class="[route.path === '/places/gestion' ? 'active' : '']"
              to="/places/gestion"
            >
              <i class="bi bi-circle"></i><span>Gestion</span>
            </RouterLink>
          </li>
          <li>
            <RouterLink :class="[route.path === '/places/new' ? 'active' : '']" to="/places/new">
              <i class="bi bi-circle"></i><span>Nouveau</span>
            </RouterLink>
          </li>
        </ul>
      </li>

      <li class="nav-item">
        <a
          :class="['nav-link', isTicketsRoute ? '' : 'collapsed']"
          data-bs-target="#tickets-nav"
          data-bs-toggle="collapse"
          href="#"
        >
          <i class="bi bi-ticket-perforated"></i><span>Tickets</span
          ><i class="bi bi-chevron-down ms-auto"></i>
        </a>
        <ul
          id="tickets-nav"
          :class="['nav-content', 'collapse', isTicketsRoute ? 'show' : '']"
          data-bs-parent="#sidebar-nav"
        >
          <li>
            <RouterLink :class="[route.path === '/tickets' ? 'active' : '']" to="/tickets">
              <i class="bi bi-circle"></i><span>Liste</span>
            </RouterLink>
          </li>
          <li>
            <RouterLink
              :class="[route.path === '/tickets/grille' ? 'active' : '']"
              to="/tickets/grille"
            >
              <i class="bi bi-circle"></i><span>Grille</span>
            </RouterLink>
          </li>
          <li>
            <RouterLink :class="[route.path === '/tickets/new' ? 'active' : '']" to="/tickets/new">
              <i class="bi bi-circle"></i><span>Nouveau</span>
            </RouterLink>
          </li>
        </ul>
      </li>

      <li class="nav-item">
        <a
          :class="['nav-link', isFilmsRoute ? '' : 'collapsed']"
          data-bs-target="#films-nav"
          data-bs-toggle="collapse"
          href="#"
        >
          <i class="bi bi-film"></i><span>Films</span
          ><i class="bi bi-chevron-down ms-auto"></i>
        </a>
        <ul
          id="films-nav"
          :class="['nav-content', 'collapse', isFilmsRoute ? 'show' : '']"
          data-bs-parent="#sidebar-nav"
        >
          <li>
            <RouterLink :class="[route.path === '/films' ? 'active' : '']" to="/films">
              <i class="bi bi-circle"></i><span>Liste</span>
            </RouterLink>
          </li>
          <li>
            <RouterLink :class="[route.path === '/films/new' ? 'active' : '']" to="/films/new">
              <i class="bi bi-circle"></i><span>Nouveau</span>
            </RouterLink>
          </li>
        </ul>
      </li>

      <li class="nav-item">
        <a
          :class="['nav-link', isClientsRoute ? '' : 'collapsed']"
          data-bs-target="#clients-nav"
          data-bs-toggle="collapse"
          href="#"
        >
          <i class="bi bi-people"></i><span>Clients</span
          ><i class="bi bi-chevron-down ms-auto"></i>
        </a>
        <ul
          id="clients-nav"
          :class="['nav-content', 'collapse', isClientsRoute ? 'show' : '']"
          data-bs-parent="#sidebar-nav"
        >
          <li>
            <RouterLink :class="[route.path === '/clients' ? 'active' : '']" to="/clients">
              <i class="bi bi-circle"></i><span>Liste</span>
            </RouterLink>
          </li>
          <li>
            <RouterLink :class="[route.path === '/clients/new' ? 'active' : '']" to="/clients/new">
              <i class="bi bi-circle"></i><span>Nouveau</span>
            </RouterLink>
          </li>
        </ul>
      </li>

      <li class="nav-item">
        <a
          :class="['nav-link', isSeancesRoute ? '' : 'collapsed']"
          data-bs-target="#seances-nav"
          data-bs-toggle="collapse"
          href="#"
        >
          <i class="bi bi-calendar-event"></i><span>Séances</span
          ><i class="bi bi-chevron-down ms-auto"></i>
        </a>
        <ul
          id="seances-nav"
          :class="['nav-content', 'collapse', isSeancesRoute ? 'show' : '']"
          data-bs-parent="#sidebar-nav"
        >
          <li>
            <RouterLink :class="[route.path === '/seances' ? 'active' : '']" to="/seances">
              <i class="bi bi-circle"></i><span>Liste</span>
            </RouterLink>
          </li>
          <li>
            <RouterLink :class="[route.path === '/seances/new' ? 'active' : '']" to="/seances/new">
              <i class="bi bi-circle"></i><span>Nouveau</span>
            </RouterLink>
          </li>
        </ul>
      </li>

      <li class="nav-item">
        <a
          :class="['nav-link', isReservationsRoute ? '' : 'collapsed']"
          data-bs-target="#reservations-nav"
          data-bs-toggle="collapse"
          href="#"
        >
          <i class="bi bi-ticket-perforated"></i><span>Réservation</span
          ><i class="bi bi-chevron-down ms-auto"></i>
        </a>
        <ul
          id="reservations-nav"
          :class="['nav-content', 'collapse', isReservationsRoute ? 'show' : '']"
          data-bs-parent="#sidebar-nav"
        >
          <li>
            <RouterLink :class="[route.path === '/reservations' ? 'active' : '']" to="/reservations">
              <i class="bi bi-circle"></i><span>Liste</span>
            </RouterLink>
          </li>
          <li>
            <RouterLink
              :class="[route.path === '/reservations/new' ? 'active' : '']"
              to="/reservations/new"
            >
              <i class="bi bi-circle"></i><span>Nouveau</span>
            </RouterLink>
          </li>
        </ul>
      </li>

      <li class="nav-item">
        <a
          :class="['nav-link', isPublicitesRoute ? '' : 'collapsed']"
          data-bs-target="#publicites-nav"
          data-bs-toggle="collapse"
          href="#"
        >
          <i class="bi bi-badge-ad"></i><span>Publicité</span
          ><i class="bi bi-chevron-down ms-auto"></i>
        </a>
        <ul
          id="publicites-nav"
          :class="['nav-content', 'collapse', isPublicitesRoute ? 'show' : '']"
          data-bs-parent="#sidebar-nav"
        >
          <li>
            <RouterLink
              :class="[route.path === '/publicites/contrats/new' ? 'active' : '']"
              to="/publicites/contrats/new"
            >
              <i class="bi bi-circle"></i><span>Nouveau contrat</span>
            </RouterLink>
          </li>
        </ul>
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
