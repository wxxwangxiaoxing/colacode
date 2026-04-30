import { reactive } from 'vue'

function normalizePath(pathname: string) {
  if (!pathname || pathname === '') {
    return '/'
  }

  const trimmed = pathname.replace(/\/+$/, '')
  return trimmed === '' ? '/' : trimmed
}

const routeState = reactive({
  path: normalizePath(window.location.pathname),
  search: window.location.search,
})

function syncRoute() {
  routeState.path = normalizePath(window.location.pathname)
  routeState.search = window.location.search
}

window.addEventListener('popstate', syncRoute)

export function useRoute() {
  return routeState
}

export function navigate(to: string) {
  const url = new URL(to, window.location.origin)
  window.history.pushState({}, '', `${url.pathname}${url.search}${url.hash}`)
  syncRoute()
}
