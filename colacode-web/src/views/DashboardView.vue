<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getCurrentUser, getPracticeHistory } from '../lib/http'
import { navigate } from '../lib/router'
import { session, setUser } from '../lib/session'
import type { PracticeInfo } from '../types/api'

const loading = ref(false)
const history = ref<PracticeInfo[]>([])

const totalScore = computed(() =>
  history.value.reduce((sum, item) => sum + (item.totalScore ?? 0), 0),
)

async function loadDashboard() {
  if (!session.token) {
    return
  }

  loading.value = true
  try {
    const [user, records] = await Promise.all([getCurrentUser(), getPracticeHistory()])
    setUser(user)
    history.value = records
  } finally {
    loading.value = false
  }
}

onMounted(loadDashboard)
</script>

<template>
  <section class="page-section">
    <div class="page-section__intro">
      <p class="eyebrow">Dashboard</p>
      <h1>把当前登录用户、练习记录和累计结果放进一个真正能看的个人面板里。</h1>
    </div>

    <div v-if="!session.token" class="content-panel panel-empty panel-empty--tall">
      <p>你还没有登录，所以这里不会伪造任何数据。</p>
      <button class="button button--primary" type="button" @click="navigate('/login?redirect=/dashboard')">
        去登录
      </button>
    </div>

    <div v-else class="dashboard-grid">
      <article class="content-panel">
        <div class="panel-head">
          <h3>用户资料</h3>
          <span class="panel-hint">{{ loading ? '同步中' : '实时读取' }}</span>
        </div>
        <div class="profile-summary">
          <strong>{{ session.user?.nickName || session.user?.userName }}</strong>
          <span>{{ session.user?.email || '未填写邮箱' }}</span>
          <p>{{ session.user?.introduce || '这个账号还没有补充个人简介。' }}</p>
        </div>
      </article>

      <article class="content-panel">
        <div class="panel-head">
          <h3>训练统计</h3>
          <span class="panel-hint">{{ history.length }} 条记录</span>
        </div>
        <div class="report-metrics">
          <div>
            <strong>{{ history.length }}</strong>
            <span>练习次数</span>
          </div>
          <div>
            <strong>{{ totalScore }}</strong>
            <span>累计得分</span>
          </div>
          <div>
            <strong>{{ history[0]?.correctCount ?? 0 }}</strong>
            <span>最近正确数</span>
          </div>
        </div>
      </article>

      <article class="content-panel content-panel--full">
        <div class="panel-head">
          <h3>最近练习</h3>
          <button class="text-link" type="button" @click="navigate('/practice')">继续练习</button>
        </div>
        <ul v-if="history.length" class="list-panel">
          <li v-for="item in history" :key="item.id">
            <strong>{{ item.setName || `练习 ${item.id}` }}</strong>
            <span>{{ item.submitTime || '暂无提交时间' }}</span>
          </li>
        </ul>
        <div v-else class="panel-empty">你还没有产生练习记录。</div>
      </article>
    </div>
  </section>
</template>
