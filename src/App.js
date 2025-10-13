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

  useEffect(() => {
    // Check if user is logged in (check localStorage for token)
    const token = localStorage.getItem('userToken');
    if (token) {
      setUserToken(token);
      setIsLoggedIn(true);
    }
  }, []);

  const handleLogin = async (username, password) => {
    try {
      const response = await ApiService.login(username, password);
      if (response.token) {
        localStorage.setItem('userToken', response.token);
        setUserToken(response.token);
        setIsLoggedIn(true);
        return { success: true, message: 'Login successful!' };
      }
    } catch (error) {
      return { success: false, message: 'Login failed. Please check your credentials.' };
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('userToken');
    setUserToken(null);
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
      {isLoggedIn && <Profile />}
      <Features />
      <TechnicalStack />
      <Footer />
    </div>
  );
}

export default App;
