import React, { useState } from 'react';
import { Cloud, Network, Home, Users } from 'lucide-react';
import CloudDashboard from './CloudStorage/CloudDashboard';
import './ModularDashboard.css';

/**
 * Modular Dashboard - Separation of Concerns
 * Provides tabbed interface for different system modules:
 * - Cloud Storage (file management)
 * - Network Management (node operations)
 * - Housing/Roommate (matching system)
 */
export default function ModularDashboard() {
    const [activeTab, setActiveTab] = useState('cloud');

    const tabs = [
        { id: 'cloud', label: 'Cloud Storage', icon: Cloud, color: '#3b82f6' },
        { id: 'network', label: 'Network', icon: Network, color: '#8b5cf6' },
        { id: 'housing', label: 'Housing', icon: Home, color: '#10b981' },
        { id: 'roommate', label: 'Roommate', icon: Users, color: '#f59e0b' }
    ];

    return (
        <div className="modular-dashboard">
            {/* Tab Navigation */}
            <div className="tab-navigation">
                <div className="tab-header">
                    <h1>STUMatch Platform</h1>
                    <p className="subtitle">Student Housing & Cloud Storage</p>
                </div>
                
                <div className="tabs">
                    {tabs.map(tab => {
                        const Icon = tab.icon;
                        return (
                            <button
                                key={tab.id}
                                className={`tab ${activeTab === tab.id ? 'active' : ''}`}
                                onClick={() => setActiveTab(tab.id)}
                                style={{
                                    '--tab-color': tab.color
                                }}
                            >
                                <Icon size={20} />
                                <span>{tab.label}</span>
                            </button>
                        );
                    })}
                </div>
            </div>

            {/* Tab Content */}
            <div className="tab-content">
                {activeTab === 'cloud' && (
                    <div className="tab-panel">
                        <CloudDashboard />
                    </div>
                )}

                {activeTab === 'network' && (
                    <div className="tab-panel">
                        <div className="coming-soon">
                            <Network size={64} />
                            <h2>Network Management</h2>
                            <p>Advanced node monitoring, topology visualization, and network analytics</p>
                            <div className="features-list">
                                <div className="feature">✓ Real-time node health monitoring</div>
                                <div className="feature">✓ Network topology visualization</div>
                                <div className="feature">✓ Bandwidth and latency metrics</div>
                                <div className="feature">✓ Distributed consensus tracking</div>
                            </div>
                        </div>
                    </div>
                )}

                {activeTab === 'housing' && (
                    <div className="tab-panel">
                        <div className="coming-soon">
                            <Home size={64} />
                            <h2>Housing Listings</h2>
                            <p>Browse and manage student housing listings</p>
                            <div className="features-list">
                                <div className="feature">✓ Search available housing</div>
                                <div className="feature">✓ Post new listings</div>
                                <div className="feature">✓ Virtual tours and photos</div>
                                <div className="feature">✓ Price comparison tools</div>
                            </div>
                        </div>
                    </div>
                )}

                {activeTab === 'roommate' && (
                    <div className="tab-panel">
                        <div className="coming-soon">
                            <Users size={64} />
                            <h2>Roommate Matching</h2>
                            <p>Find compatible roommates using AI-powered matching</p>
                            <div className="features-list">
                                <div className="feature">✓ Personality compatibility scoring</div>
                                <div className="feature">✓ Lifestyle preference matching</div>
                                <div className="feature">✓ Chat and messaging</div>
                                <div className="feature">✓ Group formation tools</div>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}
