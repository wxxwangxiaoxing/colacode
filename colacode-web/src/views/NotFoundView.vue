<script setup lang="ts">
import { navigate } from '../lib/router'
</script>

<template>
  <section class="page-section">
    <div class="content-panel panel-empty panel-empty--tall">
      <p class="eyebrow">404</p>
      <h1>这个页面还没有被挂到 ColaCode 的前端路由里。</h1>
      <button class="button button--primary" type="button" @click="navigate('/')">返回首页</button>
    </div>
  </section>
</template>
