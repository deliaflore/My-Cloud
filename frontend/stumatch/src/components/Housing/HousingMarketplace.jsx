import React, { useState } from 'react';
import { 
    Building2, 
    MapPin, 
    DollarSign, 
    Bed, 
    CheckCircle,
    Search,
    Filter,
    Star
} from 'lucide-react';
import './HousingMarketplace.css';

export default function HousingMarketplace() {
    const [searchTerm, setSearchTerm] = useState('');
    const [priceRange, setPriceRange] = useState([0, 500]);
    const [bedrooms, setBedrooms] = useState('any');

    // Mock data - will be replaced with API calls
    const listings = [
        {
            id: 1,
            title: '2-Bedroom Apartment',
            price: 120,
            location: 'Near ICT Campus',
            distance: '0.5 km',
            bedrooms: 2,
            verified: true,
            landlord: 'John Smith',
            rating: 4.5,
            image: null,
            description: 'Spacious apartment with modern amenities'
        },
        {
            id: 2,
            title: 'Studio Apartment',
            price: 80,
            location: 'Downtown',
            distance: '1.2 km',
            bedrooms: 1,
            verified: true,
            landlord: 'Mary Johnson',
            rating: 4.8,
            image: null,
            description: 'Cozy studio perfect for students'
        },
        {
            id: 3,
            title: '3-Bedroom House',
            price: 150,
            location: 'Residential Area',
            distance: '0.8 km',
            bedrooms: 3,
            verified: false,
            landlord: 'David Brown',
            rating: 4.2,
            image: null,
            description: 'Spacious house ideal for sharing'
        },
        {
            id: 4,
            title: 'Shared Room',
            price: 45,
            location: 'Campus Housing',
            distance: '0.2 km',
            bedrooms: 1,
            verified: true,
            landlord: 'University Housing',
            rating: 4.6,
            image: null,
            description: 'Affordable shared accommodation'
        }
    ];

    return (
        <div className="housing-marketplace">
            <div className="marketplace-header">
                <div>
                    <h1 className="page-title">Housing Marketplace</h1>
                    <p className="page-subtitle">Find your perfect student accommodation</p>
                </div>
                <button className="btn-primary">
                    <Building2 className="btn-icon" />
                    Post Listing
                </button>
            </div>

            {/* Search and Filters */}
            <div className="search-section">
                <div className="search-bar">
                    <Search className="search-icon" />
                    <input
                        type="text"
                        placeholder="Search by location, price, or features..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="search-input"
                    />
                </div>

                <div className="filters">
                    <div className="filter-group">
                        <label className="filter-label">Price Range</label>
                        <select className="filter-select">
                            <option>0 - 1000FCFA</option>
                            <option>100 - 2000FCFA</option>
                            <option>2000 -5000FCFA</option>
                        </select>
                    </div>

                    <div className="filter-group">
                        <label className="filter-label">Bedrooms</label>
                        <select 
                            className="filter-select"
                            value={bedrooms}
                            onChange={(e) => setBedrooms(e.target.value)}
                        >
                            <option value="any">Any</option>
                            <option value="1">1 Bedroom</option>
                            <option value="2">2 Bedrooms</option>
                            <option value="3">3+ Bedrooms</option>
                        </select>
                    </div>

                    <div className="filter-group">
                        <label className="filter-label">Distance</label>
                        <select className="filter-select">
                            <option>Within 1 km</option>
                            <option>Within 2 km</option>
                            <option>Within 5 km</option>
                        </select>
                    </div>

                    <button className="filter-btn">
                        <Filter className="btn-icon" />
                        More Filters
                    </button>
                </div>
            </div>

            {/* Listings Grid */}
            <div className="listings-grid">
                {listings.map((listing) => (
                    <div key={listing.id} className="listing-card">
                        <div className="listing-image">
                            <Building2 className="placeholder-icon" />
                            {listing.verified && (
                                <div className="verified-badge">
                                    <CheckCircle className="verified-icon" />
                                    Verified
                                </div>
                            )}
                        </div>

                        <div className="listing-content">
                            <div className="listing-header">
                                <h3 className="listing-title">{listing.title}</h3>
                                <div className="listing-rating">
                                    <Star className="star-icon filled" />
                                    <span>{listing.rating}</span>
                                </div>
                            </div>

                            <p className="listing-description">{listing.description}</p>

                            <div className="listing-details">
                                <div className="detail-item">
                                    <MapPin className="detail-icon" />
                                    <span>{listing.location} • {listing.distance}</span>
                                </div>
                                <div className="detail-item">
                                    <Bed className="detail-icon" />
                                    <span>{listing.bedrooms} {listing.bedrooms === 1 ? 'Bedroom' : 'Bedrooms'}</span>
                                </div>
                            </div>

                            <div className="listing-footer">
                                <div className="listing-price">
                                    <DollarSign className="price-icon" />
                                    <span className="price-amount">{listing.price}</span>
                                    <span className="price-period">/month</span>
                                </div>
                                <button className="btn-view">View Details</button>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}
