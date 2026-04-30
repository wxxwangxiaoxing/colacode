<script setup lang="ts">
import { computed } from 'vue'
import { logoutRequest } from '../lib/http'
import { navigate } from '../lib/router'
import { clearAuth, session } from '../lib/session'

defineProps<{
  currentPath: string
}>()

const navItems = [
  { label: '首页', to: '/' },
  { label: '题库', to: '/subjects' },
  { label: '练习', to: '/practice' },
  { label: '面板', to: '/dashboard' },
]

const displayName = computed(
  () => session.user?.nickName || session.user?.userName || '学习者',
)

function go(to: string) {
  navigate(to)
}

async function handleLogout() {
  try {
    await logoutRequest()
  } catch {
    // Ignore logout API failures locally and clear session anyway.
  }

  clearAuth()
  navigate('/')
}
</script>

<template>
  <header class="topbar">
    <button class="brand-button" type="button" @click="go('/')">
      <span class="brand-mark">
        <span class="brand-mark__badge">CC</span>
        <span class="brand-copy">
          <strong>ColaCode</strong>
          <small>Interview-first learning system</small>
        </span>
      </span>
    </button>

    <nav class="topbar__nav" aria-label="Main navigation">
      <a
        v-for="item in navItems"
        :key="item.to"
        :href="item.to"
        :class="{ 'is-active': currentPath === item.to }"
        @click.prevent="go(item.to)"
      >
        {{ item.label }}
      </a>
    </nav>

    <div class="topbar__actions">
      <template v-if="session.token">
        <div class="profile-chip">
          <span class="profile-chip__dot"></span>
          <div>
            <strong>{{ displayName }}</strong>
            <small>{{ session.user?.email || '已登录' }}</small>
          </div>
        </div>
        <button class="topbar__ghost" type="button" @click="handleLogout">退出</button>
      </template>
      <template v-else>
        <button class="topbar__ghost" type="button" @click="go('/login')">登录</button>
        <button class="topbar__cta" type="button" @click="go('/register')">创建账号</button>
      </template>
    </div>
  </header>
</template>
