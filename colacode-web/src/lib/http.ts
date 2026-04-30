import { session } from './session'
import type {
  ApiResult,
  LoginPayload,
  PageResult,
  PracticeInfo,
  PracticeSet,
  PracticeSubmitPayload,
  RegisterPayload,
  SubjectCategory,
  SubjectInfo,
  UserProfile,
} from '../types/api'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:5000'

function buildQuery(params?: Record<string, string | number | boolean | undefined | null>) {
  const search = new URLSearchParams()

  Object.entries(params ?? {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.set(key, String(value))
    }
  })

  const query = search.toString()
  return query ? `?${query}` : ''
}

async function request<T>(path: string, init?: RequestInit, query?: Record<string, string | number | boolean | undefined | null>) {
  const headers = new Headers(init?.headers)
  headers.set('Accept', 'application/json')

  if (init?.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  if (session.token) {
    headers.set('Authorization', session.token)
  }

  const response = await fetch(`${API_BASE_URL}${path}${buildQuery(query)}`, {
    ...init,
    headers,
  })

  const payload = (await response.json()) as ApiResult<T>

  if (!response.ok || !payload.success) {
    throw new Error(payload.message || '请求失败')
  }

  return payload.data
}

export function login(payload: LoginPayload) {
  return request<string>('/auth/user/login', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function register(payload: RegisterPayload) {
  return request<void>('/auth/user/register', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function logoutRequest() {
  return request<void>('/auth/user/logout', {
    method: 'POST',
  })
}

export function getCurrentUser() {
  return request<UserProfile>('/auth/user/info')
}

export function getCategoryTree() {
  return request<SubjectCategory[]>('/subject/category/queryCategoryAndLabel')
}

export function getSubjectPage(query: {
  categoryId?: number | null
  subjectType?: number | null
  pageNo?: number
  pageSize?: number
}) {
  return request<PageResult<SubjectInfo>>('/subject/info/page', undefined, {
    categoryId: query.categoryId ?? undefined,
    subjectType: query.subjectType ?? undefined,
    pageNo: query.pageNo ?? 1,
    pageSize: query.pageSize ?? 9,
  })
}

export function getContributeList(limit = 5) {
  return request<SubjectInfo[]>('/subject/info/contribute', undefined, { limit })
}

export function getPracticeSets(pageNo = 1, pageSize = 8, name = '') {
  return request<PracticeSet[]>('/practice/preSet/list', undefined, { pageNo, pageSize, name })
}

export function getPracticeHistory() {
  return request<PracticeInfo[]>('/practice/history')
}

export function getPracticeSubjectIds(setId: number) {
  return request<number[]>('/practice/set/subjects', undefined, { setId })
}

export function batchQuerySubjects(ids: number[]) {
  return request<SubjectInfo[]>('/subject/info/batchQuery', {
    method: 'POST',
    body: JSON.stringify(ids),
  })
}

export function submitPractice(payload: PracticeSubmitPayload) {
  return request<PracticeInfo>('/practice/submit', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}
