<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  batchQuerySubjects,
  getPracticeHistory,
  getPracticeSets,
  getPracticeSubjectIds,
  submitPractice,
} from '../lib/http'
import { navigate } from '../lib/router'
import { session } from '../lib/session'
import type { PracticeInfo, PracticeSet, SubjectInfo } from '../types/api'

const loading = ref(false)
const historyLoading = ref(false)
const error = ref('')
const presets = ref<PracticeSet[]>([])
const historyList = ref<PracticeInfo[]>([])
const activeSet = ref<PracticeSet | null>(null)
const practiceSubjects = ref<SubjectInfo[]>([])
const submitPending = ref(false)
const report = ref<PracticeInfo | null>(null)
const answers = reactive<Record<number, string | string[]>>({})

function isMultiple(subject: SubjectInfo) {
  return Boolean(subject.correctAnswer?.includes(','))
}

function answerValue(subjectId: number) {
  return answers[subjectId]
}

function setSingleAnswer(subjectId: number, option: string) {
  answers[subjectId] = option
}

function toggleMultipleAnswer(subjectId: number, option: string) {
  const current = Array.isArray(answers[subjectId]) ? [...answers[subjectId]] : []
  const index = current.indexOf(option)
  if (index >= 0) {
    current.splice(index, 1)
  } else {
    current.push(option)
  }
  answers[subjectId] = current
}

function setTextAnswer(subjectId: number, value: string) {
  answers[subjectId] = value
}

function serializeAnswer(subjectId: number) {
  const value = answers[subjectId]
  if (Array.isArray(value)) {
    return value.join(',')
  }
  return value?.trim?.() ?? ''
}

const answeredCount = computed(() =>
  practiceSubjects.value.filter((subject) => Boolean(serializeAnswer(subject.id))).length,
)

async function loadPresets() {
  loading.value = true
  error.value = ''

  try {
    presets.value = await getPracticeSets(1, 8)
  } catch (err) {
    error.value = err instanceof Error ? err.message : '练习组加载失败'
  } finally {
    loading.value = false
  }
}

async function loadHistory() {
  if (!session.token) {
    historyList.value = []
    return
  }

  historyLoading.value = true
  try {
    historyList.value = await getPracticeHistory()
  } catch {
    historyList.value = []
  } finally {
    historyLoading.value = false
  }
}

async function startPractice(set: PracticeSet) {
  loading.value = true
  error.value = ''
  report.value = null

  try {
    const ids = await getPracticeSubjectIds(set.id)
    const subjects = await batchQuerySubjects(ids)
    activeSet.value = set
    practiceSubjects.value = subjects
    Object.keys(answers).forEach((key) => delete answers[Number(key)])
  } catch (err) {
    error.value = err instanceof Error ? err.message : '练习题加载失败'
  } finally {
    loading.value = false
  }
}

async function submitCurrentPractice() {
  if (!activeSet.value) {
    return
  }

  if (!session.token) {
    navigate('/login?redirect=/practice')
    return
  }

  submitPending.value = true
  error.value = ''

  try {
    report.value = await submitPractice({
      setId: activeSet.value.id,
      answers: practiceSubjects.value.map((subject) => ({
        subjectId: subject.id,
        userAnswer: serializeAnswer(subject.id),
        timeUse: 0,
      })),
    })
    await loadHistory()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '提交练习失败'
  } finally {
    submitPending.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadPresets(), loadHistory()])
})
</script>

