import { reactive } from 'vue'
import type { UserProfile } from '../types/api'

const TOKEN_KEY = 'colacode_token'
const USER_KEY = 'colacode_user'

function readUser() {
  const raw = window.localStorage.getItem(USER_KEY)
  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw) as UserProfile
  } catch {
    return null
  }
}

export const session = reactive({
  token: window.localStorage.getItem(TOKEN_KEY) ?? '',
  user: readUser() as UserProfile | null,
})

export function setToken(token: string) {
  session.token = token
  window.localStorage.setItem(TOKEN_KEY, token)
}

export function setUser(user: UserProfile | null) {
  session.user = user
  if (user) {
    window.localStorage.setItem(USER_KEY, JSON.stringify(user))
  } else {
    window.localStorage.removeItem(USER_KEY)
  }
}

export function clearAuth() {
  session.token = ''
  session.user = null
  window.localStorage.removeItem(TOKEN_KEY)
  window.localStorage.removeItem(USER_KEY)
}
