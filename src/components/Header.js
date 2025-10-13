import React, { useState, useEffect } from 'react';
import { TrendingUp, Menu, X, User, LogIn, LogOut } from 'lucide-react';
import './Header.css';
import ApiService from '../services/api';

const Header = ({ isLoggedIn, onLogin, onLogout, onLoadProfit, profitData }) => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [showLoginForm, setShowLoginForm] = useState(false);
  const [showSignupForm, setShowSignupForm] = useState(false);
  const [showValidationForm, setShowValidationForm] = useState(false);
  const [loginData, setLoginData] = useState({ username: '', password: '' });
  const [signupData, setSignupData] = useState({ username: '', password: '', email: '' });
  const [validationData, setValidationData] = useState({ username: '', code: '' });
  const [loginMessage, setLoginMessage] = useState('');
  const [signupMessage, setSignupMessage] = useState('');
  const [validationMessage, setValidationMessage] = useState('');
  const [signupErrors, setSignupErrors] = useState({});
  const [validationErrors, setValidationErrors] = useState({});
  const [showErrorDialog, setShowErrorDialog] = useState(false);
  const [errorDialogMessage, setErrorDialogMessage] = useState('');

  // Handle escape key to close modals
  useEffect(() => {
    const handleEscape = (e) => {
      if (e.key === 'Escape') {
        if (showErrorDialog) {
          setShowErrorDialog(false);
          setErrorDialogMessage('');
        } else if (showValidationForm) {
          setShowValidationForm(false);
          setValidationData({ username: '', code: '' });
          setValidationErrors({});
        } else if (showSignupForm) {
          setShowSignupForm(false);
          setSignupData({ username: '', password: '', email: '' });
          setSignupErrors({});
        } else if (showLoginForm) {
          setShowLoginForm(false);
          setLoginData({ username: '', password: '' });
        }
      }
    };

    document.addEventListener('keydown', handleEscape);
    return () => document.removeEventListener('keydown', handleEscape);
  }, [showLoginForm, showSignupForm, showValidationForm, showErrorDialog]);

  // Validation functions
  const validateSignupForm = () => {
    const errors = {};
    
    // Username validation
    if (!signupData.username.trim()) {
      errors.username = 'Username is required';
    } else if (signupData.username.length < 3) {
      errors.username = 'Username must be at least 3 characters';
    } else if (!/^[a-zA-Z0-9_]+$/.test(signupData.username)) {
      errors.username = 'Username can only contain letters, numbers, and underscores';
    }
    
    // Email validation
    if (!signupData.email.trim()) {
      errors.email = 'Email is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(signupData.email)) {
      errors.email = 'Please enter a valid email address';
    }
    
    // Password validation
    if (!signupData.password) {
      errors.password = 'Password is required';
    } else if (signupData.password.length < 6) {
      errors.password = 'Password must be at least 6 characters';
    }
    
    setSignupErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const validateValidationForm = () => {
    const errors = {};
    
    if (!validationData.username.trim()) {
      errors.username = 'Username is required';
    }
    
    if (!validationData.code) {
      errors.code = 'Verification code is required';
    } else if (!/^\d{6,9}$/.test(validationData.code)) {
      errors.code = 'Verification code must be 6-9 digits';
    }
    
    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    const result = await onLogin(loginData.username, loginData.password);
    setLoginMessage(result.message);
    if (result.success) {
      setShowLoginForm(false);
      setLoginData({ username: '', password: '' });
    }
  };

  const handleSignupSubmit = async (e) => {
    e.preventDefault();
    setSignupMessage('');
    setSignupErrors({});
    
    console.log('Signup form data:', signupData); // Debug log
    
    if (!validateSignupForm()) {
      return;
    }
    
    try {
      const response = await ApiService.registerUser(signupData.username, signupData.password, signupData.email);
      setSignupMessage(response.message || 'Registration successful! Check your email for validation code.');
      setShowSignupForm(false);
      setShowValidationForm(true);
      setValidationData({ username: signupData.username, code: '' });
    } catch (error) {
      console.error('Registration error:', error); // Debug log
      console.error('Error data:', error.data); // Debug log for backend response
      console.error('Error status:', error.status); // Debug log for HTTP status
      console.error('Error response:', error.response); // Debug log for full response
      
      // Extract the exact error message from the backend response
      let errorMessage = 'Registration failed. Please try again.';
      
      if (error.isNetworkError) {
        errorMessage = 'Network error: Unable to connect to the server. Please check your internet connection and try again.';
      } else if (error.data) {
        // Try different possible error message fields from the backend
        if (error.data.message) {
          errorMessage = error.data.message;
        } else if (error.data.error) {
          errorMessage = error.data.error;
        } else if (error.data.details) {
          errorMessage = error.data.details;
        } else if (typeof error.data === 'string') {
          errorMessage = error.data;
        }
      } else if (error.message) {
        // Use the error message from the API service
        errorMessage = error.message;
      }
      
      // Add HTTP status code if available
      if (error.status) {
        errorMessage += ` (Status: ${error.status})`;
      }
      
      console.log('Displaying error message:', errorMessage); // Debug log
      setErrorDialogMessage(errorMessage);
      setShowErrorDialog(true);
    }
  };

  const handleValidationSubmit = async (e) => {
    e.preventDefault();
    setValidationMessage('');
    setValidationErrors({});
    
    if (!validateValidationForm()) {
      return;
    }
    
    try {
      const response = await ApiService.validateAccount(validationData.username, parseInt(validationData.code));
      setValidationMessage(response.message || 'Account validated successfully!');
      
      // Auto-login after successful validation
      const loginResult = await onLogin(validationData.username, signupData.password);
      if (loginResult.success) {
        setShowValidationForm(false);
        setValidationData({ username: '', code: '' });
        setSignupData({ username: '', password: '', email: '' });
      }
    } catch (error) {
      console.error('Validation error:', error); // Debug log
      console.error('Error data:', error.data); // Debug log for backend response
      console.error('Error status:', error.status); // Debug log for HTTP status
      console.error('Error response:', error.response); // Debug log for full response
      
      // Extract the exact error message from the backend response
      let errorMessage = 'Validation failed. Please check your code and try again.';
      
      if (error.isNetworkError) {
        errorMessage = 'Network error: Unable to connect to the server. Please check your internet connection and try again.';
      } else if (error.data) {
        // Try different possible error message fields from the backend
        if (error.data.message) {
          errorMessage = error.data.message;
        } else if (error.data.error) {
          errorMessage = error.data.error;
        } else if (error.data.details) {
          errorMessage = error.data.details;
        } else if (typeof error.data === 'string') {
          errorMessage = error.data;
        }
      } else if (error.message) {
        // Use the error message from the API service
        errorMessage = error.message;
      }
      
      // Add HTTP status code if available
      if (error.status) {
        errorMessage += ` (Status: ${error.status})`;
      }
      
      console.log('Displaying error message:', errorMessage); // Debug log
      setErrorDialogMessage(errorMessage);
      setShowErrorDialog(true);
    }
  };

  const handleLogout = () => {
    onLogout();
    setShowLoginForm(false);
    setShowSignupForm(false);
    setShowValidationForm(false);
  };

  const closeLoginForm = () => {
    setShowLoginForm(false);
    setLoginData({ username: '', password: '' });
    setLoginMessage('');
  };

  const closeSignupForm = () => {
    setShowSignupForm(false);
    setSignupData({ username: '', password: '', email: '' });
    setSignupErrors({});
    setSignupMessage('');
  };

  const closeValidationForm = () => {
    setShowValidationForm(false);
    setValidationData({ username: '', code: '' });
    setValidationErrors({});
    setValidationMessage('');
  };

  const closeErrorDialog = () => {
    setShowErrorDialog(false);
    setErrorDialogMessage('');
  };

  const testBackendResponse = async () => {
    try {
      const result = await ApiService.testBackendResponse();
      console.log('Backend test result:', result);
      setErrorDialogMessage(`Backend Test Result:\nStatus: ${result.status}\nResponse: ${result.text}\nOK: ${result.ok}`);
      setShowErrorDialog(true);
    } catch (error) {
      console.error('Backend test failed:', error);
      setErrorDialogMessage(`Backend Test Failed: ${error.message}`);
      setShowErrorDialog(true);
    }
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
                <button className="btn-secondary" onClick={() => setShowSignupForm(true)}>
                  <User className="icon" />
                  Sign Up
                </button>
                <button className="btn-primary" onClick={() => setShowLoginForm(true)}>
                  <LogIn className="icon" />
                  Login
                </button>
              </>
            )}
            {/* Debug button - remove in production */}
            <button className="btn-secondary" onClick={testBackendResponse} style={{fontSize: '0.8rem', padding: '0.25rem 0.5rem'}}>
              Test Backend
            </button>
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
        <div className="modal-overlay" onClick={closeLoginForm}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Login to Metal Investment</h2>
              <button className="modal-close" onClick={closeLoginForm}>
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
              <div className="form-actions">
                <button type="button" className="btn-secondary" onClick={closeLoginForm}>
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  Login
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Sign Up Modal */}
      {showSignupForm && (
        <div className="modal-overlay" onClick={closeSignupForm}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Sign Up for Metal Investment</h2>
              <button className="modal-close" onClick={closeSignupForm}>
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleSignupSubmit} className="login-form">
              <div className="form-group">
                <label htmlFor="signup-username">Username *</label>
                <input
                  type="text"
                  id="signup-username"
                  name="username"
                  value={signupData.username}
                  onChange={(e) => setSignupData({ ...signupData, username: e.target.value })}
                  className={signupErrors.username ? 'error' : ''}
                  placeholder="Enter your username"
                  required
                />
                {signupErrors.username && (
                  <div className="error-message">{signupErrors.username}</div>
                )}
              </div>
              <div className="form-group">
                <label htmlFor="signup-email">Email *</label>
                <input
                  type="email"
                  id="signup-email"
                  name="email"
                  value={signupData.email}
                  onChange={(e) => setSignupData({ ...signupData, email: e.target.value })}
                  className={signupErrors.email ? 'error' : ''}
                  placeholder="Enter your email address"
                  required
                />
                {signupErrors.email && (
                  <div className="error-message">{signupErrors.email}</div>
                )}
              </div>
              <div className="form-group">
                <label htmlFor="signup-password">Password *</label>
                <input
                  type="password"
                  id="signup-password"
                  name="password"
                  value={signupData.password}
                  onChange={(e) => setSignupData({ ...signupData, password: e.target.value })}
                  className={signupErrors.password ? 'error' : ''}
                  placeholder="Enter your password"
                  required
                />
                {signupErrors.password && (
                  <div className="error-message">{signupErrors.password}</div>
                )}
              </div>
              {signupMessage && (
                <div className={`message ${signupMessage.includes('successful') ? 'success' : 'error'}`}>
                  {signupMessage}
                </div>
              )}
              <div className="form-actions">
                <button type="button" className="btn-secondary" onClick={closeSignupForm}>
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  Sign Up
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Validation Modal */}
      {showValidationForm && (
        <div className="modal-overlay" onClick={closeValidationForm}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Validate Your Account</h2>
              <button className="modal-close" onClick={closeValidationForm}>
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleValidationSubmit} className="login-form">
              <div className="form-group">
                <label htmlFor="validation-username">Username *</label>
                <input
                  type="text"
                  id="validation-username"
                  name="username"
                  value={validationData.username}
                  onChange={(e) => setValidationData({ ...validationData, username: e.target.value })}
                  className={validationErrors.username ? 'error' : ''}
                  placeholder="Enter your username"
                  required
                />
                {validationErrors.username && (
                  <div className="error-message">{validationErrors.username}</div>
                )}
              </div>
              <div className="form-group">
                <label htmlFor="validation-code">Verification Code *</label>
                <input
                  type="number"
                  id="validation-code"
                  name="code"
                  value={validationData.code}
                  onChange={(e) => setValidationData({ ...validationData, code: e.target.value })}
                  placeholder="Enter the 6-9 digit code sent to your email"
                  className={validationErrors.code ? 'error' : ''}
                  required
                />
                {validationErrors.code && (
                  <div className="error-message">{validationErrors.code}</div>
                )}
              </div>
              {validationMessage && (
                <div className={`message ${validationMessage.includes('successfully') ? 'success' : 'error'}`}>
                  {validationMessage}
                </div>
              )}
              <div className="form-actions">
                <button type="button" className="btn-secondary" onClick={closeValidationForm}>
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  Validate Account
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Error Dialog */}
      {showErrorDialog && (
        <div className="modal-overlay" onClick={closeErrorDialog}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Error</h2>
              <button className="modal-close" onClick={closeErrorDialog}>
                <X size={20} />
              </button>
            </div>
            <div className="error-dialog-content">
              <div className="error-icon">⚠️</div>
              <p className="error-message-text">{errorDialogMessage}</p>
              <button className="btn-primary full-width" onClick={closeErrorDialog}>
                OK
              </button>
            </div>
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
