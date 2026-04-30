<script setup lang="ts">
import { reactive, ref } from 'vue'
import { getCurrentUser, login } from '../lib/http'
import { navigate, useRoute } from '../lib/router'
import { setToken, setUser } from '../lib/session'

const route = useRoute()
const form = reactive({
  userName: '',
  password: '',
})
const error = ref('')
const pending = ref(false)

async function submit() {
  error.value = ''
  pending.value = true

  try {
    const token = await login(form)
    setToken(token)
    const user = await getCurrentUser()
    setUser(user)
    const redirect = new URLSearchParams(route.search).get('redirect') || '/dashboard'
    navigate(redirect)
  } catch (err) {
    error.value = err instanceof Error ? err.message : '登录失败'
  } finally {
    pending.value = false
  }
}
</script>

<template>
  <section class="page-section">
    <div class="page-section__intro">
      <p class="eyebrow">Sign In</p>
      <h1>登录 ColaCode，继续你的题库、练习和训练记录。</h1>
      <p>
        当前登录页直接对接 `/auth/user/login` 和 `/auth/user/info`，不是前端本地假登录。
      </p>
    </div>

    <form class="auth-card" @submit.prevent="submit">
      <label>
        <span>用户名</span>
        <input v-model.trim="form.userName" type="text" placeholder="请输入用户名" />
      </label>

      <label>
        <span>密码</span>
        <input v-model="form.password" type="password" placeholder="请输入密码" />
      </label>

      <p v-if="error" class="inline-error">{{ error }}</p>

      <button class="button button--primary button--block" type="submit" :disabled="pending">
        {{ pending ? '登录中...' : '登录账号' }}
      </button>

      <button class="button button--ghost button--block" type="button" @click="navigate('/register')">
        还没有账号？去注册
      </button>
    </form>
  </section>
</template>
