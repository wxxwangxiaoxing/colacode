<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getContributeList, getPracticeSets } from '../lib/http'
import { navigate } from '../lib/router'
import type { PracticeSet, SubjectInfo } from '../types/api'

const contributions = ref<SubjectInfo[]>([])
const practiceSets = ref<PracticeSet[]>([])
const loading = ref(true)
const error = ref('')

const pillars = [
  {
    title: '结构化题库',
    text: '按分类、题型、难度拆开训练，不再依赖零散收藏和临时记忆。',
  },
  {
    title: '闭环练习',
    text: '从预设练习组到提交报告，整个做题过程都能沉淀为可复盘的数据。',
  },
  {
    title: '面试导向',
    text: '后续可以自然接入 AI 面试、面经和报告模块，形成完整求职流程。',
  },
]

const steps = [
  '先从题库里建立知识地图，知道自己到底在补哪一块。',
  '再用预设练习组做一轮集中训练，把正确率和耗时拉出来看。',
  '最后把训练记录沉淀到个人面板，后面再继续接 AI 面试模块。',
]

onMounted(async () => {
  loading.value = true
  error.value = ''

  try {
    const [contributeData, practiceData] = await Promise.all([
      getContributeList(6),
      getPracticeSets(1, 4),
    ])
    contributions.value = contributeData
    practiceSets.value = practiceData
  } catch (err) {
    error.value = err instanceof Error ? err.message : '首页数据加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="hero-panel hero-panel--refined">
    <div class="hero-copy">
      <p class="eyebrow">ColaCode Platform</p>
      <h1>把刷题、练习和求职准备，做成一套真正顺手的学习界面。</h1>
      <p class="hero-copy__body">
        这套前端不是展示模板，而是已经开始接真实接口的训练平台。首页负责表达产品气质，题库和练习页负责把后端能力转成可用体验。
      </p>

      <div class="hero-copy__actions">
        <button class="button button--primary" type="button" @click="navigate('/subjects')">
          浏览题库
        </button>
        <button class="button button--ghost" type="button" @click="navigate('/practice')">
          开始练习
        </button>
      </div>

      <div class="metric-row metric-row--hero">
        <article class="metric-card">
          <strong>{{ contributions.length || '--' }}</strong>
          <span>首页实时题目贡献</span>
        </article>
        <article class="metric-card">
          <strong>{{ practiceSets.length || '--' }}</strong>
          <span>可直接开始的预设练习</span>
        </article>
        <article class="metric-card">
          <strong>3</strong>
          <span>已打通的核心业务模块</span>
        </article>
      </div>
    </div>

    <div class="hero-stage hero-stage--refined">
      <article class="stage-card stage-card--primary">
        <p class="stage-card__tag">Training Console</p>
        <h2>不是一张静态首页，而是一套会继续长业务的前端。</h2>
        <ul>
          <li>首页强调产品感和训练节奏</li>
          <li>题库页处理真实筛选和分页</li>
          <li>练习页已经可以拉题并提交</li>
        </ul>
      </article>

      <article class="stage-card stage-card--accent">
        <p class="stage-card__tag">Connected APIs</p>
        <h2>Auth / Subject / Practice</h2>
        <p>现在看到的内容已经开始读取真实后端，不再是前端静态拼装。</p>
      </article>

      <article class="stage-card stage-card--mini">
        <span>Live</span>
        <strong>{{ loading ? '...' : contributions.length }}</strong>
        <small>题目动态</small>
      </article>
    </div>
  </section>

  <section class="section-block">
    <div class="section-heading">
      <p class="eyebrow">Product Direction</p>
      <h2>现在这套界面更像一个正在成长的学习产品，而不是临时拼出来的后台壳子。</h2>
      <p v-if="error" class="inline-error">{{ error }}</p>
      <p v-else>视觉上做减法，结构上做分层，让训练路径和核心模块一眼能看明白。</p>
    </div>

    <div class="feature-grid">
      <article v-for="pillar in pillars" :key="pillar.title" class="feature-card">
        <p class="feature-card__eyebrow">Core</p>
        <h3>{{ pillar.title }}</h3>
        <p>{{ pillar.text }}</p>
      </article>
    </div>
  </section>

  <section class="section-block section-block--split">
    <div class="section-heading">
      <p class="eyebrow">Live Content</p>
      <h2>首页直接读取两类实时数据：贡献题目和预设练习组。</h2>
      <p>
        这样首页不是概念展示，而是一个有动态内容、有后端来源、有行动入口的真实页面。
      </p>
    </div>

    <div class="stack">
      <article class="content-panel">
        <div class="panel-head">
          <h3>贡献题目</h3>
          <button class="text-link" type="button" @click="navigate('/subjects')">查看题库</button>
        </div>
        <div v-if="loading" class="panel-empty">正在加载题目动态...</div>
        <ul v-else-if="contributions.length" class="list-panel">
          <li v-for="item in contributions" :key="item.id">
            <strong>{{ item.subjectName }}</strong>
            <span>{{ item.createdBy || '题库贡献者' }}</span>
          </li>
        </ul>
        <div v-else class="panel-empty">暂无贡献题目数据</div>
      </article>

      <article class="content-panel">
        <div class="panel-head">
          <h3>预设练习</h3>
          <button class="text-link" type="button" @click="navigate('/practice')">进入练习</button>
        </div>
        <div v-if="loading" class="panel-empty">正在加载练习组...</div>
        <ul v-else-if="practiceSets.length" class="list-panel">
          <li v-for="set in practiceSets" :key="set.id">
            <strong>{{ set.setName }}</strong>
            <span>{{ set.description || '从这一组开始一轮完整训练。' }}</span>
          </li>
        </ul>
        <div v-else class="panel-empty">暂无预设练习组</div>
      </article>
    </div>
  </section>

  <section class="section-block section-block--split section-block--reverse">
    <div class="content-panel content-panel--editorial">
      <p class="eyebrow">Workflow</p>
      <h3>建议的本地训练路径</h3>
      <ol class="timeline-list">
        <li v-for="step in steps" :key="step">{{ step }}</li>
      </ol>
    </div>

    <div class="content-panel content-panel--spotlight">
      <p class="eyebrow">Next Move</p>
      <h3>现在最适合继续做的，是把题库和练习页打磨成真正好用的学习工具。</h3>
      <p>
        首页已经承担品牌和导航角色，接下来只要继续补题目详情、逐题练习和报告视图，这套前端就会越来越像产品。
      </p>
      <div class="hero-copy__actions">
        <button class="button button--primary" type="button" @click="navigate('/dashboard')">
          查看我的面板
        </button>
      </div>
    </div>
  </section>
</template>
