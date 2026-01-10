import React, { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { 
    Mail, Lock, User, Eye, EyeOff, Loader, CheckCircle, 
    AlertCircle, Folder
} from 'lucide-react';

// Layout
import Layout from './components/Layout/Layout';

// Pages
import Dashboard from './components/Dashboard/Dashboard';
import HousingMarketplace from './components/Housing/HousingMarketplace';
import RoommateMatching from './components/Roommates/RoommateMatching';
import MyHousing from './components/MyHousing/MyHousing';
import NetworkStatus from './components/Admin/NetworkStatus';
import FileManager from './components/FileStorage/FileManager';
import CloudDashboard from './components/CloudStorage/CloudDashboard';
import ModularDashboard from './components/ModularDashboard';

import './App.css';

const API_BASE = 'http://localhost:8081/api';

export default function MyCloudApp() {
    // Auth state
    const [authState, setAuthState] = useState('login');
    const [token, setToken] = useState(localStorage.getItem('token'));
    const [user, setUser] = useState(null);
    
    // Data state
    const [files, setFiles] = useState([]);
    const [storage, setStorage] = useState({ used: 0, total: 1073741824 });
    const [networkStatus, setNetworkStatus] = useState(null);
    const [nodes, setNodes] = useState([]);
    const [runningNodes, setRunningNodes] = useState([]);
    
    // UI state
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const [showPassword, setShowPassword] = useState(false);
    
    // Form states
    const [loginForm, setLoginForm] = useState({ email: '', password: '' });
    const [registerForm, setRegisterForm] = useState({ fullName: '', email: '', password: '' });
    const [otpForm, setOtpForm] = useState({ email: '', code: '' });

    useEffect(() => {
        if (token) {
            setAuthState('authenticated');
            fetchUserData();
            fetchNetworkStatus();
            const interval = setInterval(fetchNetworkStatus, 5000);
            return () => clearInterval(interval);
        }
    }, [token]);

    const fetchUserData = async () => {
        try {
            await Promise.all([fetchFiles(), fetchStorage()]);
        } catch (err) {
            console.error('Failed to fetch user data:', err);
        }
    };

    const fetchFiles = async () => {
        try {
            const response = await fetch(`${API_BASE}/files`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (response.ok) {
                const data = await response.json();
                setFiles(data);
            }
        } catch (err) {
            console.error('Failed to fetch files:', err);
        }
    };

    const fetchStorage = async () => {
        setStorage({ used: 0, total: 1073741824 });
    };

    const fetchNetworkStatus = async () => {
        try {
            const [statusRes, nodesRes, runningRes] = await Promise.all([
                fetch(`${API_BASE}/network/status`),
                fetch(`${API_BASE}/network/nodes`),
                fetch(`${API_BASE}/network/nodes/running`)
            ]);
            
            if (statusRes.ok) setNetworkStatus(await statusRes.json());
            if (nodesRes.ok) setNodes(await nodesRes.json());
            if (runningRes.ok) {
                const data = await runningRes.json();
                setRunningNodes(Array.isArray(data) ? data : data?.runningNodes || []);
            }
        } catch (err) {
            console.error('Failed to fetch network status:', err);
        }
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            const response = await fetch(`${API_BASE}/auth/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(registerForm)
            });

            const data = await response.json();

            if (response.ok) {
                setSuccess('Registration successful! Check your email for OTP code.');
                setOtpForm({ ...otpForm, email: registerForm.email });
                setAuthState('otp');
            } else {
                setError(data.error || 'Registration failed');
            }
        } catch (err) {
            setError('Network error. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const handleLogin = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            const response = await fetch(`${API_BASE}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(loginForm)
            });

            const data = await response.json();

            if (response.ok) {
                setSuccess('OTP sent to your email!');
                setOtpForm({ ...otpForm, email: loginForm.email });
                setAuthState('otp');
            } else {
                setError(data.error || 'Login failed');
            }
        } catch (err) {
            setError('Network error. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const handleVerifyOtp = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            const response = await fetch(`${API_BASE}/auth/verify-otp`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(otpForm)
            });

            const data = await response.json();

            if (response.ok && data.token) {
                setToken(data.token);
                localStorage.setItem('token', data.token);
                setAuthState('authenticated');
                setSuccess('Login successful!');
                setTimeout(() => setSuccess(null), 3000);
                await fetchUserData();
            } else {
                setError(data.error || 'Invalid OTP code');
            }
        } catch (err) {
            setError('Network error. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const handleLogout = () => {
        setToken(null);
        localStorage.removeItem('token');
        setAuthState('login');
        setUser(null);
        setFiles([]);
    };

    // Auth Views
    if (authState === 'login') {
        return (
            <div className="auth-container">
                <div className="auth-card">
                    <div className="auth-header">
                        <img
                            src="/mycloud.png"
                            alt="MyCloud"
                            className="auth-logo"
                            style={{ width: "200px", height: "200px" }}
                            />

                        <p>Agricultural Data and Resource Management System</p>
                    </div>

                    {error && (
                        <div className="alert alert-error">
                            <AlertCircle className="alert-icon" />
                            {error}
                        </div>
                    )}

                    <form onSubmit={handleLogin} className="auth-form">
                        <div className="form-group">
                            <Mail className="input-icon" />
                            <input
                                type="email"
                                placeholder="Email address"
                                value={loginForm.email}
                                onChange={(e) => setLoginForm({ ...loginForm, email: e.target.value })}
                                required
                            />
                        </div>

                        <div className="form-group">
                            <Lock className="input-icon" />
                            <input
                                type={showPassword ? "text" : "password"}
                                placeholder="Password"
                                value={loginForm.password}
                                onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })}
                                required
                            />
                            <button
                                type="button"
                                className="password-toggle"
                                onClick={() => setShowPassword(!showPassword)}
                            >
                                {showPassword ? <EyeOff /> : <Eye />}
                            </button>
                        </div>

                        <button type="submit" className="btn btn-primary" disabled={loading}>
                            {loading ? <Loader className="spinner" /> : 'Sign In'}
                        </button>
                    </form>

                    <div className="auth-footer">
                        <p>Don't have an account? <button onClick={() => setAuthState('register')} className="link">Sign up</button></p>
                    </div>
                </div>
            </div>
        );
    }

    if (authState === 'register') {
        return (
            <div className="auth-container">
                <div className="auth-card">
                    <div className="auth-header">
                        <img
                            src="/mycloud.png"
                            alt="MyCloud"
                            className="auth-logo"
                            style={{ width: "200px", height: "200px" }}
                            />
                        <h1>Create Account</h1>
                        <p>Join MyCloud today</p>
                    </div>

                    {error && (
                        <div className="alert alert-error">
                            <AlertCircle className="alert-icon" />
                            {error}
                        </div>
                    )}

                    <form onSubmit={handleRegister} className="auth-form">
                        <div className="form-group">
                            <User className="input-icon" />
                            <input
                                type="text"
                                placeholder="Full name"
                                value={registerForm.fullName}
                                onChange={(e) => setRegisterForm({ ...registerForm, fullName: e.target.value })}
                                required
                            />
                        </div>

                        <div className="form-group">
                            <Mail className="input-icon" />
                            <input
                                type="email"
                                placeholder="Email address"
                                value={registerForm.email}
                                onChange={(e) => setRegisterForm({ ...registerForm, email: e.target.value })}
                                required
                            />
                        </div>

                        <div className="form-group">
                            <Lock className="input-icon" />
                            <input
                                type={showPassword ? "text" : "password"}
                                placeholder="Password"
                                value={registerForm.password}
                                onChange={(e) => setRegisterForm({ ...registerForm, password: e.target.value })}
                                required
                            />
                            <button
                                type="button"
                                className="password-toggle"
                                onClick={() => setShowPassword(!showPassword)}
                            >
                                {showPassword ? <EyeOff /> : <Eye />}
                            </button>
                        </div>

                        <button type="submit" className="btn btn-primary" disabled={loading}>
                            {loading ? <Loader className="spinner" /> : 'Create Account'}
                        </button>
                    </form>

                    <div className="auth-footer">
                        <p>Already have an account? <button onClick={() => setAuthState('login')} className="link">Sign in</button></p>
                    </div>
                </div>
            </div>
        );
    }

    if (authState === 'otp') {
        return (
            <div className="auth-container">
                <div className="auth-card">
                    <div className="auth-header">
                        <Mail className="auth-logo" />
                        <h1>Verify Email</h1>
                        <p>Enter the code sent to {otpForm.email}</p>
                    </div>

                    {error && (
                        <div className="alert alert-error">
                            <AlertCircle className="alert-icon" />
                            {error}
                        </div>
                    )}

                    {success && (
                        <div className="alert alert-success">
                            <CheckCircle className="alert-icon" />
                            {success}
                        </div>
                    )}

                    <form onSubmit={handleVerifyOtp} className="auth-form">
                        <div className="form-group otp-group">
                            <input
                                type="text"
                                placeholder="Enter 6-digit code"
                                value={otpForm.code}
                                onChange={(e) => setOtpForm({ ...otpForm, code: e.target.value.replace(/\D/g, '').slice(0, 6) })}
                                maxLength={6}
                                required
                                className="otp-input"
                            />
                        </div>

                        <button type="submit" className="btn btn-primary" disabled={loading || otpForm.code.length !== 6}>
                            {loading ? <Loader className="spinner" /> : 'Verify Code'}
                        </button>
                    </form>

                    <div className="auth-footer">
                        <p>Didn't receive code? <button onClick={() => setAuthState('login')} className="link">Go back</button></p>
                    </div>
                </div>
            </div>
        );
    }

    // Main App (Authenticated)
    return (
        <BrowserRouter>
            <Layout 
                user={user} 
                storage={storage} 
                networkStatus={networkStatus} 
                onLogout={handleLogout}
                userRole="student"
            >
                <Routes>
                    <Route path="/" element={
                        <Dashboard 
                            files={files} 
                            networkStatus={networkStatus} 
                            storage={storage} 
                        />
                    } />
                    <Route path="/housing" element={<HousingMarketplace />} />
                    <Route path="/roommates" element={<RoommateMatching />} />
                    <Route path="/my-housing" element={<MyHousing />} />
                    <Route path="/files" element={<CloudDashboard />} />
                    <Route path="/cloud-storage" element={<CloudDashboard />} />
                    <Route path="/storage-admin" element={<CloudDashboard />} />
                    <Route path="/network" element={
                        <NetworkStatus 
                            nodes={nodes} 
                            networkStatus={networkStatus} 
                        />
                    } />
                    <Route path="/admin" element={<div><h1>Admin Panel (Coming Soon)</h1></div>} />
                    <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
            </Layout>
        </BrowserRouter>
    );
}
