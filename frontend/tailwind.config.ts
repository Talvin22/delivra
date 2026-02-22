import type { Config } from 'tailwindcss'

const config: Config = {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        // Design tokens — тёмная тема Delivra
        bg: {
          base:    '#0f1117',
          surface: '#1a1d2e',
          raised:  '#1e2235',
          border:  '#2d3148',
          muted:   '#4a4f6a',
        },
        text: {
          primary:   '#e2e8f0',
          secondary: '#8892b0',
          muted:     '#4a4f6a',
        },
        brand: {
          DEFAULT: '#6c8aff',
          hover:   '#5a78f0',
          muted:   '#6c8aff22',
        },
        success: { DEFAULT: '#22c55e', muted: '#22c55e22' },
        warning: { DEFAULT: '#f59e0b', muted: '#f59e0b22' },
        danger:  { DEFAULT: '#ff4d6d', muted: '#ff4d6d22' },
      },
      borderRadius: {
        sm: '6px',
        md: '10px',
        lg: '14px',
        xl: '20px',
      },
      fontFamily: {
        sans: ['-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'sans-serif'],
        mono: ['JetBrains Mono', 'Menlo', 'monospace'],
      },
      animation: {
        pulse2: 'pulse2 1.5s infinite',
      },
      keyframes: {
        pulse2: { '0%,100%': { opacity: '1' }, '50%': { opacity: '0.4' } },
      },
    },
  },
  plugins: [],
}

export default config
