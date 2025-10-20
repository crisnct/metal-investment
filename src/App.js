import React, { useState, useEffect } from 'react';
import './App.css';
import Header from './components/Header';
import Hero from './components/Hero';
import Features from './components/Features';
import TechnicalStack from './components/TechnicalStack';
import DevOps from './components/DevOps';
import PartnerSystems from './components/PartnerSystems';
import Profile from './components/Profile';
import Footer from './components/Footer';
import ApiService from './services/api';

const DB_DOWN_MESSAGE = 'The database has been temporarily shut down by the platform owner for cost-saving purposes. Sign-up and login functionality are currently unavailable. If you would like to test the platform, please reach out to the owner at hello@cristiantone.me.';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userToken, setUserToken] = useState(null);
  const [userInfo, setUserInfo] = useState({ username: '', email: '' });
  const [bannerMessage, setBannerMessage] = useState('');

  useEffect(() => {
    // Check if user is logged in (check localStorage for token and user info)
    const token = localStorage.getItem('userToken');
    const storedUsername = localStorage.getItem('userUsername');
    const storedEmail = localStorage.getItem('userEmail');
    
    if (token) {
      setUserToken(token);
      setIsLoggedIn(true);
      setUserInfo({ 
        username: storedUsername || '', 
        email: storedEmail || '' 
      });
    }

    let isMounted = true;

    const checkDatabaseStatus = async () => {
      try {
        const health = await ApiService.getHealthStatus();
        if (!isMounted) {
          return;
        }
        const dbStatus = health && typeof health.database === 'string'
          ? health.database.toUpperCase()
          : 'UNKNOWN';
        if (dbStatus !== 'UP') {
          setBannerMessage(DB_DOWN_MESSAGE);
        } else {
          setBannerMessage('');
        }
      } catch (error) {
        console.error('Failed to evaluate database status from health endpoint:', error);
        if (isMounted) {
          setBannerMessage(DB_DOWN_MESSAGE);
        }
      }
    };

    checkDatabaseStatus();

    return () => {
      isMounted = false;
    };
  }, []);

  const handleLogin = async (username, password) => {
    try {
      const response = await ApiService.login(username, password);
      if (response.token) {
        // Store token and user details in localStorage
        localStorage.setItem('userToken', response.token);
        localStorage.setItem('userUsername', response.username || '');
        localStorage.setItem('userEmail', response.email || '');
        
        setUserToken(response.token);
        setUserInfo({ 
          username: response.username || '', 
          email: response.email || '' 
        });
        setIsLoggedIn(true);
        return { success: true, message: 'Login successful!' };
      }
    } catch (error) {
      return { success: false, message: 'Login failed. Please check your credentials.' };
    }
  };

  const handleLogout = async () => {
    try {
      // Call the logout API to properly invalidate all sessions
      await ApiService.logout();
    } catch (error) {
      console.error('Logout API call failed:', error);
      // Continue with local logout even if API call fails
    }
    
    // Clear all user data from localStorage
    localStorage.removeItem('userToken');
    localStorage.removeItem('userUsername');
    localStorage.removeItem('userEmail');
    
    setUserToken(null);
    setUserInfo({ username: '', email: '' });
    setIsLoggedIn(false);
  };


  return (
    <div className="App">
      <Header 
        isLoggedIn={isLoggedIn} 
        onLogin={handleLogin} 
        onLogout={handleLogout}
      />
      {bannerMessage && (
        <div className="app-banner">
          {bannerMessage}
        </div>
      )}
      <Hero />
      {isLoggedIn && <Profile userInfo={userInfo} />}
      <Features />
      <TechnicalStack />
      <DevOps />
      <PartnerSystems />
      <Footer />
    </div>
  );
}

export default App;
