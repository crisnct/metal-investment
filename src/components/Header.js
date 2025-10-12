import React, { useState } from 'react';
import { TrendingUp, Menu, X, User, LogIn, LogOut } from 'lucide-react';
import './Header.css';

const Header = ({ isLoggedIn, onLogin, onLogout, onLoadProfit, profitData }) => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [showLoginForm, setShowLoginForm] = useState(false);
  const [loginData, setLoginData] = useState({ username: '', password: '' });
  const [loginMessage, setLoginMessage] = useState('');

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    const result = await onLogin(loginData.username, loginData.password);
    setLoginMessage(result.message);
    if (result.success) {
      setShowLoginForm(false);
      setLoginData({ username: '', password: '' });
    }
  };

  const handleLogout = () => {
    onLogout();
    setShowLoginForm(false);
  };

  return (
    <header className="header">
      <div className="container">
        <div className="header-content">
          <div className="logo">
            <TrendingUp className="logo-icon" />
            <span className="logo-text">Metal Investment</span>
          </div>
          
          <nav className={`nav ${isMenuOpen ? 'nav-open' : ''}`}>
            <a href="#home" className="nav-link">Home</a>
            <a href="#features" className="nav-link">Features</a>
            <a href="#tech" className="nav-link">Technology</a>
          </nav>

          <div className="header-actions">
            {isLoggedIn ? (
              <>
                <button className="btn-secondary" onClick={onLoadProfit}>
                  <TrendingUp className="icon" />
                  View Profit
                </button>
                <button className="btn-primary" onClick={handleLogout}>
                  <LogOut className="icon" />
                  Logout
                </button>
              </>
            ) : (
              <>
                <button className="btn-secondary">
                  <User className="icon" />
                  Sign Up
                </button>
                <button className="btn-primary" onClick={() => setShowLoginForm(true)}>
                  <LogIn className="icon" />
                  Login
                </button>
              </>
            )}
          </div>
          
          <button 
            className="menu-toggle"
            onClick={() => setIsMenuOpen(!isMenuOpen)}
            aria-label="Toggle menu"
          >
            {isMenuOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>
      </div>

      {/* Login Modal */}
      {showLoginForm && (
        <div className="modal-overlay" onClick={() => setShowLoginForm(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Login to Metal Investment</h2>
              <button className="modal-close" onClick={() => setShowLoginForm(false)}>
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleLoginSubmit} className="login-form">
              <div className="form-group">
                <label htmlFor="username">Username</label>
                <input
                  type="text"
                  id="username"
                  value={loginData.username}
                  onChange={(e) => setLoginData({ ...loginData, username: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label htmlFor="password">Password</label>
                <input
                  type="password"
                  id="password"
                  value={loginData.password}
                  onChange={(e) => setLoginData({ ...loginData, password: e.target.value })}
                  required
                />
              </div>
              {loginMessage && (
                <div className={`message ${loginMessage.includes('successful') ? 'success' : 'error'}`}>
                  {loginMessage}
                </div>
              )}
              <button type="submit" className="btn-primary full-width">
                Login
              </button>
            </form>
          </div>
        </div>
      )}

      {/* Profit Data Display */}
      {profitData && (
        <div className="profit-display">
          <h3>Your Profit Information</h3>
          <p>Username: {profitData.username}</p>
          <p>Last Updated: {new Date(profitData.time).toLocaleString()}</p>
          {profitData.metalInfo && profitData.metalInfo.length > 0 ? (
            <div className="metal-info">
              {profitData.metalInfo.map((metal, index) => (
                <div key={index} className="metal-item">
                  <strong>{metal.metalSymbol}</strong>: {metal.amount} units
                  <br />
                  Profit: {metal.profit} ({metal.profitPercentage}%)
                </div>
              ))}
            </div>
          ) : (
            <p>No metal investments recorded yet.</p>
          )}
        </div>
      )}
    </header>
  );
};

export default Header;
