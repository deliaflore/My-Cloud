import React from 'react';
import { NavLink } from 'react-router-dom';
import { 
    LayoutDashboard, 
    Building2, 
    Users, 
    Home, 
    FolderOpen, 
    Settings,
    Network
} from 'lucide-react';
import './Sidebar.css';

export default function Sidebar({ userRole = 'student' }) {
    return (
        <aside className="sidebar">
            <div className="sidebar-header">
                <Building2 className="sidebar-logo" />
                <h2>MyCloud</h2>
                <p className="sidebar-subtitle">Student Housing Platform</p>
            </div>

            <nav className="sidebar-nav">
                <NavLink to="/" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
                    <LayoutDashboard className="nav-icon" />
                    <span>Dashboard</span>
                </NavLink>

                <NavLink to="/housing" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
                    <Building2 className="nav-icon" />
                    <span>Housing Market</span>
                </NavLink>

                <NavLink to="/roommates" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
                    <Users className="nav-icon" />
                    <span>Find Roommates</span>
                </NavLink>

                <NavLink to="/my-housing" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
                    <Home className="nav-icon" />
                    <span>My Housing</span>
                </NavLink>

                <NavLink to="/files" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
                    <FolderOpen className="nav-icon" />
                    <span>Cloud Storage</span>
                </NavLink>

                {userRole === 'admin' && (
                    <NavLink to="/admin" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
                        <Settings className="nav-icon" />
                        <span>Admin Panel</span>
                    </NavLink>
                )}

                <NavLink to="/network" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
                    <Network className="nav-icon" />
                    <span>Network Status</span>
                </NavLink>
            </nav>

            <div className="sidebar-footer">
                <div className="sidebar-info">
                    <p className="info-label">Distributed System</p>
                    <p className="info-value">Active</p>
                </div>
            </div>
        </aside>
    );
}
