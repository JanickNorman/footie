import React, { useState, useEffect, useRef } from 'react';

export default function SubNavbar({ items }) {
  const [activeItem, setActiveItem] = useState(items.length > 0 ? items[0].name : null);
  const [hoveredItem, setHoveredItem] = useState(null);
  const observer = useRef(null);

  useEffect(() => {
    if (observer.current) {
      observer.current.disconnect();
    }

    const handleIntersect = (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          const intersectingItem = items.find(item => `#${entry.target.id}` === item.link);
          if (intersectingItem) {
            setActiveItem(intersectingItem.name);
          }
        }
      });
    };

    observer.current = new IntersectionObserver(handleIntersect, {
      rootMargin: '-40% 0px -60% 0px',
    });

    const elements = items.map(item => document.querySelector(item.link)).filter(Boolean);
    elements.forEach(el => observer.current.observe(el));

    return () => {
      if (observer.current) {
        observer.current.disconnect();
      }
    };
  }, [items]);

  const navStyle = {
    position: 'fixed',
    top: 'var(--subnav-top, 104px)',
    left: 0,
    right: 0,
    display: 'block',
    justifyContent: 'center',
    padding: '10px 0',
    backgroundColor: 'rgba(255, 255, 255, 0.95)',
    backdropFilter: 'blur(8px)',
    borderBottom: '1px solid #e2e8f0',
    zIndex: 1000,
    boxShadow: '0 2px 4px rgba(0,0,0,0.05)',
    transition: 'top 360ms cubic-bezier(0.22,0.9,0.36,1)',
    willChange: 'top'
  };

  const itemStyle = (item) => ({
    padding: '8px 16px',
    cursor: 'pointer',
    fontWeight: '600',
    fontSize: '14px',
    color: activeItem === item.name || hoveredItem === item.name ? '#2b6cb0' : '#4a5568',
    borderBottom: activeItem === item.name ? '2px solid #2b6cb0' : '2px solid transparent',
    transition: 'all 0.2s ease-in-out',
    textDecoration: 'none',
    whiteSpace: 'nowrap',
  });

  // Scrollable contained track with always-visible arrows (hide native scrollbar)
  const wrapperRef = useRef(null)
  const trackRef = useRef(null)
  const [canScrollLeft, setCanScrollLeft] = useState(false)
  const [canScrollRight, setCanScrollRight] = useState(false)

  useEffect(() => {
    const track = trackRef.current
    if (!track) return

    const updateArrows = () => {
      const { scrollLeft, scrollWidth, clientWidth } = track
      setCanScrollLeft(scrollLeft > 8)
      setCanScrollRight(scrollLeft + clientWidth < scrollWidth - 8)
    }

    updateArrows()
    track.addEventListener('scroll', updateArrows, { passive: true })
    window.addEventListener('resize', updateArrows)
    return () => {
      track.removeEventListener('scroll', updateArrows)
      window.removeEventListener('resize', updateArrows)
    }
  }, [items])

  const scrollBy = (delta) => {
    const track = trackRef.current
    if (!track) return
    track.scrollBy({ left: delta, behavior: 'smooth' })
  }

  const arrowStyle = {
    position: 'absolute',
    top: '50%',
    transform: 'translateY(-50%)',
    width: 34,
    height: 34,
    borderRadius: 18,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    background: 'rgba(255,255,255,0.9)',
    boxShadow: '0 2px 6px rgba(0,0,0,0.12)',
    cursor: 'pointer',
    zIndex: 1101
  }

  return (
    <nav style={navStyle}>
      <div ref={wrapperRef} style={{maxWidth:1200,margin:'0 auto',width:'100%',position:'relative',padding:'0 12px'}}>
        {/* left arrow - always visible */}
        <button
          aria-label="Scroll left"
          onClick={() => scrollBy(-240)}
          style={{...arrowStyle,left:8,opacity: canScrollLeft ? 1 : 0.45,pointerEvents: canScrollLeft ? 'auto' : 'none'}}
        >
          ‹
        </button>

        {/* scroll track (native scrollbar hidden via CSS class) */}
        <div ref={trackRef} className="subnav-track" style={{overflowX:'auto',display:'flex',gap:12,alignItems:'center',padding:'6px 44px',scrollBehavior:'smooth'}}>
          {items.map((item) => (
            <a
              key={item.name}
              href={item.link}
              style={itemStyle(item)}
              onMouseEnter={() => setHoveredItem(item.name)}
              onMouseLeave={() => setHoveredItem(null)}
              onClick={(e) => {
                e.preventDefault();
                document.querySelector(item.link)?.scrollIntoView({
                  behavior: 'smooth',
                  block: 'start'
                });
                setTimeout(() => setActiveItem(item.name), 200);
              }}
            >
              {item.name}
            </a>
          ))}
        </div>

        {/* right arrow - always visible */}
        <button
          aria-label="Scroll right"
          onClick={() => scrollBy(240)}
          style={{...arrowStyle,right:8,opacity: canScrollRight ? 1 : 0.45,pointerEvents: canScrollRight ? 'auto' : 'none'}}
        >
          ›
        </button>
      </div>
    </nav>
  );
}
