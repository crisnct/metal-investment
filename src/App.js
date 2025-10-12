import React, { useState, useEffect } from 'react';
import './App.css';
import Header from './components/Header';
import Hero from './components/Hero';
import TechnicalStack from './components/TechnicalStack';
import Footer from './components/Footer';
import ApiService from './services/api';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userToken, setUserToken] = useState(null);
  const [profitData, setProfitData] = useState(null);

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
    setProfitData(null);
  };

  const loadProfitData = async () => {
    if (userToken) {
      try {
        const data = await ApiService.getProfit(userToken);
        setProfitData(data);
      } catch (error) {
        console.error('Failed to load profit data:', error);
      }
    }
  };

  return (
    <div className="App">
      <Header 
        isLoggedIn={isLoggedIn} 
        onLogin={handleLogin} 
        onLogout={handleLogout}
        onLoadProfit={loadProfitData}
        profitData={profitData}
      />
      <Hero />
      <TechnicalStack />
      <Footer />
    </div>
  );
}

export default App;
