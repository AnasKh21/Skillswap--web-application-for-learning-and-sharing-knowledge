/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        primary:   '#FF6B35',
        danger:    '#E63946',
        accent:    '#FFB703',
        surface:   '#FFFBF5',
        'surface-card': '#FFFFFF',
        dark:      '#1A1A2E',
        muted:     '#6B7280',
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        card: '0 8px 32px rgba(255, 107, 53, 0.12)',
        'card-hover': '0 16px 48px rgba(255, 107, 53, 0.2)',
      },
      backgroundImage: {
        'gradient-brand': 'linear-gradient(135deg, #FF6B35 0%, #FFB703 100%)',
        'gradient-danger': 'linear-gradient(135deg, #E63946 0%, #FF6B35 100%)',
        'gradient-hero': 'linear-gradient(160deg, #FF6B35 0%, #FFB703 50%, #E63946 100%)',
      }
    },
  },
  plugins: [],
}
