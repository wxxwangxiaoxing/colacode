<script setup lang="ts">
import { reactive, ref } from 'vue'
import { register } from '../lib/http'
import { navigate } from '../lib/router'

const form = reactive({
  userName: '',
  nickName: '',
  email: '',
  phone: '',
  password: '',
  introduce: '',
})

const pending = ref(false)
const error = ref('')
const success = ref('')

async function submit() {
  pending.value = true
  error.value = ''
  success.value = ''

  try {
    await register({
      ...form,
      sex: 0,
      status: 0,
    })
    success.value = '注册成功，可以直接登录继续使用。'
  } catch (err) {
    error.value = err instanceof Error ? err.message : '注册失败'
  } finally {
    pending.value = false
  }
}
</script>

<template>
  <section class="page-section">
    <div class="page-section__intro">
      <p class="eyebrow">Register</p>
      <h1>先创建一个真实账号，后面的练习记录才能落到你的个人面板里。</h1>
      <p>
        当前表单会直接提交到 `/auth/user/register`，注册成功后就能回到登录页继续训练。
      </p>
    </div>

    <form class="auth-card auth-card--wide" @submit.prevent="submit">
      <div class="form-grid">
        <label>
          <span>用户名</span>
          <input v-model.trim="form.userName" type="text" placeholder="6-32 个字符更稳妥" />
        </label>
        <label>
          <span>昵称</span>
          <input v-model.trim="form.nickName" type="text" placeholder="用于站内展示" />
        </label>
        <label>
          <span>邮箱</span>
          <input v-model.trim="form.email" type="email" placeholder="邮箱地址" />
        </label>
        <label>
          <span>手机号</span>
          <input v-model.trim="form.phone" type="tel" placeholder="11 位手机号" />
        </label>
      </div>

      <label>
        <span>密码</span>
        <input v-model="form.password" type="password" placeholder="至少 6 位" />
      </label>

      <label>
        <span>个人简介</span>
        <textarea
          v-model.trim="form.introduce"
          rows="4"
          placeholder="比如：Java 后端 / 校招冲刺 / 想重点补分布式与项目表达"
        ></textarea>
      </label>

      <p v-if="error" class="inline-error">{{ error }}</p>
      <p v-if="success" class="inline-success">{{ success }}</p>

      <div class="auth-card__actions">
        <button class="button button--primary" type="submit" :disabled="pending">
          {{ pending ? '注册中...' : '创建账号' }}
        </button>
        <button class="button button--ghost" type="button" @click="navigate('/login')">
          已有账号，去登录
        </button>
      </div>
    </form>
  </section>
</template>
