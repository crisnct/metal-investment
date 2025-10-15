import React, { useState, useEffect } from 'react';
import { TrendingUp, Menu, X, User, LogIn, LogOut } from 'lucide-react';
import './Header.css';
import ApiService from '../services/api';

const Header = ({ isLoggedIn, onLogin, onLogout }) => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [showLoginForm, setShowLoginForm] = useState(false);
  const [showSignupForm, setShowSignupForm] = useState(false);
  const [showValidationForm, setShowValidationForm] = useState(false);
  const [loginData, setLoginData] = useState({ username: '', password: '' });
  const [signupData, setSignupData] = useState({ username: '', password: '', email: '' });
  const [validationData, setValidationData] = useState({ email: '', username: '', code: '' });
  const [loginMessage, setLoginMessage] = useState('');
  const [signupMessage, setSignupMessage] = useState('');
  const [validationMessage, setValidationMessage] = useState('');
  const [signupErrors, setSignupErrors] = useState({});
  const [validationErrors, setValidationErrors] = useState({});
  const [showErrorDialog, setShowErrorDialog] = useState(false);
  const [errorDialogMessage, setErrorDialogMessage] = useState('');
  const [emailSent, setEmailSent] = useState(false);
  const [showResetPasswordDialog, setShowResetPasswordDialog] = useState(false);
  const [showResetPasswordEmailDialog, setShowResetPasswordEmailDialog] = useState(false);
  const [resetPasswordData, setResetPasswordData] = useState({ email: '', code: '', newPassword: '' });
  const [resetPasswordErrors, setResetPasswordErrors] = useState({});
  const [resetPasswordMessage, setResetPasswordMessage] = useState('');
  const [resetPasswordToken, setResetPasswordToken] = useState(null);
  const [resetPasswordEmail, setResetPasswordEmail] = useState('');

  // Handle escape key to close modals
  useEffect(() => {
    const handleEscape = (e) => {
      if (e.key === 'Escape') {
        if (showErrorDialog) {
          setShowErrorDialog(false);
          setErrorDialogMessage('');
        } else if (showResetPasswordDialog) {
          setShowResetPasswordDialog(false);
          setResetPasswordData({ email: '', code: '', newPassword: '' });
          setResetPasswordErrors({});
          setResetPasswordMessage('');
          setResetPasswordToken(null);
          setResetPasswordEmail('');
        } else if (showResetPasswordEmailDialog) {
          setShowResetPasswordEmailDialog(false);
          setResetPasswordData({ email: '', code: '', newPassword: '' });
          setResetPasswordErrors({});
          setResetPasswordMessage('');
          setResetPasswordToken(null);
          setResetPasswordEmail('');
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
  }, [showLoginForm, showSignupForm, showValidationForm, showErrorDialog, showResetPasswordDialog, showResetPasswordEmailDialog]);

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
    
    // Email validation
    if (!validationData.email) {
      errors.email = 'Email is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(validationData.email)) {
      errors.email = 'Please enter a valid email address';
    }
    
    // Username validation
    if (!validationData.username.trim()) {
      errors.username = 'Username is required';
    }
    
    // Code validation
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
      setValidationMessage('Account validated successfully! You can now login.');
      
      // Close validation form and redirect to login
      setTimeout(() => {
        setShowValidationForm(false);
        setValidationData({ email: '', username: '', code: '' });
        setEmailSent(false);
        setShowLoginForm(true);
      }, 2000);
      
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
    setValidationData({ email: '', username: '', code: '' });
    setValidationErrors({});
    setValidationMessage('');
    setEmailSent(false);
  };

  const closeErrorDialog = () => {
    setShowErrorDialog(false);
    setErrorDialogMessage('');
  };

  const handleResetPassword = () => {
    setShowResetPasswordEmailDialog(true);
    closeLoginForm();
  };

  const validateResetPasswordEmailForm = () => {
    const errors = {};
    
    if (!resetPasswordData.email.trim()) {
      errors.email = 'Email is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(resetPasswordData.email)) {
      errors.email = 'Please enter a valid email address';
    }
    
    return errors;
  };

  const validateResetPasswordForm = () => {
    const errors = {};
    
    if (!resetPasswordData.code.trim()) {
      errors.code = 'Code is required';
    }
    
    if (!resetPasswordData.newPassword.trim()) {
      errors.newPassword = 'New password is required';
    } else if (resetPasswordData.newPassword.length < 6) {
      errors.newPassword = 'Password must be at least 6 characters long';
    }
    
    return errors;
  };

  const handleResetPasswordEmailSubmit = async (e) => {
    e.preventDefault();
    
    const errors = validateResetPasswordEmailForm();
    if (Object.keys(errors).length > 0) {
      setResetPasswordErrors(errors);
      return;
    }
    
    setResetPasswordErrors({});
    setResetPasswordMessage('');
    
    try {
      const response = await ApiService.resetPassword(resetPasswordData.email);
      setResetPasswordToken(response.token);
      setResetPasswordEmail(resetPasswordData.email);
      setShowResetPasswordEmailDialog(false);
      setShowResetPasswordDialog(true);
    } catch (error) {
      setResetPasswordMessage(error.data?.message || error.message || 'Failed to reset password');
    }
  };

  const handleResetPasswordSubmit = async (e) => {
    e.preventDefault();
    
    const errors = validateResetPasswordForm();
    if (Object.keys(errors).length > 0) {
      setResetPasswordErrors(errors);
      return;
    }
    
    setResetPasswordErrors({});
    setResetPasswordMessage('');
    
    try {
      const response = await ApiService.changePassword(
        resetPasswordToken,
        resetPasswordData.code,
        resetPasswordData.newPassword,
        resetPasswordEmail
      );
      
      setResetPasswordMessage('Password changed successfully! You can now login with your new password.');
      setResetPasswordData({ code: '', newPassword: '' });
      
      // Close dialog after 2 seconds
      setTimeout(() => {
        setShowResetPasswordDialog(false);
        setResetPasswordMessage('');
        setResetPasswordToken(null);
      }, 2000);
      
    } catch (error) {
      setResetPasswordMessage(error.data?.message || error.message || 'Failed to change password');
    }
  };

  const closeResetPasswordDialog = () => {
    setShowResetPasswordDialog(false);
    setResetPasswordData({ email: '', code: '', newPassword: '' });
    setResetPasswordErrors({});
    setResetPasswordMessage('');
    setResetPasswordToken(null);
    setResetPasswordEmail('');
  };

  const closeResetPasswordEmailDialog = () => {
    setShowResetPasswordEmailDialog(false);
    setResetPasswordData({ email: '', code: '', newPassword: '' });
    setResetPasswordErrors({});
    setResetPasswordMessage('');
    setResetPasswordToken(null);
    setResetPasswordEmail('');
  };


  const handleResendEmail = async () => {
    // Validate email and username before sending
    const errors = {};
    if (!validationData.email) {
      errors.email = 'Email is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(validationData.email)) {
      errors.email = 'Please enter a valid email address';
    }
    if (!validationData.username) {
      errors.username = 'Username is required';
    }
    
    if (Object.keys(errors).length > 0) {
      setValidationErrors(errors);
      return;
    }

    try {
      // First check if user exists and is pending validation
      await ApiService.checkUserPendingValidation(validationData.username, validationData.email);
      
      // If check passes, resend validation email
      await ApiService.resendValidationEmail(validationData.username, validationData.email);
      
      setEmailSent(true);
      setValidationErrors({});
      setValidationMessage('Validation email sent successfully! Please check your inbox.');
    } catch (error) {
      console.error('Resend email error:', error);
      let errorMessage = 'Failed to send validation email. Please try again.';
      
      if (error.isNetworkError) {
        errorMessage = 'Network error: Unable to connect to the server. Please check your internet connection and try again.';
      } else if (error.data) {
        if (error.data.message) {
          errorMessage = error.data.message;
        } else if (error.data.error) {
          errorMessage = error.data.error;
        }
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      setErrorDialogMessage(errorMessage);
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
                <button className="btn-secondary" onClick={() => document.getElementById('profile-section')?.scrollIntoView({ behavior: 'smooth' })}>
                  <User className="icon" />
                  Profile
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
          <div className="form-actions-right">
            <button type="button" className="btn-secondary" onClick={closeLoginForm}>
              Cancel
            </button>
            <button type="submit" className="btn-primary btn-login">
              <LogIn className="icon" />
              Login
            </button>
          </div>
        </div>
        <div className="form-links-bottom">
          <div className="form-links-left">
            <a href="#" onClick={(e) => { e.preventDefault(); setShowValidationForm(true); closeLoginForm(); }} className="link">
              Validate account
            </a>
          </div>
          <div className="form-links-right">
            <a href="#" onClick={(e) => { e.preventDefault(); handleResetPassword(); }} className="link">
              Reset password
            </a>
          </div>
        </div>
            </form>
          </div>
        </div>
      )}

      {/* Sign Up Modal */}
      {showSignupForm && (
        <div className="modal-overlay">
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
          <div className="modal validation-popup" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Validate Your Account</h2>
              <button className="modal-close" onClick={closeValidationForm}>
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleValidationSubmit} className="login-form">
              <div className="form-group">
                <label htmlFor="validation-email">Email *</label>
                <input
                  type="email"
                  id="validation-email"
                  name="email"
                  value={validationData.email}
                  onChange={(e) => setValidationData({ ...validationData, email: e.target.value })}
                  className={validationErrors.email ? 'error' : ''}
                  placeholder="Enter your email address"
                  required
                />
                {validationErrors.email && (
                  <div className="error-message">{validationErrors.email}</div>
                )}
              </div>
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
                <div className="form-group-input">
                  <input
                    type="text"
                    id="validation-code"
                    name="code"
                    value={validationData.code}
                    onChange={(e) => setValidationData({ ...validationData, code: e.target.value })}
                    placeholder="Enter the 6-9 digit code sent to your email"
                    className={validationErrors.code ? 'error' : ''}
                    required
                  />
                  <a href="#" onClick={(e) => { e.preventDefault(); handleResendEmail(); }} className="link" style={{ pointerEvents: emailSent ? 'none' : 'auto', opacity: emailSent ? 0.6 : 1 }}>
                    {emailSent ? 'Email sent! Check your inbox.' : 'Email code'}
                  </a>
                </div>
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
                <div className="form-actions-right">
                  <button type="button" className="btn-secondary" onClick={closeValidationForm}>
                    Cancel
                  </button>
                  <button type="submit" className="btn-primary btn-validate">
                    <LogIn className="icon" />
                    Validate
                  </button>
                </div>
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

      {/* Reset Password Email Dialog */}
      {showResetPasswordEmailDialog && (
        <div className="modal-overlay" onClick={closeResetPasswordEmailDialog}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Reset Password</h2>
              <button className="modal-close" onClick={closeResetPasswordEmailDialog}>
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleResetPasswordEmailSubmit} className="login-form">
              <div className="form-group">
                <label htmlFor="reset-email">Email Address *</label>
                <input
                  type="email"
                  id="reset-email"
                  name="email"
                  value={resetPasswordData.email}
                  onChange={(e) => setResetPasswordData({ ...resetPasswordData, email: e.target.value })}
                  className={resetPasswordErrors.email ? 'error' : ''}
                  placeholder="Enter your email address"
                  required
                />
                {resetPasswordErrors.email && (
                  <div className="error-message">{resetPasswordErrors.email}</div>
                )}
              </div>
              {resetPasswordMessage && (
                <div className={`message ${resetPasswordMessage.includes('successfully') ? 'success' : 'error'}`}>
                  {resetPasswordMessage}
                </div>
              )}
              <div className="form-actions">
                <div className="form-actions-right">
                  <button type="button" className="btn-secondary" onClick={closeResetPasswordEmailDialog}>
                    Cancel
                  </button>
                  <button type="submit" className="btn-primary">
                    Send Reset Code
                  </button>
                </div>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Reset Password Dialog */}
      {showResetPasswordDialog && (
        <div className="modal-overlay" onClick={closeResetPasswordDialog}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Reset Password</h2>
              <button className="modal-close" onClick={closeResetPasswordDialog}>
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleResetPasswordSubmit} className="login-form">
              <div className="form-group">
                <label htmlFor="reset-code">Code *</label>
                <input
                  type="text"
                  id="reset-code"
                  name="code"
                  value={resetPasswordData.code}
                  onChange={(e) => setResetPasswordData({ ...resetPasswordData, code: e.target.value })}
                  className={resetPasswordErrors.code ? 'error' : ''}
                  placeholder="Enter the code sent to your email"
                  required
                />
                {resetPasswordErrors.code && (
                  <div className="error-message">{resetPasswordErrors.code}</div>
                )}
              </div>
              <div className="form-group">
                <label htmlFor="reset-new-password">New Password *</label>
                <input
                  type="password"
                  id="reset-new-password"
                  name="newPassword"
                  value={resetPasswordData.newPassword}
                  onChange={(e) => setResetPasswordData({ ...resetPasswordData, newPassword: e.target.value })}
                  className={resetPasswordErrors.newPassword ? 'error' : ''}
                  placeholder="Enter your new password (min 6 characters)"
                  required
                />
                {resetPasswordErrors.newPassword && (
                  <div className="error-message">{resetPasswordErrors.newPassword}</div>
                )}
              </div>
              {resetPasswordMessage && (
                <div className={`message ${resetPasswordMessage.includes('successfully') ? 'success' : 'error'}`}>
                  {resetPasswordMessage}
                </div>
              )}
              <div className="form-actions">
                <div className="form-actions-right">
                  <button type="button" className="btn-secondary" onClick={closeResetPasswordDialog}>
                    Cancel
                  </button>
                  <button type="submit" className="btn-primary">
                    Reset
                  </button>
                </div>
              </div>
            </form>
          </div>
        </div>
      )}


    </header>
  );
};

export default Header;
