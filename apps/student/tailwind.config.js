/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './app/**/*.{js,jsx,ts,tsx}',
    './components/**/*.{js,jsx,ts,tsx}',
    '../../packages/ui/src/**/*.{js,jsx,ts,tsx}',
  ],
  presets: [require('nativewind/preset')],
  theme: {
    extend: {
      colors: {
        primary: '#6C63FF',
        'primary-light': '#EAE8FF',
        'primary-dark': '#4B44CC',
        secondary: '#FF6584',
        'secondary-light': '#FFE8ED',
        background: '#F8F7FF',
        surface: '#FFFFFF',
        border: '#E8E6F0',
        'text-main': '#2D2D2D',
        'text-secondary': '#7B7B8D',
        success: '#43C59E',
        error: '#FF4D6A',
      },
      fontFamily: {
        sans: ['Inter', 'System'],
      },
    },
  },
  plugins: [],
}
