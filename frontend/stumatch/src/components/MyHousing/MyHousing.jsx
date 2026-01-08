import React from 'react';
import { Home, Users, FileText, DollarSign } from 'lucide-react';
import './MyHousing.css';

export default function MyHousing() {
    return (
        <div className="my-housing">
            <h1 className="page-title">My Housing</h1>
            <p className="page-subtitle">Manage your current accommodation</p>

            <div className="housing-info">
                <h2>Current Housing</h2>
                <div className="info-card">
                    <p><strong>Address:</strong> 123 Campus Road, Apt 2B</p>
                    <p><strong>Rent:</strong> 12000FCFA/month (split 2 ways = 6000FCFA each)</p>
                    <p><strong>Lease:</strong> Jan 2025 - Dec 2025</p>
                </div>
            </div>

            <div className="roommates-section">
                <h2>Roommates</h2>
                <div className="roommates-list">
                    <div className="roommate-item">You</div>
                    <div className="roommate-item">Jane Smith</div>
                </div>
            </div>

            <div className="documents-section">
                <h2>Shared Documents</h2>
                <div className="documents-list">
                    <div className="document-item">
                        <FileText /> Lease Agreement.pdf
                    </div>
                    <div className="document-item">
                        <FileText /> Utility Bill - Nov.pdf
                    </div>
                </div>
                <button className="btn-upload">Upload Document</button>
            </div>

            <div className="bills-section">
                <h2>Bill Splitting</h2>
                <div className="bills-list">
                    <div className="bill-item">
                        <span>Rent: $60 each</span>
                        <span className="status-paid">✓ Paid</span>
                    </div>
                    <div className="bill-item">
                        <span>Utilities: $25 each</span>
                        <span className="status-pending">⏳ Pending</span>
                    </div>
                </div>
            </div>
        </div>
    );
}
