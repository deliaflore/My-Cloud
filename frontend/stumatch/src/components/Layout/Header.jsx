import React from 'react';
import { Bell, User, LogOut, HardDrive, Network } from 'lucide-react';
import './Header.css';

export default function Header({ user, storage, networkStatus, onLogout }) {
    const formatBytes = (bytes) => {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
    };

    return (
        <header className="app-header-new">
            <div className="header-left-new">
                <h1 className="header-title">Welcome back!</h1>
                <p className="header-subtitle">Manage your housing and find roommates</p>
            </div>

            <div className="header-right-new">
                {networkStatus && (
                    <div className="header-stat">
                        <Network className="stat-icon" />
                        <div className="stat-content">
                            <span className="stat-label">Network</span>
                            <span className="stat-value">{networkStatus.totalNodes || 0} nodes</span>
                        </div>
                    </div>
                )}

                {storage && (
                    <div className="header-stat">
                        <HardDrive className="stat-icon" />
                        <div className="stat-content">
                            <span className="stat-label">Storage</span>
                            <span className="stat-value">
                                {formatBytes(storage.used)} / {formatBytes(storage.total)}
                            </span>
                        </div>
                    </div>
                )}

                <button className="header-icon-btn" title="Notifications">
                    <Bell />
                    <span className="notification-badge">3</span>
                </button>

                <div className="header-user">
                    <div className="user-avatar">
                        <User />
                    </div>
                    <div className="user-info">
                        <span className="user-name">{user?.fullName || 'User'}</span>
                        <span className="user-role">Student</span>
                    </div>
                </div>

                <button className="header-icon-btn" onClick={onLogout} title="Logout">
                    <LogOut />
                </button>
            </div>
        </header>
    );
}
