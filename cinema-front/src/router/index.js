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
import SeanceChiffreAffaire from '../views/seances/SeanceChiffreAffaire.vue'
import TarifList from '../views/tarifs/TarifList.vue'
import TarifForm from '../views/tarifs/TarifForm.vue'
import TarifGrid from '../views/tarifs/TarifGrid.vue'
import ConfigurationTarifForm from '../views/configurationTarifs/ConfigurationTarifForm.vue'
import ConfigurationTarifGrid from '../views/configurationTarifs/ConfigurationTarifGrid.vue'
import PlaceList from '../views/places/PlaceList.vue'
import PlaceForm from '../views/places/PlaceForm.vue'
import PlaceGestion from '../views/places/PlaceGestion.vue'
import TicketList from '../views/tickets/TicketList.vue'
import TicketForm from '../views/tickets/TicketForm.vue'
import TicketGrid from '../views/tickets/TicketGrid.vue'
import ClientList from '../views/clients/ClientList.vue'
import ClientForm from '../views/clients/ClientForm.vue'
import ReservationList from '../views/reservations/ReservationList.vue'
import ReservationForm from '../views/reservations/ReservationForm.vue'
import ReservationEdit from '../views/reservations/ReservationEdit.vue'
import ReservationSimulation from '../views/reservations/ReservationSimulation.vue'
import ContratPubliciteList from '../views/publicites/ContratPubliciteList.vue'
import ContratPubliciteForm from '../views/publicites/ContratPubliciteForm.vue'
import PaiementContratPubliciteByContrat from '../views/publicites/PaiementContratPubliciteByContrat.vue'
import ChiffreAffairePublicite from '../views/publicites/ChiffreAffairePublicite.vue'
import SocieteGrid from '../views/publicites/config/SocieteGrid.vue'
import VideoPublicitaireGrid from '../views/publicites/config/VideoPublicitaireGrid.vue'
import TarifPubliciteGrid from '../views/publicites/config/TarifPubliciteGrid.vue'

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
          path: 'seances/chiffres-affaire',
          name: 'seances-chiffres-affaire',
          component: SeanceChiffreAffaire,
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
          path: 'tarifs/grille',
          name: 'tarifs-grille',
          component: TarifGrid,
        },
        {
          path: 'configuration-tarifs/new',
          name: 'configuration-tarifs-new',
          component: ConfigurationTarifForm,
        },
        {
          path: 'configuration-tarifs/grille',
          name: 'configuration-tarifs-grille',
          component: ConfigurationTarifGrid,
        },
        {
          path: 'places',
          name: 'places-list',
          component: PlaceList,
        },
        {
          path: 'places/gestion',
          name: 'places-gestion',
          component: PlaceGestion,
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
          path: 'tickets/grille',
          name: 'tickets-grille',
          component: TicketGrid,
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
        {
          path: 'reservations/:id/edit',
          name: 'reservations-edit',
          component: ReservationEdit,
          props: true,
        },
        {
          path: 'reservations/:id/simulation',
          name: 'reservations-simulation',
          component: ReservationSimulation,
          props: true,
        },
        {
          path: 'publicites/contrats',
          name: 'publicites-contrats-list',
          component: ContratPubliciteList,
        },
        {
          path: 'publicites/contrats/:id/payer',
          name: 'publicites-contrats-payer',
          component: PaiementContratPubliciteByContrat,
          props: true,
        },
        {
          path: 'publicites/contrats/new',
          name: 'publicites-contrats-new',
          component: ContratPubliciteForm,
        },
        {
          path: 'publicites/chiffres-affaire',
          name: 'publicites-chiffres-affaire',
          component: ChiffreAffairePublicite,
        },
        {
          path: 'publicites/config/societes',
          name: 'publicites-config-societes',
          component: SocieteGrid,
        },
        {
          path: 'publicites/config/videos-publicitaires',
          name: 'publicites-config-videos-publicitaires',
          component: VideoPublicitaireGrid,
        },
        {
          path: 'publicites/config/tarifs-publicite',
          name: 'publicites-config-tarifs-publicite',
          component: TarifPubliciteGrid,
        },
        // {
        //   path: 'advertising',
        //   name: 'advertising-manager',
        //   component: AdvertisingManager,
        // },
      ],
    },
  ],
})

export default router
