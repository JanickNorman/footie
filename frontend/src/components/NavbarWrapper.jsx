import React from 'react'
import Scoreboard from './Scoreboard'
import GlobalNavbar from './GlobalNavbar'

export default function NavbarWrapper(){
  const scoreboardRef = React.useRef(null)
  const [collapsed, setCollapsed] = React.useState(false)
  const scoreboardHeight = 40 // default
  const navbarHeight = 64

  React.useEffect(() => {
    const el = scoreboardRef.current
    let measuredHeight = el ? el.getBoundingClientRect().height : scoreboardHeight

    // Throttle scroll handling via requestAnimationFrame to avoid running logic every scroll event
    let ticking = false
    let latestScrollY = window.scrollY

    const update = (scrollY) => {
      const shouldCollapse = scrollY > measuredHeight
      setCollapsed(prev => (prev === shouldCollapse ? prev : shouldCollapse))
      const paddingTop = shouldCollapse ? (navbarHeight + 20) : (measuredHeight + navbarHeight + 20)
      document.documentElement.style.setProperty('--page-padding-top', `${paddingTop}px`)
      const subnavTop = shouldCollapse ? (navbarHeight) : (measuredHeight + navbarHeight)
      document.documentElement.style.setProperty('--subnav-top', `${subnavTop}px`)
    }

    const onScroll = () => {
      latestScrollY = window.scrollY
      if (!ticking) {
        ticking = true
        requestAnimationFrame(() => {
          update(latestScrollY)
          ticking = false
        })
      }
    }

    const onResize = () => {
      measuredHeight = scoreboardRef.current ? scoreboardRef.current.getBoundingClientRect().height : scoreboardHeight
      requestAnimationFrame(() => update(window.scrollY))
    }

    // initialize
    update(window.scrollY)
    window.addEventListener('scroll', onScroll, { passive: true })
    window.addEventListener('resize', onResize)
    return () => {
      window.removeEventListener('scroll', onScroll)
      window.removeEventListener('resize', onResize)
    }
  }, [])

  return (
    <>
      <Scoreboard ref={scoreboardRef} collapsed={collapsed} />
      <GlobalNavbar topOffset={collapsed ? 0 : scoreboardHeight} />
    </>
  )
}
