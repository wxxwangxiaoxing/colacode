<script setup lang="ts">
import { computed, onMounted, watchEffect } from 'vue'
import AppFooter from './components/AppFooter.vue'
import AppHeader from './components/AppHeader.vue'
import { getCurrentUser } from './lib/http'
import { useRoute } from './lib/router'
import { clearAuth, session, setUser } from './lib/session'
import DashboardView from './views/DashboardView.vue'
import HomeView from './views/HomeView.vue'
import LoginView from './views/LoginView.vue'
import NotFoundView from './views/NotFoundView.vue'
import PracticeView from './views/PracticeView.vue'
import RegisterView from './views/RegisterView.vue'
import SubjectsView from './views/SubjectsView.vue'

const route = useRoute()

const pages = {
  '/': { title: 'ColaCode | 首页', component: HomeView },
  '/login': { title: 'ColaCode | 登录', component: LoginView },
  '/register': { title: 'ColaCode | 注册', component: RegisterView },
  '/subjects': { title: 'ColaCode | 题库', component: SubjectsView },
  '/practice': { title: 'ColaCode | 练习', component: PracticeView },
  '/dashboard': { title: 'ColaCode | 面板', component: DashboardView },
} as const

const currentPage = computed(
  () =>
    pages[route.path as keyof typeof pages] || {
      title: 'ColaCode | 404',
      component: NotFoundView,
    },
)

watchEffect(() => {
  document.title = currentPage.value.title
})

onMounted(async () => {
  if (!session.token) {
    return
  }

  try {
    setUser(await getCurrentUser())
  } catch {
    clearAuth()
  }
})
</script>

<template>
  <div class="app-shell">
    <div class="app-shell__glow app-shell__glow--left"></div>
    <div class="app-shell__glow app-shell__glow--right"></div>

    <div class="page-shell">
      <AppHeader :current-path="route.path" />
      <main class="page-main">
        <component :is="currentPage.component" />
      </main>
      <AppFooter />
    </div>
  </div>
</template>
