import React from 'react';
import Sidebar from '../Sidebar/Sidebar';
import MainContent from './MainContent';
import useAppStore from '../../store/appStore';
import { FaBars, FaCog } from 'react-icons/fa';

const Layout = () => {
  const sidebarCollapsed = useAppStore(state => state.sidebarCollapsed);
  const toggleSidebar = useAppStore(state => state.toggleSidebar);
  const togglePropertiesPanel = useAppStore(state => state.togglePropertiesPanel);
  
  return (
    <div className="flex h-screen bg-gray-100">
      {/* Sidebar */}
      <div 
        className={`bg-gray-800 text-white transition-all duration-300 ${
          sidebarCollapsed ? 'w-0 overflow-hidden' : 'w-64'
        }`}
      >
        <Sidebar />
      </div>
      
      {/* Main Content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Header */}
        <header className="bg-white shadow-sm h-16 flex items-center px-6">
          <button 
            className="p-2 rounded-md hover:bg-gray-100 mr-4"
            onClick={toggleSidebar}
          >
            <FaBars className="text-gray-600" />
          </button>
          
          <h1 className="text-xl font-semibold text-gray-800 flex-1">QuickCalc</h1>
          
          <button 
            className="p-2 rounded-md hover:bg-gray-100"
            onClick={togglePropertiesPanel}
          >
            <FaCog className="text-gray-600" />
          </button>
        </header>
        
        {/* Main Content Area */}
        <main className="flex-1 overflow-auto">
          <MainContent />
        </main>
      </div>
    </div>
  );
};

export default Layout;
