import React, { useState, useEffect } from 'react';
import { Gift, HardDrive, CheckCircle, X } from 'lucide-react';
import './WelcomeMessage.css';

/**
 * Welcome Message Component
 * Shows when user first logs in
 * Displays storage entitlement and welcome message
 */
export default function WelcomeMessage({ user, quota, onClose }) {
    const [show, setShow] = useState(false);

    useEffect(() => {
        // Check if user has seen welcome message (use email as key)
        const userEmail = user?.email || user?.name;
        if (!userEmail) return;
        
        const welcomeKey = `hasSeenWelcome_${userEmail}`;
        const hasSeenWelcome = localStorage.getItem(welcomeKey);
        
        if (!hasSeenWelcome) {
            setShow(true);
            localStorage.setItem(welcomeKey, 'true');
        }
    }, [user]);

    const handleClose = () => {
        setShow(false);
        if (onClose) onClose();
    };

    if (!show) return null;

    const quotaGB = quota ? (quota / (1024 * 1024 * 1024)).toFixed(0) : '2';

    return (
        <div className="welcome-overlay">
            <div className="welcome-modal">
                <button className="welcome-close" onClick={handleClose}>
                    <X size={24} />
                </button>

                <div className="welcome-header">
                    <div className="welcome-icon">
                        <Gift size={48} />
                    </div>
                    <h1>Welcome to STUMatch Cloud Storage!</h1>
                    <p className="welcome-subtitle">
                        Thank you for enrolling, {user?.name || user?.email || 'Student'}!
                    </p>
                </div>

                <div className="welcome-content">
                    <div className="entitlement-card">
                        <div className="entitlement-icon">
                            <HardDrive size={40} />
                        </div>
                        <div className="entitlement-details">
                            <h2>Your Free Storage Allocation</h2>
                            <div className="storage-amount">
                                <span className="amount">{quotaGB} GB</span>
                                <span className="label">Free Tier</span>
                            </div>
                            <p className="entitlement-description">
                                You are entitled to <strong>{quotaGB} GB</strong> of secure, distributed cloud storage 
                                absolutely free! Upload your documents, photos, and files with confidence.
                            </p>
                        </div>
                    </div>

                    <div className="features-grid">
                        <div className="feature-item">
                            <CheckCircle size={20} />
                            <div>
                                <strong>Fault Tolerant</strong>
                                <p>Files replicated 2x for safety</p>
                            </div>
                        </div>
                        <div className="feature-item">
                            <CheckCircle size={20} />
                            <div>
                                <strong>Fast Distribution</strong>
                                <p>Chunks spread across nodes</p>
                            </div>
                        </div>
                        <div className="feature-item">
                            <CheckCircle size={20} />
                            <div>
                                <strong>Secure Access</strong>
                                <p>JWT authentication protected</p>
                            </div>
                        </div>
                        <div className="feature-item">
                            <CheckCircle size={20} />
                            <div>
                                <strong>Easy Management</strong>
                                <p>Upload, download, delete anytime</p>
                            </div>
                        </div>
                    </div>

                    <div className="welcome-cta">
                        <button className="btn-primary" onClick={handleClose}>
                            Get Started →
                        </button>
                        <p className="cta-note">
                            Your storage is ready to use. Upload your first file to get started!
                        </p>
                    </div>
                </div>

                <div className="welcome-footer">
                    <p>Need more storage? Contact your administrator.</p>
                </div>
            </div>
        </div>
    );
}
