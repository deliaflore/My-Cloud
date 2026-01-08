import React from 'react';
import Sidebar from './Sidebar';
import Header from './Header';
import './Layout.css';

export default function Layout({ children, user, storage, networkStatus, onLogout, userRole }) {
    return (
        <div className="app-layout">
            <Sidebar userRole={userRole} />
            <div className="main-content">
                <Header 
                    user={user} 
                    storage={storage} 
                    networkStatus={networkStatus} 
                    onLogout={onLogout} 
                />
                <main className="page-content">
                    {children}
                </main>
            </div>
        </div>
    );
}
