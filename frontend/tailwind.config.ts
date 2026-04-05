import type { Config } from 'tailwindcss'

const config: Config = {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        // CSS-variable tokens — work for both dark and light themes.
        // Format: rgb(var(--name) / <alpha-value>) enables opacity modifiers like bg-bg-base/50
        bg: {
          base:    'rgb(var(--bg-base)    / <alpha-value>)',
          surface: 'rgb(var(--bg-surface) / <alpha-value>)',
          raised:  'rgb(var(--bg-raised)  / <alpha-value>)',
          border:  'rgb(var(--bg-border)  / <alpha-value>)',
          muted:   'rgb(var(--bg-muted)   / <alpha-value>)',
        },
        text: {
          primary:   'rgb(var(--text-primary)   / <alpha-value>)',
          secondary: 'rgb(var(--text-secondary) / <alpha-value>)',
          muted:     'rgb(var(--text-muted)     / <alpha-value>)',
        },
        // Brand / status colors stay fixed — same in both themes
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