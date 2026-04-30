<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getCategoryTree, getSubjectPage } from '../lib/http'
import type { SubjectCategory, SubjectInfo } from '../types/api'

const categories = ref<SubjectCategory[]>([])
const subjects = ref<SubjectInfo[]>([])
const loading = ref(false)
const error = ref('')
const selectedPrimaryId = ref<number | null>(null)
const selectedCategoryId = ref<number | null>(null)
const selectedType = ref<number | null>(null)
const pageNo = ref(1)
const pageSize = 9
const total = ref(0)

const typeOptions = [
  { label: '全部题型', value: null },
  { label: '单选题', value: 1 },
  { label: '多选题', value: 2 },
  { label: '判断题', value: 3 },
  { label: '简答题', value: 4 },
  { label: '编程题', value: 5 },
]

const currentPrimary = computed(
  () => categories.value.find((item) => item.id === selectedPrimaryId.value) ?? null,
)

const childCategories = computed(() => currentPrimary.value?.children ?? [])

function diffLabel(diff?: number) {
  const labels: Record<number, string> = {
    1: '入门',
    2: '基础',
    3: '进阶',
    4: '较难',
    5: '高压',
  }
  return labels[diff ?? 0] || '未分级'
}

function typeLabel(type?: number) {
  return typeOptions.find((item) => item.value === type)?.label || '题目'
}

async function loadCategories() {
  const tree = await getCategoryTree()
  categories.value = tree

  if (tree.length && selectedPrimaryId.value === null) {
    selectedPrimaryId.value = tree[0].id
    selectedCategoryId.value = tree[0].children?.[0]?.id ?? tree[0].id
  }
}

async function loadSubjects() {
  loading.value = true
  error.value = ''

  try {
    const data = await getSubjectPage({
      categoryId: selectedCategoryId.value,
      subjectType: selectedType.value,
      pageNo: pageNo.value,
      pageSize,
    })
    subjects.value = data.records
    total.value = data.total
  } catch (err) {
    error.value = err instanceof Error ? err.message : '题库加载失败'
  } finally {
    loading.value = false
  }
}

function choosePrimary(id: number) {
  selectedPrimaryId.value = id
  const primary = categories.value.find((item) => item.id === id)
  selectedCategoryId.value = primary?.children?.[0]?.id ?? primary?.id ?? null
  pageNo.value = 1
  loadSubjects()
}

function chooseChild(id: number | null) {
  selectedCategoryId.value = id
  pageNo.value = 1
  loadSubjects()
}

function chooseType(value: number | null) {
  selectedType.value = value
  pageNo.value = 1
  loadSubjects()
}

function nextPage() {
  if (pageNo.value * pageSize >= total.value) {
    return
  }
  pageNo.value += 1
  loadSubjects()
}

function prevPage() {
  if (pageNo.value <= 1) {
    return
  }
  pageNo.value -= 1
  loadSubjects()
}

onMounted(async () => {
  await loadCategories()
  await loadSubjects()
})
</script>

<template>
  <section class="page-section">
    <div class="page-section__intro">
      <p class="eyebrow">Question Bank</p>
      <h1>题库页已经开始使用真实分类树、分页和题型筛选接口。</h1>
      <p>
        当前页面直接对接 `/subject/category/queryCategoryAndLabel` 和 `/subject/info/page`，能做真实浏览，不是静态题目卡片。
      </p>
    </div>

    <div class="filter-shell">
      <div class="chip-row">
        <button
          v-for="item in categories"
          :key="item.id"
          class="chip"
          :class="{ 'is-selected': selectedPrimaryId === item.id }"
          type="button"
          @click="choosePrimary(item.id)"
        >
          {{ item.categoryName }}
        </button>
      </div>

      <div v-if="childCategories.length" class="chip-row chip-row--soft">
        <button
          v-for="child in childCategories"
          :key="child.id"
          class="chip chip--soft"
          :class="{ 'is-selected': selectedCategoryId === child.id }"
          type="button"
          @click="chooseChild(child.id)"
        >
          {{ child.categoryName }}
        </button>
      </div>

      <div class="chip-row chip-row--soft">
        <button
          v-for="item in typeOptions"
          :key="item.label"
          class="chip chip--soft"
          :class="{ 'is-selected': selectedType === item.value }"
          type="button"
          @click="chooseType(item.value)"
        >
          {{ item.label }}
        </button>
      </div>
    </div>

    <p v-if="error" class="inline-error">{{ error }}</p>

    <div v-if="loading" class="panel-empty">正在加载题库...</div>

    <div v-else class="subject-grid">
      <article v-for="subject in subjects" :key="subject.id" class="subject-card">
        <div class="subject-card__meta">
          <span>{{ typeLabel(subject.subjectType) }}</span>
          <span>{{ diffLabel(subject.subjectDiff) }}</span>
        </div>
        <h3>{{ subject.subjectName }}</h3>
        <p>{{ subject.subjectComment || subject.subjectParse || '暂时还没有补充解析说明。' }}</p>

        <ul v-if="subject.optionList?.length" class="option-preview">
          <li v-for="option in subject.optionList.slice(0, 4)" :key="`${subject.id}-${option.optionType}`">
            <strong>{{ option.optionType }}</strong>
            <span>{{ option.optionContent }}</span>
          </li>
        </ul>

        <div class="subject-card__footer">
          <span>浏览 {{ subject.browseCount ?? 0 }}</span>
          <span>ID {{ subject.id }}</span>
        </div>
      </article>

      <div v-if="!subjects.length" class="panel-empty panel-empty--full">
        当前筛选条件下还没有题目。
      </div>
    </div>

    <div class="pager">
      <button class="button button--ghost" type="button" :disabled="pageNo <= 1" @click="prevPage">
        上一页
      </button>
      <span>第 {{ pageNo }} 页 / 共 {{ Math.max(1, Math.ceil(total / pageSize)) }} 页</span>
      <button
        class="button button--ghost"
        type="button"
        :disabled="pageNo * pageSize >= total"
        @click="nextPage"
      >
        下一页
      </button>
    </div>
  </section>
</template>
