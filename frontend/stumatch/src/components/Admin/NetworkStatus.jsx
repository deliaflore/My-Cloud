import React from 'react';
import { Network, Server, HardDrive, Activity } from 'lucide-react';
import './NetworkStatus.css';

export default function NetworkStatus({ nodes, networkStatus, onStartNode, onStopNode }) {
    const formatBytes = (bytes) => {
        if (!bytes) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
    };

    return (
        <div className="network-status">
            <h1 className="page-title">Network Status</h1>
            <p className="page-subtitle">Monitor your distributed storage network</p>

            {networkStatus && (
                <div className="network-stats">
                    <div className="stat-box">
                        <Network className="stat-box-icon" />
                        <div>
                            <p className="stat-box-label">Active Nodes</p>
                            <h3 className="stat-box-value">{networkStatus.totalNodes || 0}</h3>
                        </div>
                    </div>
                    <div className="stat-box">
                        <HardDrive className="stat-box-icon" />
                        <div>
                            <p className="stat-box-label">Total Storage</p>
                            <h3 className="stat-box-value">{formatBytes(networkStatus.totalStorageBytes)}</h3>
                        </div>
                    </div>
                    <div className="stat-box">
                        <Activity className="stat-box-icon" />
                        <div>
                            <p className="stat-box-label">Used Storage</p>
                            <h3 className="stat-box-value">{formatBytes(networkStatus.usedStorageBytes)}</h3>
                        </div>
                    </div>
                    <div className="stat-box">
                        <Server className="stat-box-icon" />
                        <div>
                            <p className="stat-box-label">Total Chunks</p>
                            <h3 className="stat-box-value">{networkStatus.totalChunks || 0}</h3>
                        </div>
                    </div>
                </div>
            )}

            <div className="nodes-section">
                <h2>Storage Nodes</h2>
                <div className="nodes-grid">
                    {nodes && nodes.length > 0 ? (
                        nodes.map(node => (
                            <div key={node.nodeId} className="node-card">
                                <Server className="node-icon" />
                                <h3>{node.nodeId}</h3>
                                <p className="node-status">Active</p>
                            </div>
                        ))
                    ) : (
                        <p>No nodes registered yet</p>
                    )}
                </div>
            </div>
        </div>
    );
}
