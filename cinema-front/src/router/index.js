import { createRouter, createWebHistory } from 'vue-router'

import MainLayoutDashboard from '../layouts/MainLayoutDashboard.vue'
import DashboardHome from '../views/DashboardHome.vue'
import TestList from '../views/tests/TestList.vue'
import TestForm from '../views/tests/TestForm.vue'
import SalleList from '../views/salles/SalleList.vue'
import SalleForm from '../views/salles/SalleForm.vue'
import FilmList from '../views/films/FilmList.vue'
import FilmForm from '../views/films/FilmForm.vue'
import SeanceList from '../views/seances/SeanceList.vue'
import SeanceForm from '../views/seances/SeanceForm.vue'
import TarifList from '../views/tarifs/TarifList.vue'
import TarifForm from '../views/tarifs/TarifForm.vue'
import PlaceList from '../views/places/PlaceList.vue'
import PlaceForm from '../views/places/PlaceForm.vue'
import TicketList from '../views/tickets/TicketList.vue'
import TicketForm from '../views/tickets/TicketForm.vue'
import ClientList from '../views/clients/ClientList.vue'
import ClientForm from '../views/clients/ClientForm.vue'
import ReservationList from '../views/reservations/ReservationList.vue'
import ReservationForm from '../views/reservations/ReservationForm.vue'

const router = createRouter({
  history: createWebHistory('/cinema-front/'),
  routes: [
    {
      path: '/',
      component: MainLayoutDashboard,
      children: [
        {
          path: '',
          name: 'dashboard-home',
          component: DashboardHome,
        },
        {
          path: 'tests',
          name: 'tests-list',
          component: TestList,
        },
        {
          path: 'tests/new',
          name: 'tests-new',
          component: TestForm,
        },
        {
          path: 'tests/:id/edit',
          name: 'tests-edit',
          component: TestForm,
          props: true,
        },
        {
          path: 'salles',
          name: 'salles-list',
          component: SalleList,
        },
        {
          path: 'salles/new',
          name: 'salles-new',
          component: SalleForm,
        },
        {
          path: 'salles/:id/edit',
          name: 'salles-edit',
          component: SalleForm,
          props: true,
        },
        {
          path: 'films',
          name: 'films-list',
          component: FilmList,
        },
        {
          path: 'films/new',
          name: 'films-new',
          component: FilmForm,
        },
        {
          path: 'seances',
          name: 'seances-list',
          component: SeanceList,
        },
        {
          path: 'seances/new',
          name: 'seances-new',
          component: SeanceForm,
        },
        {
          path: 'tarifs',
          name: 'tarifs-list',
          component: TarifList,
        },
        {
          path: 'tarifs/new',
          name: 'tarifs-new',
          component: TarifForm,
        },
        {
          path: 'places',
          name: 'places-list',
          component: PlaceList,
        },
        {
          path: 'places/new',
          name: 'places-new',
          component: PlaceForm,
        },
        {
          path: 'tickets',
          name: 'tickets-list',
          component: TicketList,
        },
        {
          path: 'tickets/new',
          name: 'tickets-new',
          component: TicketForm,
        },
        {
          path: 'clients',
          name: 'clients-list',
          component: ClientList,
        },
        {
          path: 'clients/new',
          name: 'clients-new',
          component: ClientForm,
        },
        {
          path: 'reservations',
          name: 'reservations-list',
          component: ReservationList,
        },
        {
          path: 'reservations/new',
          name: 'reservations-new',
          component: ReservationForm,
        },
      ],
    },
  ],
})

export default router
