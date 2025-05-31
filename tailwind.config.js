module.exports = {
  content: [
    './frontend/src/**/*.{js,jsx}',
    './frontend/public/index.html',
  ],
  theme: {
    extend: {
      colors: {
        primary: '#1f77b4',
        gray: {
          100: '#f8fafc',
          200: '#e2e8f0',
          500: '#64748b',
        }
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
      },
      boxShadow: {
        sm: '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
        md: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
      },
      spacing: {
        '2': '8px',
        '4': '16px',
        '6': '24px',
        '8': '32px',
      }
    },
  },
  plugins: [],
};
