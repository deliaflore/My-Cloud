import React, { useState, useEffect } from 'react';
import { Server, Database, CheckCircle, ArrowRight } from 'lucide-react';
import './ChunkDistribution.css';

export default function ChunkDistribution({ distribution, fileName }) {
    const [animating, setAnimating] = useState(false);
    const [completedChunks, setCompletedChunks] = useState([]);

    useEffect(() => {
        if (distribution && distribution.length > 0) {
            setAnimating(true);
            setCompletedChunks([]);

            // Animate chunks one by one
            distribution.forEach((chunk, index) => {
                setTimeout(() => {
                    setCompletedChunks(prev => [...prev, index]);
                }, (index + 1) * 500); // 500ms delay between each chunk
            });

            // Stop animation after all chunks
            setTimeout(() => {
                setAnimating(false);
            }, distribution.length * 500 + 1000);
        }
    }, [distribution]);

    if (!distribution || distribution.length === 0) {
        return null;
    }

    const totalChunks = distribution.length;
    const nodeMap = {};
    
    // Group chunks by node
    distribution.forEach((chunk, index) => {
        if (!nodeMap[chunk.nodeId]) {
            nodeMap[chunk.nodeId] = [];
        }
        nodeMap[chunk.nodeId].push({ ...chunk, index });
    });

    const nodes = Object.keys(nodeMap);

    return (
        <div className="chunk-distribution">
            <div className="distribution-header">
                <h3>File Distribution</h3>
                <p className="distribution-subtitle">
                    <strong>{fileName}</strong> → <strong>{totalChunks} file chunks</strong> distributed across <strong>{nodes.length} active node{nodes.length > 1 ? 's' : ''}</strong>
                </p>
            </div>

            <div className="distribution-visual">
                {/* File Source */}
                <div className="file-source">
                    <Database size={32} />
                    <span className="file-name">{fileName}</span>
                    <div className="chunk-count">{totalChunks} chunks</div>
                </div>

                {/* Distribution Flow */}
                <div className="distribution-flow">
                    {distribution.map((chunk, index) => (
                        <div 
                            key={index}
                            className={`chunk-flow ${completedChunks.includes(index) ? 'completed' : ''}`}
                            style={{ animationDelay: `${index * 0.5}s` }}
                        >
                            <div className="chunk-particle">
                                <span>Chunk {chunk.chunkIndex + 1}</span>
                            </div>
                            <ArrowRight className="flow-arrow" size={20} />
                        </div>
                    ))}
                </div>

                {/* Target Nodes */}
                <div className="target-nodes">
                    {nodes.map(nodeId => {
                        const nodeChunks = nodeMap[nodeId];
                        const nodeCompleted = nodeChunks.every(chunk => 
                            completedChunks.includes(chunk.index)
                        );

                        return (
                            <div 
                                key={nodeId} 
                                className={`node-target ${nodeCompleted ? 'completed' : ''}`}
                            >
                                <div className="node-icon">
                                    <Server size={24} />
                                    {nodeCompleted && (
                                        <CheckCircle 
                                            className="check-icon" 
                                            size={16} 
                                        />
                                    )}
                                </div>
                                <div className="node-info">
                                    <h4>{nodeId}</h4>
                                    <div className="node-chunks">
                                        {nodeChunks.map(chunk => (
                                            <div 
                                                key={chunk.index}
                                                className={`mini-chunk ${completedChunks.includes(chunk.index) ? 'stored' : ''}`}
                                            >
                                                {chunk.chunkIndex + 1}
                                            </div>
                                        ))}
                                    </div>
                                    <p className="chunk-summary">
                                        {nodeChunks.length} chunk{nodeChunks.length > 1 ? 's' : ''}
                                    </p>
                                </div>
                            </div>
                        );
                    })}
                </div>
            </div>

            {/* Statistics */}
            <div className="distribution-stats">
                <div className="stat">
                    <span className="stat-label">File Chunks</span>
                    <span className="stat-value">{totalChunks} pieces</span>
                </div>
                <div className="stat">
                    <span className="stat-label">Active Nodes</span>
                    <span className="stat-value">{nodes.length} node{nodes.length > 1 ? 's' : ''}</span>
                </div>
                <div className="stat">
                    <span className="stat-label">Replication</span>
                    <span className="stat-value">
                        {(totalChunks / nodes.length).toFixed(1)}x
                    </span>
                </div>
                <div className="stat">
                    <span className="stat-label">Distribution</span>
                    <span className="stat-value status-complete">
                        {completedChunks.length === totalChunks ? '✓ Complete' : `${completedChunks.length}/${totalChunks}`}
                    </span>
                </div>
            </div>
        </div>
    );
}
