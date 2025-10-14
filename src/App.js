import React, { useState, useEffect } from 'react';
import './App.css';
import Header from './components/Header';
import Hero from './components/Hero';
import Features from './components/Features';
import TechnicalStack from './components/TechnicalStack';
import Profile from './components/Profile';
import Footer from './components/Footer';
import ApiService from './services/api';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userToken, setUserToken] = useState(null);
  const [userInfo, setUserInfo] = useState({ username: '', email: '' });

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

  const handleLogout = () => {
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
      <Hero />
      {isLoggedIn && <Profile userInfo={userInfo} />}
      <Features />
      <TechnicalStack />
      <Footer />
    </div>
  );
}

export default App;
