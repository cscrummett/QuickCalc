import React from 'react';
import { createRoot } from 'react-dom/client';
import App from './App';
import './index.css';

console.log('React index.js is running');

// Simple approach - just render to the root element if it exists
const container = document.getElementById('root');

if (container) {
  console.log('Found root container, rendering React app');
  const root = createRoot(container);
  root.render(
    <React.StrictMode>
      <App />
    </React.StrictMode>
  );
}
