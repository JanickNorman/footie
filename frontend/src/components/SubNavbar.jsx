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
    position: 'sticky',
    top: '64px',
    display: 'flex',
    justifyContent: 'center',
    padding: '10px',
    backgroundColor: 'rgba(255, 255, 255, 0.85)',
    backdropFilter: 'blur(8px)',
    borderBottom: '1px solid #e2e8f0',
    zIndex: 1000,
    boxShadow: '0 2px 4px rgba(0,0,0,0.05)',
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

  return (
    <nav style={navStyle}>
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
             // A little timeout to allow scroll to happen before observer kicks in
            setTimeout(() => setActiveItem(item.name), 200);
          }}
        >
          {item.name}
        </a>
      ))}
    </nav>
  );
}
