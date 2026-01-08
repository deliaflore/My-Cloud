import React, { useState } from 'react';
import { 
    Upload, Download, Trash2, File, Network, 
    Info, ChevronUp, Loader, Server
} from 'lucide-react';
import './FileManager.css';

const API_BASE = 'http://localhost:8081/api';

export default function FileManager({ files, storage, networkStatus, nodes, token, onRefresh }) {
    const [loading, setLoading] = useState(false);
    const [expandedFile, setExpandedFile] = useState(null);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    const formatBytes = (bytes) => {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
    };

    const formatDate = (dateString) => {
        return new Date(dateString).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const handleUpload = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        setLoading(true);
        setError(null);

        const formData = new FormData();
        formData.append('file', file);

        try {
            const response = await fetch(`${API_BASE}/files/upload`, {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${token}` },
                body: formData
            });

            if (response.ok) {
                setSuccess(`File "${file.name}" uploaded successfully!`);
                setTimeout(() => setSuccess(null), 3000);
                onRefresh();
            } else {
                const data = await response.json();
                setError(data.error || 'Upload failed');
            }
        } catch (err) {
            setError('Upload failed. Please try again.');
        } finally {
            setLoading(false);
            e.target.value = '';
        }
    };

    const handleDownload = async (fileId, fileName) => {
        try {
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
            } else {
                setError('Download failed');
            }
        } catch (err) {
            setError('Download failed. Please try again.');
        }
    };

    const handleDelete = async (fileId, fileName) => {
        if (!window.confirm(`Are you sure you want to delete "${fileName}"?`)) return;

        setLoading(true);
        setError(null);

        try {
            const response = await fetch(`${API_BASE}/files/${fileId}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.ok) {
                setSuccess(`File "${fileName}" deleted successfully!`);
                setTimeout(() => setSuccess(null), 3000);
                onRefresh();
            } else {
                setError('Delete failed');
            }
        } catch (err) {
            setError('Delete failed. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="file-manager">
            <div className="file-manager-header">
                <div>
                    <h1 className="page-title">Cloud Storage</h1>
                    <p className="page-subtitle">Distributed file storage across your network</p>
                </div>
            </div>

            {/* Alerts */}
            {error && (
                <div className="alert alert-error">
                    {error}
                </div>
            )}
            {success && (
                <div className="alert alert-success">
                    {success}
                </div>
            )}

            {/* Storage Stats */}
            <div className="storage-stats">
                <div className="stat-card-file">
                    <h3>Storage Usage</h3>
                    <div className="storage-bar">
                        <div 
                            className="storage-fill" 
                            style={{ width: `${(storage.used / storage.total) * 100}%` }}
                        />
                    </div>
                    <p className="storage-text">
                        {formatBytes(storage.used)} of {formatBytes(storage.total)} used
                    </p>
                </div>

                {networkStatus && (
                    <div className="stat-card-file">
                        <h3>Network Status</h3>
                        <div className="network-info">
                            <div className="network-stat">
                                <span className="label">Active Nodes:</span>
                                <span className="value">{nodes?.length || 0}</span>
                            </div>
                            <div className="network-stat">
                                <span className="label">Total Chunks:</span>
                                <span className="value">{networkStatus.totalChunks || 0}</span>
                            </div>
                        </div>
                    </div>
                )}
            </div>

            {/* Upload Section */}
            <div className="upload-section-new">
                <input
                    type="file"
                    id="file-upload"
                    onChange={handleUpload}
                    style={{ display: 'none' }}
                    disabled={loading}
                />
                <label htmlFor="file-upload" className="upload-box">
                    <Upload className="upload-icon-new" />
                    <h3>Upload Files</h3>
                    <p>Click to browse or drag and drop</p>
                    <small>Available space: {formatBytes(storage.total - storage.used)}</small>
                </label>
            </div>

            {/* Files Grid */}
            <div className="files-section-new">
                <div className="section-header-new">
                    <h2>My Files</h2>
                    <span className="file-count-badge">{files?.length || 0} files</span>
                </div>

                {loading && files?.length === 0 ? (
                    <div className="empty-state-new">
                        <Loader className="spinner-new" />
                        <p>Loading files...</p>
                    </div>
                ) : !files || files.length === 0 ? (
                    <div className="empty-state-new">
                        <File className="empty-icon-new" />
                        <h3>No files yet</h3>
                        <p>Upload your first file to get started!</p>
                    </div>
                ) : (
                    <div className="files-grid-new">
                        {files.map((file) => (
                            <div key={file.id} className="file-card-new">
                                <div className="file-card-header">
                                    <div className="file-icon-new">
                                        <File />
                                    </div>
                                    <div className="file-info-new">
                                        <h3 className="file-name-new" title={file.fileName}>
                                            {file.fileName}
                                        </h3>
                                        <p className="file-meta-new">
                                            {formatBytes(file.sizeBytes)} • {formatDate(file.createdAt)}
                                        </p>
                                    </div>
                                </div>

                                {file.storageNodeHint && (
                                    <div className="distribution-badge">
                                        <Network className="dist-icon-new" />
                                        <span>{file.storageNodeHint}</span>
                                    </div>
                                )}

                                <div className="file-actions-new">
                                    <button
                                        onClick={() => setExpandedFile(expandedFile === file.id ? null : file.id)}
                                        className="btn-file-action"
                                        title="View details"
                                    >
                                        {expandedFile === file.id ? <ChevronUp /> : <Info />}
                                    </button>
                                    <button
                                        onClick={() => handleDownload(file.id, file.fileName)}
                                        className="btn-file-action btn-download-new"
                                        title="Download"
                                    >
                                        <Download />
                                    </button>
                                    <button
                                        onClick={() => handleDelete(file.id, file.fileName)}
                                        className="btn-file-action btn-delete-new"
                                        title="Delete"
                                    >
                                        <Trash2 />
                                    </button>
                                </div>

                                {expandedFile === file.id && (
                                    <div className="file-details-new">
                                        <div className="detail-row-new">
                                            <span className="detail-label-new">File ID:</span>
                                            <span className="detail-value-new">{file.id}</span>
                                        </div>
                                        <div className="detail-row-new">
                                            <span className="detail-label-new">Content Type:</span>
                                            <span className="detail-value-new">{file.contentType || 'Unknown'}</span>
                                        </div>
                                        {file.storageNodeHint && (
                                            <div className="detail-row-new">
                                                <span className="detail-label-new">Distribution:</span>
                                                <span className="detail-value-new dist-highlight-new">
                                                    {file.storageNodeHint}
                                                </span>
                                            </div>
                                        )}
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Network Nodes Display */}
            {nodes && nodes.length > 0 && (
                <div className="nodes-display">
                    <h3>Active Storage Nodes</h3>
                    <div className="nodes-grid-small">
                        {nodes.map((node) => (
                            <div key={node.nodeId} className="node-chip">
                                <Server className="node-chip-icon" />
                                <span>{node.nodeId}</span>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}