<template>
  <section class="page-section">
    <div class="page-section__intro">
      <p class="eyebrow">Practice Flow</p>
      <h1>练习页已经连上预设练习组、批量拉题和提交报告接口。</h1>
      <p>
        当前流程对接 `/practice/preSet/list`、`/practice/set/subjects`、`/subject/info/batchQuery`
        和 `/practice/submit`。
      </p>
    </div>

    <p v-if="error" class="inline-error">{{ error }}</p>

    <div class="practice-layout">
      <div class="stack">
        <article class="content-panel">
          <div class="panel-head">
            <h3>预设练习组</h3>
            <span class="panel-hint">{{ presets.length }} 组</span>
          </div>
          <div v-if="loading && !presets.length" class="panel-empty">正在加载练习组...</div>
          <div v-else class="preset-grid">
            <button
              v-for="preset in presets"
              :key="preset.id"
              class="preset-card"
              :class="{ 'is-selected': activeSet?.id === preset.id }"
              type="button"
              @click="startPractice(preset)"
            >
              <strong>{{ preset.setName }}</strong>
              <span>{{ preset.description || '从这一组开始一轮完整训练。' }}</span>
            </button>
          </div>
        </article>

        <article class="content-panel">
          <div class="panel-head">
            <h3>练习记录</h3>
            <span class="panel-hint">{{ session.token ? '已登录' : '登录后可查看' }}</span>
          </div>
          <div v-if="!session.token" class="panel-empty">
            登录后可以查看你的练习历史与报告。
          </div>
          <div v-else-if="historyLoading" class="panel-empty">正在加载历史记录...</div>
          <ul v-else-if="historyList.length" class="list-panel">
            <li v-for="item in historyList.slice(0, 6)" :key="item.id">
              <strong>{{ item.setName || `练习 ${item.id}` }}</strong>
              <span>
                得分 {{ item.totalScore ?? 0 }} · 正确 {{ item.correctCount ?? 0 }} · 错误
                {{ item.wrongCount ?? 0 }}
              </span>
            </li>
          </ul>
          <div v-else class="panel-empty">还没有练习记录。</div>
        </article>
      </div>

      <article class="content-panel content-panel--stretch">
        <div class="panel-head">
          <h3>{{ activeSet?.setName || '选择一组练习开始答题' }}</h3>
          <span class="panel-hint">{{ answeredCount }}/{{ practiceSubjects.length }} 已作答</span>
        </div>

        <div v-if="!activeSet" class="panel-empty panel-empty--tall">
          左侧选择一个预设练习组，页面会实时向后端拉取对应题目。
        </div>

        <div v-else-if="loading" class="panel-empty panel-empty--tall">
          正在加载练习题...
        </div>

        <div v-else class="quiz-stack">
          <article v-for="(subject, index) in practiceSubjects" :key="subject.id" class="quiz-card">
            <div class="quiz-card__head">
              <span>Q{{ index + 1 }}</span>
              <strong>{{ subject.subjectName }}</strong>
            </div>

            <div v-if="subject.optionList?.length" class="option-list">
              <button
                v-for="option in subject.optionList"
                :key="`${subject.id}-${option.optionType}`"
                class="option-button"
                :class="{
                  'is-selected': Array.isArray(answerValue(subject.id))
                    ? answerValue(subject.id).includes(option.optionType)
                    : answerValue(subject.id) === option.optionType,
                }"
                type="button"
                @click="
                  isMultiple(subject)
                    ? toggleMultipleAnswer(subject.id, option.optionType)
                    : setSingleAnswer(subject.id, option.optionType)
                "
              >
                <strong>{{ option.optionType }}</strong>
                <span>{{ option.optionContent }}</span>
              </button>
            </div>

            <textarea
              v-else
              class="answer-box"
              rows="4"
              :value="typeof answerValue(subject.id) === 'string' ? answerValue(subject.id) : ''"
              placeholder="输入你的答案或思路"
              @input="setTextAnswer(subject.id, ($event.target as HTMLTextAreaElement).value)"
            ></textarea>
          </article>

          <div class="submit-row">
            <button
              class="button button--primary"
              type="button"
              :disabled="submitPending"
              @click="submitCurrentPractice"
            >
              {{ session.token ? (submitPending ? '提交中...' : '提交本次练习') : '登录后提交练习' }}
            </button>
          </div>

          <article v-if="report" class="report-card">
            <p class="eyebrow">Practice Report</p>
            <h3>{{ report.setName || activeSet?.setName }}</h3>
            <div class="report-metrics">
              <div>
                <strong>{{ report.totalScore ?? 0 }}</strong>
                <span>总分</span>
              </div>
              <div>
                <strong>{{ report.correctCount ?? 0 }}</strong>
                <span>正确</span>
              </div>
              <div>
                <strong>{{ report.wrongCount ?? 0 }}</strong>
                <span>错误</span>
              </div>
            </div>
          </article>
        </div>
      </article>
    </div>
  </section>
</template>
