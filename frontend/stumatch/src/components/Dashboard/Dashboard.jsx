import React from 'react';
import { 
    Building2, 
    Users, 
    FolderOpen, 
    Network,
    TrendingUp,
    Clock,
    CheckCircle,
    AlertCircle
} from 'lucide-react';
import './Dashboard.css';

export default function Dashboard({ files, networkStatus, storage }) {
    const formatBytes = (bytes) => {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
    };

    const stats = [
        {
            title: 'Available Listings',
            value: '45',
            change: '+12%',
            icon: Building2,
            color: '#667eea'
        },
        {
            title: 'Compatible Roommates',
            value: '12',
            change: '+3 new',
            icon: Users,
            color: '#f59e0b'
        },
        {
            title: 'My Files',
            value: files?.length || 0,
            change: formatBytes(storage?.used || 0),
            icon: FolderOpen,
            color: '#10b981'
        },
        {
            title: 'Network Nodes',
            value: networkStatus?.totalNodes || 0,
            change: 'Active',
            icon: Network,
            color: '#8b5cf6'
        }
    ];

    const recentActivity = [
        {
            type: 'listing',
            title: 'New listing near ICT Campus',
            description: '2-bedroom apartment, $120/month',
            time: '2 hours ago',
            icon: Building2,
            status: 'new'
        },
        {
            type: 'match',
            title: 'Roommate match found',
            description: 'John Doe - 85% compatibility',
            time: '5 hours ago',
            icon: Users,
            status: 'success'
        },
        {
            type: 'file',
            title: 'Contract uploaded',
            description: 'Lease Agreement.pdf distributed across 3 nodes',
            time: '1 day ago',
            icon: FolderOpen,
            status: 'success'
        },
        {
            type: 'system',
            title: 'Node registered',
            description: 'node3 joined the network',
            time: '2 days ago',
            icon: Network,
            status: 'info'
        }
    ];

    return (
        <div className="dashboard">
            <div className="dashboard-header">
                <div>
                    <h1 className="dashboard-title">Dashboard</h1>
                    <p className="dashboard-subtitle">Overview of your housing and storage</p>
                </div>
            </div>

            {/* Stats Grid */}
            <div className="stats-grid">
                {stats.map((stat, index) => (
                    <div key={index} className="stat-card">
                        <div className="stat-icon" style={{ background: `${stat.color}15`, color: stat.color }}>
                            <stat.icon />
                        </div>
                        <div className="stat-content">
                            <p className="stat-label">{stat.title}</p>
                            <h3 className="stat-value">{stat.value}</h3>
                            <p className="stat-change">
                                <TrendingUp className="change-icon" />
                                {stat.change}
                            </p>
                        </div>
                    </div>
                ))}
            </div>

            {/* Recent Activity */}
            <div className="activity-section">
                <div className="section-header">
                    <h2 className="section-title">Recent Activity</h2>
                    <button className="view-all-btn">View All</button>
                </div>

                <div className="activity-list">
                    {recentActivity.map((activity, index) => (
                        <div key={index} className="activity-item">
                            <div 
                                className={`activity-icon ${activity.status}`}
                                style={{ 
                                    background: activity.status === 'new' ? '#667eea15' : 
                                               activity.status === 'success' ? '#10b98115' : '#3b82f615',
                                    color: activity.status === 'new' ? '#667eea' : 
                                           activity.status === 'success' ? '#10b981' : '#3b82f6'
                                }}
                            >
                                <activity.icon />
                            </div>
                            <div className="activity-content">
                                <h4 className="activity-title">{activity.title}</h4>
                                <p className="activity-description">{activity.description}</p>
                            </div>
                            <div className="activity-time">
                                <Clock className="time-icon" />
                                <span>{activity.time}</span>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            {/* Quick Actions */}
            <div className="quick-actions">
                <div className="section-header">
                    <h2 className="section-title">Quick Actions</h2>
                </div>

                <div className="actions-grid">
                    <button className="action-card">
                        <Building2 className="action-icon" />
                        <h3>Browse Housing</h3>
                        <p>Find your perfect place</p>
                    </button>

                    <button className="action-card">
                        <Users className="action-icon" />
                        <h3>Find Roommates</h3>
                        <p>Match with compatible people</p>
                    </button>

                    <button className="action-card">
                        <FolderOpen className="action-icon" />
                        <h3>Upload Documents</h3>
                        <p>Store contracts & receipts</p>
                    </button>

                    <button className="action-card">
                        <CheckCircle className="action-icon" />
                        <h3>Update Profile</h3>
                        <p>Improve your matches</p>
                    </button>
                </div>
            </div>
        </div>
    );
}
