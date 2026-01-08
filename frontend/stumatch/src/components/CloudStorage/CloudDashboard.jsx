import React, { useState, useEffect } from 'react';
import { 
    HardDrive, Upload, Download, Trash2, Server, 
    Play, Square, RotateCw, X, Activity, File
} from 'lucide-react';
import ChunkDistribution from './ChunkDistribution';
import ConfirmModal from './ConfirmModal';
import WelcomeMessage from '../WelcomeMessage';
import './CloudDashboard.css';

const API_BASE = 'http://localhost:8081/api';

// Node configuration - Reduced from 100GB to 5GB for development
// (100GB was causing "not enough disk space" errors)
const NODE_STORAGE_GB = 5;  // Change this to allocate more/less storage per node
const NODE_RAM_GB = 8;

export default function CloudDashboard() {
    const [userDashboard, setUserDashboard] = useState(null);
    const [files, setFiles] = useState([]);
    const [nodes, setNodes] = useState([]);
    const [runningNodes, setRunningNodes] = useState([]);
    const [networkStatus, setNetworkStatus] = useState(null);
    const [loading, setLoading] = useState(true);
    const [uploading, setUploading] = useState(false);
    const [message, setMessage] = useState(null);
    const [latestDistribution, setLatestDistribution] = useState(null);
    const [confirmModal, setConfirmModal] = useState({ isOpen: false, title: '', message: '', onConfirm: null, danger: false });
    const [showWelcome, setShowWelcome] = useState(true);
    // Node lifecycle states: 'creating' | 'starting' | 'registering' | 'active' | 'failed' | 'offline'
    const [nodeLifecycleStates, setNodeLifecycleStates] = useState({}); // { nodeId: { state: 'creating', progress: 30, error: null } }

    useEffect(() => {
        fetchDashboardData();
        const interval = setInterval(fetchDashboardData, 5000); // Refresh every 5s
        return () => clearInterval(interval);
    }, []);

    const fetchDashboardData = async () => {
        const token = localStorage.getItem('token');
        try {
            // Fetch user dashboard
            const dashRes = await fetch(`${API_BASE}/user/dashboard`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (dashRes.ok) setUserDashboard(await dashRes.json());

            // Fetch files
            const filesRes = await fetch(`${API_BASE}/user/dashboard/files`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (filesRes.ok) setFiles(await filesRes.json());

            // Fetch network status
            const netRes = await fetch(`${API_BASE}/network/status`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (netRes.ok) setNetworkStatus(await netRes.json());

            // Fetch nodes
            const nodesRes = await fetch(`${API_BASE}/network/nodes`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (nodesRes.ok) setNodes(await nodesRes.json());

            // Fetch running nodes
            const runningRes = await fetch(`${API_BASE}/network/nodes/running`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (runningRes.ok) {
                const data = await runningRes.json();
                setRunningNodes(data.runningNodes || []);
            }

        } catch (error) {
            console.error('Failed to fetch dashboard data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleUpload = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        setUploading(true);
        const formData = new FormData();
        formData.append('file', file);

        try {
            const token = localStorage.getItem('token');
            const response = await fetch(`${API_BASE}/files/upload`, {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${token}` },
                body: formData
            });

            if (response.ok) {
                const result = await response.json();
                showMessage('File uploaded successfully!', 'success');
                
                // Fetch chunk distribution for the uploaded file
                if (result.id) {
                    fetchFileDistribution(result.id, file.name);
                }
                
                fetchDashboardData();
            } else {
                showMessage('Upload failed', 'error');
            }
        } catch (error) {
            showMessage('Upload failed', 'error');
        } finally {
            setUploading(false);
            e.target.value = '';
        }
    };

    const handleDownload = async (fileId, fileName) => {
        try {
            const token = localStorage.getItem('token');
            const response = await fetch(`${API_BASE}/files/${fileId}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.ok) {
                const blob = await response.blob();
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = fileName;
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                document.body.removeChild(a);
            }
        } catch (error) {
            showMessage('Download failed', 'error');
        }
    };

    const handleDelete = (fileId, fileName) => {
        setConfirmModal({
            isOpen: true,
            title: 'Delete File',
            message: `Are you sure you want to delete "${fileName}"? This action cannot be undone.`,
            danger: true,
            onConfirm: () => deleteFile(fileId)
        });
    };

    const deleteFile = async (fileId) => {
        try {
            const token = localStorage.getItem('token');
            const response = await fetch(`${API_BASE}/files/${fileId}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.ok) {
                showMessage('File deleted', 'success');
                fetchDashboardData();
            }
        } catch (error) {
            showMessage('Delete failed', 'error');
        }
    };

    const handleStartNode = async () => {
        const nodeId = `node${Date.now()}`;
        
        // Smart port allocation - find next free port
        const usedPorts = new Set();
        nodes.forEach(node => {
            if (node.port) usedPorts.add(node.port);
        });
        runningNodes.forEach(nId => {
            const node = nodes.find(n => n.nodeId === nId);
            if (node && node.port) usedPorts.add(node.port);
        });
        
        // Find next free port starting from 50051
        let port = 50051;
        while (usedPorts.has(port)) {
            port++;
        }

        // LIFECYCLE STEP 1: Creating
        setNodeLifecycleStates(prev => ({
            ...prev,
            [nodeId]: { state: 'creating', progress: 10, error: null, port }
        }));
        showMessage(`Creating node on port ${port}...`, 'info');

        try {
            const token = localStorage.getItem('token');
            
            // LIFECYCLE STEP 2: Starting
            setNodeLifecycleStates(prev => ({
                ...prev,
                [nodeId]: { state: 'starting', progress: 30, error: null, port }
            }));

            const response = await fetch(`${API_BASE}/network/nodes/start`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ nodeId, port, storageGB: NODE_STORAGE_GB, ramGB: NODE_RAM_GB })
            });

            if (response.ok) {
                // LIFECYCLE STEP 3: Registering
                setNodeLifecycleStates(prev => ({
                    ...prev,
                    [nodeId]: { state: 'registering', progress: 60, error: null, port }
                }));
                showMessage(`Node ${nodeId} registering...`, 'success');
                
                // Poll for active status
                let attempts = 0;
                const maxAttempts = 15; // Give more time
                const checkInterval = setInterval(async () => {
                    attempts++;
                    
                    // Fetch running nodes directly to avoid stale state
                    try {
                        const token = localStorage.getItem('token');
                        const runningRes = await fetch(`${API_BASE}/network/nodes/running`, {
                            headers: { 'Authorization': `Bearer ${token}` }
                        });
                        
                        if (runningRes.ok) {
                            const data = await runningRes.json();
                            const activeNodes = data.runningNodes || [];
                            const isActive = activeNodes.includes(nodeId);
                            
                            if (isActive) {
                                // LIFECYCLE STEP 4: Active!
                                setNodeLifecycleStates(prev => ({
                                    ...prev,
                                    [nodeId]: { state: 'active', progress: 100, error: null, port }
                                }));
                                showMessage(`✅ Node ${nodeId} is active!`, 'success');
                                clearInterval(checkInterval);
                                
                                // Refresh full dashboard data
                                fetchDashboardData();
                                
                                // Clear lifecycle state after 3 seconds
                                setTimeout(() => {
                                    setNodeLifecycleStates(prev => {
                                        const newStates = { ...prev };
                                        delete newStates[nodeId];
                                        return newStates;
                                    });
                                }, 3000);
                            } else if (attempts >= maxAttempts) {
                                // Failed to activate
                                setNodeLifecycleStates(prev => ({
                                    ...prev,
                                    [nodeId]: { 
                                        state: 'failed', 
                                        progress: 100, 
                                        error: 'Node did not register within 15 seconds. Check backend logs.',
                                        port 
                                    }
                                }));
                                showMessage(`⚠️ Node ${nodeId} failed to activate`, 'error');
                                clearInterval(checkInterval);
                            } else {
                                // Still waiting
                                const progress = 60 + (attempts * 2.5);
                                setNodeLifecycleStates(prev => ({
                                    ...prev,
                                    [nodeId]: { state: 'registering', progress: Math.min(progress, 99), error: null, port }
                                }));
                            }
                        } else {
                            console.error('Failed to fetch running nodes');
                        }
                    } catch (err) {
                        console.error('Error checking node status:', err);
                    }
                }, 1000);
            } else {
                const error = await response.json();
                setNodeLifecycleStates(prev => ({
                    ...prev,
                    [nodeId]: { 
                        state: 'failed', 
                        progress: 100, 
                        error: error.message || 'Unknown error',
                        port 
                    }
                }));
                showMessage(`Failed to create node: ${error.message || 'Unknown error'}`, 'error');
            }
        } catch (error) {
            setNodeLifecycleStates(prev => ({
                ...prev,
                [nodeId]: { 
                    state: 'failed', 
                    progress: 100, 
                    error: error.message,
                    port 
                }
            }));
            showMessage(`Failed to create node: ${error.message}`, 'error');
        }
    };

    const handleStartExistingNode = async (nodeId) => {
        // Find the node's port (assuming it's stored or we can fetch it)
        const node = nodes.find(n => n.nodeId === nodeId);
        const port = node?.port || (50051 + nodes.indexOf(node));

        try {
            const token = localStorage.getItem('token');
            const response = await fetch(`${API_BASE}/network/nodes/start`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ nodeId, port, storageGB: NODE_STORAGE_GB, ramGB: NODE_RAM_GB })
            });

            if (response.ok) {
                showMessage(`Starting ${nodeId}...`, 'success');
                setTimeout(fetchDashboardData, 3000);
            } else {
                showMessage(`Failed to start ${nodeId}`, 'error');
            }
        } catch (error) {
            showMessage(`Failed to start ${nodeId}`, 'error');
        }
    };

    const handleStopNode = async (nodeId) => {
        try {
            const token = localStorage.getItem('token');
            const response = await fetch(`${API_BASE}/network/nodes/stop/${nodeId}`, {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.ok) {
                showMessage('Node stopped', 'success');
                fetchDashboardData();
            }
        } catch (error) {
            showMessage('Failed to stop node', 'error');
        }
    };

    const handleRestartNode = async (nodeId) => {
        try {
            const token = localStorage.getItem('token');
            const port = 50051 + nodes.findIndex(n => n.nodeId === nodeId);
            const response = await fetch(`${API_BASE}/network/nodes/restart/${nodeId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ port, storageGB: NODE_STORAGE_GB, ramGB: NODE_RAM_GB })
            });

            if (response.ok) {
                showMessage('Node restarted', 'success');
                setTimeout(fetchDashboardData, 3000);
            }
        } catch (error) {
            showMessage('Failed to restart node', 'error');
        }
    };

    const handleDeleteNode = (nodeId) => {
        setConfirmModal({
            isOpen: true,
            title: 'Delete Node',
            message: `Are you sure you want to delete node "${nodeId}"? This will stop the node and remove it from the network.`,
            danger: true,
            onConfirm: () => deleteNode(nodeId)
        });
    };

    const deleteNode = async (nodeId) => {
        try {
            const token = localStorage.getItem('token');
            const response = await fetch(`${API_BASE}/network/nodes/${nodeId}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.ok) {
                showMessage('Node deleted', 'success');
                fetchDashboardData();
            }
        } catch (error) {
            showMessage('Failed to delete node', 'error');
        }
    };

    const handleDeleteAllNodes = () => {
        setConfirmModal({
            isOpen: true,
            title: 'Stop All Nodes',
            message: `Are you sure you want to stop ALL ${nodes.length} nodes? This will halt all distributed storage operations.`,
            danger: true,
            onConfirm: () => deleteAllNodes()
        });
    };

    const deleteAllNodes = async () => {
        try{
            const token = localStorage.getItem('token');
            const response = await fetch(`${API_BASE}/network/nodes/delete-all`, {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.ok) {
                showMessage('All nodes stopped', 'success');
                fetchDashboardData();
            }
        } catch (error) {
            showMessage('Failed to stop nodes', 'error');
        }
    };

    const fetchFileDistribution = async (fileId, fileName) => {
        try {
            const token = localStorage.getItem('token');
            const response = await fetch(`${API_BASE}/user/dashboard/files/${fileId}/distribution`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.ok) {
                const distribution = await response.json();
                setLatestDistribution(distribution);
                
                // Clear after 10 seconds
                setTimeout(() => setLatestDistribution(null), 10000);
            }
        } catch (error) {
            console.error('Failed to fetch distribution:', error);
        }
    };

    const showMessage = (text, type) => {
        setMessage({ text, type });
        setTimeout(() => setMessage(null), 3000);
    };

    const formatBytes = (bytes) => {
        if (!bytes) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
    };

    if (loading) return <div className="loading">Loading dashboard...</div>;

    return (
        <div className="cloud-dashboard">
            {/* Welcome Message - Shows on first login */}
            {showWelcome && (
                <WelcomeMessage
                    user={{ email: userDashboard?.userName }}
                    quota={userDashboard?.totalQuotaBytes}
                    onClose={() => setShowWelcome(false)}
                />
            )}

            {/* Confirmation Modal */}
            <ConfirmModal
                isOpen={confirmModal.isOpen}
                title={confirmModal.title}
                message={confirmModal.message}
                onConfirm={confirmModal.onConfirm}
                onCancel={() => setConfirmModal({ ...confirmModal, isOpen: false })}
                danger={confirmModal.danger}
            />

            {message && (
                <div className={`message ${message.type}`}>
                    {message.text}
                </div>
            )}

            {/* User Storage Overview */}
            <div className="dashboard-header">
                <h1>Welcome, {userDashboard?.userName || 'User'}!</h1>
                <p className="subtitle">Your Cloud Storage Dashboard</p>
            </div>

            <div className="storage-overview">
                <div className="storage-card">
                    <div className="storage-icon">
                        <HardDrive size={32} />
                    </div>
                    <div className="storage-details">
                        <h3>Storage Quota</h3>
                        <div className="storage-bar">
                            <div 
                                className="storage-fill" 
                                style={{width: `${userDashboard?.usagePercentage || 0}%`}}
                            />
                        </div>
                        <div className="storage-stats">
                            <span>{userDashboard?.usedGB || '0 B'} used</span>
                            <span>{userDashboard?.usagePercentage?.toFixed(1) || 0}%</span>
                            <span>{userDashboard?.availableGB || '0 B'} free</span>
                        </div>
                        <p className="storage-total">Total: {userDashboard?.quotaGB || '2 GB'}</p>
                    </div>
                </div>

                <div className="stats-grid">
                    <div className="stat-card">
                        <File size={24} />
                        <div>
                            <h4>{userDashboard?.totalFiles || 0}</h4>
                            <p>Files</p>
                        </div>
                    </div>
                    <div className="stat-card">
                        <Server size={24} />
                        <div>
                            <h4>{runningNodes.length}/{nodes.length}</h4>
                            <p>Nodes Active</p>
                        </div>
                    </div>
                    <div className="stat-card">
                        <HardDrive size={24} />
                        <div>
                            <h4>{nodes.length * NODE_STORAGE_GB} GB</h4>
                            <p>Total Storage</p>
                        </div>
                    </div>
                </div>
            </div>

            {/* File Upload */}
            <div className="upload-section">
                <label className="upload-btn">
                    <Upload size={20} />
                    {uploading ? 'Uploading...' : 'Upload File'}
                    <input type="file" onChange={handleUpload} disabled={uploading} />
                </label>
            </div>

            {/* Chunk Distribution Visualization */}
            {latestDistribution && (
                <ChunkDistribution 
                    distribution={latestDistribution.distribution} 
                    fileName={latestDistribution.fileName}
                />
            )}

            {/* Files List */}
            <div className="files-section">
                <h2>My Files ({files.length})</h2>
                {files.length === 0 ? (
                    <p className="empty-state">No files yet. Upload your first file!</p>
                ) : (
                    <div className="files-list">
                        {files.map(file => (
                            <div key={file.id} className="file-card">
                                <div className="file-info">
                                    <File size={20} />
                                    <div>
                                        <h4>{file.fileName}</h4>
                                        <p>{formatBytes(file.sizeBytes)} • {new Date(file.createdAt).toLocaleDateString()}</p>
                                    </div>
                                </div>
                                <div className="file-actions">
                                    <button onClick={() => handleDownload(file.id, file.fileName)} title="Download">
                                        <Download size={18} />
                                    </button>
                                    <button onClick={() => handleDelete(file.id, file.fileName)} title="Delete" className="danger">
                                        <Trash2 size={18} />
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Node Management */}
            <div className="nodes-section">
                <div className="section-header">
                    <h2>Node Management</h2>
                    <div className="node-controls">
                        <button onClick={handleStartNode} className="btn-start">
                            <Play size={18} /> Start New Node
                        </button>
                        <button onClick={handleDeleteAllNodes} className="btn-danger">
                            <Square size={18} /> Stop All
                        </button>
                    </div>
                </div>

                {nodes.length === 0 && Object.keys(nodeLifecycleStates).length === 0 ? (
                    <p className="empty-state">No nodes yet. Start your first node!</p>
                ) : (
                    <div className="nodes-grid">
                        {/* Show nodes being created/started first */}
                        {Object.entries(nodeLifecycleStates).map(([nodeId, lifecycle]) => (
                            <div key={nodeId} className={`node-card ${lifecycle.state}`}>
                                <div className="node-header">
                                    <div className="node-title">
                                        <Server size={20} />
                                        <h4>{nodeId}</h4>
                                    </div>
                                    <span className={`status-badge lifecycle-${lifecycle.state}`}>
                                        {lifecycle.state === 'creating' && '⚙️ Creating...'}
                                        {lifecycle.state === 'starting' && '🚀 Starting...'}
                                        {lifecycle.state === 'registering' && '📡 Registering...'}
                                        {lifecycle.state === 'active' && '✅ Active'}
                                        {lifecycle.state === 'failed' && '❌ Failed'}
                                    </span>
                                </div>
                                
                                {/* Progress Bar */}
                                <div className="lifecycle-progress">
                                    <div className="progress-bar">
                                        <div 
                                            className={`progress-fill ${lifecycle.state}`}
                                            style={{ width: `${lifecycle.progress}%` }}
                                        />
                                    </div>
                                    <div className="progress-text">
                                        <span>Port: {lifecycle.port}</span>
                                        <span>{lifecycle.progress}%</span>
                                    </div>
                                </div>

                                {/* Error Message */}
                                {lifecycle.error && (
                                    <div className="lifecycle-error">
                                        ⚠️ {lifecycle.error}
                                    </div>
                                )}
                            </div>
                        ))}

                        {/* Show existing nodes */}
                        {nodes.map(node => {
                            const isRunning = runningNodes.includes(node.nodeId);
                            const inLifecycle = nodeLifecycleStates[node.nodeId];
                            
                            // Don't show if in lifecycle state
                            if (inLifecycle) return null;
                            
                            return (
                                <div key={node.nodeId} className={`node-card ${isRunning ? 'running' : 'offline'}`}>
                                    <div className="node-header">
                                        <div className="node-title">
                                            <Server size={20} />
                                            <h4>{node.nodeId}</h4>
                                        </div>
                                        <span className={`status-badge ${isRunning ? 'active' : 'inactive'}`}>
                                            {isRunning ? '● Running' : '○ Offline'}
                                        </span>
                                    </div>
                                    <div className="node-info">
                                        <small>Port: {node.port || '—'}</small>
                                    </div>
                                    <div className="node-actions">
                                        {isRunning ? (
                                            <>
                                                <button onClick={() => handleStopNode(node.nodeId)} title="Stop">
                                                    <Square size={16} /> Stop
                                                </button>
                                                <button onClick={() => handleRestartNode(node.nodeId)} title="Restart">
                                                    <RotateCw size={16} /> Restart
                                                </button>
                                            </>
                                        ) : (
                                            <button onClick={() => handleStartExistingNode(node.nodeId)} title="Start">
                                                <Play size={16} /> Start
                                            </button>
                                        )}
                                        <button onClick={() => handleDeleteNode(node.nodeId)} className="btn-delete" title="Delete">
                                            <X size={16} /> Delete
                                        </button>
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                )}
            </div>
        </div>
    );
}
