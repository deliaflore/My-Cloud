import React from 'react';
import { Users, Heart, MessageCircle, User } from 'lucide-react';
import './RoommateMatching.css';

export default function RoommateMatching() {
    const matches = [
        { id: 1, name: 'John Doe', compatibility: 92, budget: '$60-90', habits: 'Night owl 🌙', cleanliness: 'Very organized' },
        { id: 2, name: 'Jane Smith', compatibility: 88, budget: '$50-80', habits: 'Early bird 🌅', cleanliness: 'Moderate' },
        { id: 3, name: 'Mike Johnson', compatibility: 85, budget: '$70-100', habits: 'Flexible', cleanliness: 'Very organized' },
    ];

    return (
        <div className="roommate-matching">
            <h1 className="page-title">Find Your Perfect Roommate</h1>
            <p className="page-subtitle">Match with compatible people based on your preferences</p>

            <div className="profile-section">
                <h2>Your Profile</h2>
                <div className="profile-card">
                    <div className="profile-item">
                        <strong>Budget:</strong> $50-100/month
                    </div>
                    <div className="profile-item">
                        <strong>Study Habits:</strong> Night owl 🌙
                    </div>
                    <div className="profile-item">
                        <strong>Cleanliness:</strong> Very organized
                    </div>
                    <div className="profile-item">
                        <strong>Lifestyle:</strong> Quiet, non-smoker
                    </div>
                    <button className="btn-edit">Edit Profile</button>
                </div>
            </div>

            <div className="matches-section">
                <h2>Top Matches</h2>
                <div className="matches-grid">
                    {matches.map(match => (
                        <div key={match.id} className="match-card">
                            <div className="match-avatar">
                                <User />
                            </div>
                            <h3>{match.name}</h3>
                            <div className="compatibility-score">
                                <Heart className="heart-icon" />
                                {match.compatibility}% Compatible
                            </div>
                            <div className="match-details">
                                <p><strong>Budget:</strong> {match.budget}</p>
                                <p><strong>Habits:</strong> {match.habits}</p>
                                <p><strong>Cleanliness:</strong> {match.cleanliness}</p>
                            </div>
                            <div className="match-actions">
                                <button className="btn-connect">Connect</button>
                                <button className="btn-message">
                                    <MessageCircle />
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}
