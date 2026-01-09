import { createRouter, createWebHistory } from 'vue-router'

import MainLayoutDashboard from '../layouts/MainLayoutDashboard.vue'
import DashboardHome from '../views/DashboardHome.vue'
import TestList from '../views/tests/TestList.vue'
import TestForm from '../views/tests/TestForm.vue'
import SalleList from '../views/salles/SalleList.vue'
import SalleForm from '../views/salles/SalleForm.vue'

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
      ],
    },
  ],
})

export default router
