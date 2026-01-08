import React from 'react';
import { AlertTriangle, X } from 'lucide-react';
import './ConfirmModal.css';

export default function ConfirmModal({ isOpen, title, message, onConfirm, onCancel, danger = false }) {
    if (!isOpen) return null;

    return (
        <div className="modal-overlay" onClick={onCancel}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <div className={`modal-icon ${danger ? 'danger' : ''}`}>
                        <AlertTriangle size={24} />
                    </div>
                    <button className="modal-close" onClick={onCancel}>
                        <X size={20} />
                    </button>
                </div>

                <div className="modal-body">
                    <h3>{title}</h3>
                    <p>{message}</p>
                </div>

                <div className="modal-footer">
                    <button className="btn-cancel" onClick={onCancel}>
                        Cancel
                    </button>
                    <button 
                        className={`btn-confirm ${danger ? 'danger' : ''}`} 
                        onClick={() => {
                            onConfirm();
                            onCancel();
                        }}
                    >
                        {danger ? 'Delete' : 'Confirm'}
                    </button>
                </div>
            </div>
        </div>
    );
}
