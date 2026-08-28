import { useCounterStore } from '@/stores/counter'

export function useThemeSwitch() {
  const store = useCounterStore()

  function doToggleTheme() {
    const next = store.theme === 'dark' ? 'light' : 'dark'
    document.documentElement.setAttribute('data-theme', next)
    store.setTheme(next)
  }

  function fallbackToggleTheme() {
    document.documentElement.classList.add('theme-changing')
    doToggleTheme()
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        document.documentElement.classList.remove('theme-changing')
      })
    })
  }

  function handleToggleTheme(e) {
    if (!document.startViewTransition) {
      fallbackToggleTheme()
      return
    }

    const x = e.clientX ?? window.innerWidth / 2
    const y = e.clientY ?? window.innerHeight / 2
    const endRadius = Math.hypot(
      Math.max(x, innerWidth - x),
      Math.max(y, innerHeight - y)
    )

    const t = document.startViewTransition(() => doToggleTheme())
    t.ready.then(() => {
      document.documentElement.animate(
        {
          clipPath: [
            `circle(0px at ${x}px ${y}px)`,
            `circle(${endRadius}px at ${x}px ${y}px)`
          ]
        },
        {
          duration: 500,
          easing: 'ease-in-out',
          pseudoElement: '::view-transition-new(root)'
        }
      )
    })
  }

  return { handleToggleTheme }
}
