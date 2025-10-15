import React, { useEffect, useState } from 'react';
import './Profile.css';
import ApiService from '../services/api';

const Profile = ({ userInfo }) => {
  const [showPurchaseDialog, setShowPurchaseDialog] = useState(false);
  const [purchaseData, setPurchaseData] = useState({ symbol: '', amount: '', cost: '' });
  const [purchaseErrors, setPurchaseErrors] = useState({});
  const [purchaseMessage, setPurchaseMessage] = useState('');
  const [showSellDialog, setShowSellDialog] = useState(false);
  const [sellData, setSellData] = useState({ symbol: '', amount: '', price: '' });
  const [sellErrors, setSellErrors] = useState({});
  const [sellMessage, setSellMessage] = useState('');
  const [profitLoading, setProfitLoading] = useState(false);
  const [profitError, setProfitError] = useState('');
  const [profitLastUpdated, setProfitLastUpdated] = useState('');
  const [profitEntries, setProfitEntries] = useState([]);
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const [notificationPeriod, setNotificationPeriod] = useState('');
  const [notificationLoading, setNotificationLoading] = useState(false);
  const [notificationMessage, setNotificationMessage] = useState('');
  const [showDeleteConfirmDialog, setShowDeleteConfirmDialog] = useState(false);
  const [showDeleteAccountDialog, setShowDeleteAccountDialog] = useState(false);
  const [deleteAccountData, setDeleteAccountData] = useState({ password: '', code: '' });
  const [deleteAccountErrors, setDeleteAccountErrors] = useState({});
  const [deleteAccountMessage, setDeleteAccountMessage] = useState('');
  const [deleteAccountLoading, setDeleteAccountLoading] = useState(false);

  useEffect(() => {
    const loadProfit = async () => {
      try {
        setProfitLoading(true);
        setProfitError('');
        const data = await ApiService.getProfit();
        console.log('Profit API response:', data); // Debug log
        
        // Handle the correct response structure
        let entries = [];
        let lastUpdatedText = '';

        if (data && data.metalInfo && Array.isArray(data.metalInfo)) {
          // Convert UserMetalInfoDto to table format
          entries = data.metalInfo.map(item => ({
            symbol: item.metalSymbol,
            amount: parseFloat(item.amountPurchased).toFixed(2),
            cost: Math.round(item.costPurchased),
            currentPrice: Math.round(item.costNow / item.amountPurchased), // Calculate price per unit
            value: Math.round(item.costNow),
            profit: Math.round(item.profit),
            date: data.time ? new Date(data.time).toLocaleDateString() : new Date().toLocaleDateString()
          }));
          
          // Set last updated from the response timestamp
          if (data.time) {
            lastUpdatedText = new Date(data.time).toLocaleString();
          }
        }

        setProfitEntries(entries);
        if (lastUpdatedText) {
          setProfitLastUpdated(lastUpdatedText);
        } else {
          const now = new Date();
          setProfitLastUpdated(now.toLocaleString());
        }
      } catch (err) {
        setProfitError(err?.data?.message || err?.message || 'Failed to load profit');
        const now = new Date();
        setProfitLastUpdated(now.toLocaleString());
      } finally {
        setProfitLoading(false);
      }
    };
    loadProfit();
  }, [refreshTrigger]);

  // Load notification period on mount
  useEffect(() => {
    const loadNotificationPeriod = async () => {
      try {
        setNotificationLoading(true);
        const data = await ApiService.getNotificationPeriod();
        if (data && data.message) {
          // Extract number from message like "The notification period is 7 days"
          const match = data.message.match(/(\d+)/);
          if (match) {
            // Display days directly (no conversion needed)
            setNotificationPeriod(match[1]);
          }
        }
      } catch (error) {
        console.error('Failed to load notification period:', error);
        // Don't show error to user, just leave field empty
      } finally {
        setNotificationLoading(false);
      }
    };
    
    loadNotificationPeriod();
  }, []);

  const handlePurchaseClick = () => {
    setShowPurchaseDialog(true);
  };

  const closePurchaseDialog = () => {
    setShowPurchaseDialog(false);
    setPurchaseData({ symbol: '', amount: '', cost: '' });
    setPurchaseErrors({});
    setPurchaseMessage('');
  };

  const handleSellClick = () => {
    setShowSellDialog(true);
  };

  const closeSellDialog = () => {
    setShowSellDialog(false);
    setSellData({ symbol: '', amount: '', price: '' });
    setSellErrors({});
    setSellMessage('');
  };

  const validatePurchaseForm = () => {
    const errors = {};
    
    if (!purchaseData.symbol.trim()) {
      errors.symbol = 'Symbol is required';
    }
    
    if (!purchaseData.amount.trim()) {
      errors.amount = 'Amount is required';
    } else if (isNaN(purchaseData.amount) || parseFloat(purchaseData.amount) <= 0) {
      errors.amount = 'Amount must be a positive number';
    }
    
    if (!purchaseData.cost.trim()) {
      errors.cost = 'Cost (RON) is required';
    } else if (isNaN(purchaseData.cost) || parseFloat(purchaseData.cost) <= 0) {
      errors.cost = 'Cost (RON) must be a positive number';
    }
    
    return errors;
  };

  const validateSellForm = () => {
    const errors = {};
    
    if (!sellData.symbol.trim()) {
      errors.symbol = 'Symbol is required';
    }
    
    if (!sellData.amount.trim()) {
      errors.amount = 'Amount is required';
    } else if (isNaN(sellData.amount) || parseFloat(sellData.amount) <= 0) {
      errors.amount = 'Amount must be a positive number';
    }
    
    if (!sellData.price.trim()) {
      errors.price = 'Price is required';
    } else if (isNaN(sellData.price) || parseFloat(sellData.price) <= 0) {
      errors.price = 'Price must be a positive number';
    }
    
    return errors;
  };

  const handlePurchaseSubmit = async (e) => {
    e.preventDefault();
    
    const errors = validatePurchaseForm();
    if (Object.keys(errors).length > 0) {
      setPurchaseErrors(errors);
      return;
    }
    
    setPurchaseErrors({});
    setPurchaseMessage('');
    
    try {
      const response = await ApiService.recordPurchase(
        parseInt(purchaseData.amount),
        purchaseData.symbol,
        parseFloat(purchaseData.cost)
      );
      
      setPurchaseMessage('Purchase recorded successfully!');
      setPurchaseData({ symbol: '', amount: '', cost: '' });
      
      // Refresh profit data to show new purchase
      setRefreshTrigger(prev => prev + 1);
      
      // Close dialog after 2 seconds
      setTimeout(() => {
        closePurchaseDialog();
      }, 2000);
      
    } catch (error) {
      setPurchaseMessage(error.data?.message || error.message || 'Failed to record purchase');
    }
  };

  const handleSellSubmit = async (e) => {
    e.preventDefault();
    
    const errors = validateSellForm();
    if (Object.keys(errors).length > 0) {
      setSellErrors(errors);
      return;
    }
    
    setSellErrors({});
    setSellMessage('');
    
    try {
      const response = await ApiService.sellMetal(
        parseFloat(sellData.amount),
        sellData.symbol,
        parseFloat(sellData.price)
      );
      
      setSellMessage('Metal sold successfully!');
      setSellData({ symbol: '', amount: '', price: '' });
      
      // Refresh profit data to show updated holdings
      setRefreshTrigger(prev => prev + 1);
      
      // Close dialog after 2 seconds
      setTimeout(() => {
        closeSellDialog();
      }, 2000);
      
    } catch (error) {
      setSellMessage(error.data?.message || error.message || 'Failed to sell metal');
    }
  };

  const handleSetNotification = async () => {
    if (!notificationPeriod || isNaN(notificationPeriod) || parseInt(notificationPeriod) < 0) {
      setNotificationMessage('Please enter a valid number of days');
      return;
    }

    try {
      setNotificationLoading(true);
      setNotificationMessage('');
      
      // Send days directly to backend
      await ApiService.setNotificationPeriod(parseInt(notificationPeriod));
      setNotificationMessage('Notification period updated successfully!');
      
      // Clear message after 3 seconds
      setTimeout(() => {
        setNotificationMessage('');
      }, 3000);
      
    } catch (error) {
      setNotificationMessage(error.data?.message || error.message || 'Failed to update notification period');
    } finally {
      setNotificationLoading(false);
    }
  };

  const handleDeleteAccountClick = () => {
    setShowDeleteConfirmDialog(true);
  };

  const closeDeleteConfirmDialog = () => {
    setShowDeleteConfirmDialog(false);
  };

  const handleDeleteConfirmContinue = async () => {
    try {
      setDeleteAccountLoading(true);
      setDeleteAccountMessage('');
      
      // Call the preparation API to send email with code
      await ApiService.deleteAccountPreparation();
      
      // Close confirmation dialog and show delete account dialog
      setShowDeleteConfirmDialog(false);
      setShowDeleteAccountDialog(true);
      
    } catch (error) {
      setDeleteAccountMessage(error.data?.message || error.message || 'Failed to send preparation email');
    } finally {
      setDeleteAccountLoading(false);
    }
  };

  const closeDeleteAccountDialog = () => {
    setShowDeleteAccountDialog(false);
    setDeleteAccountData({ password: '', code: '' });
    setDeleteAccountErrors({});
    setDeleteAccountMessage('');
  };

  const validateDeleteAccountForm = () => {
    const errors = {};
    
    if (!deleteAccountData.password.trim()) {
      errors.password = 'Password is required';
    }
    
    if (!deleteAccountData.code.trim()) {
      errors.code = 'Confirmation code is required';
    } else if (isNaN(deleteAccountData.code) || deleteAccountData.code.length !== 6) {
      errors.code = 'Confirmation code must be a 6-digit number';
    }
    
    return errors;
  };

  const handleDeleteAccountSubmit = async (e) => {
    e.preventDefault();
    
    const errors = validateDeleteAccountForm();
    if (Object.keys(errors).length > 0) {
      setDeleteAccountErrors(errors);
      return;
    }
    
    setDeleteAccountErrors({});
    setDeleteAccountMessage('');
    
    try {
      setDeleteAccountLoading(true);
      
      // Call the delete account API
      await ApiService.deleteAccount(deleteAccountData.password, deleteAccountData.code);
      
      setDeleteAccountMessage('Account deleted successfully! You will be logged out.');
      
      // Clear local storage and redirect to login
      setTimeout(() => {
        localStorage.removeItem('userToken');
        localStorage.removeItem('userInfo');
        window.location.href = '/';
      }, 2000);
      
    } catch (error) {
      setDeleteAccountMessage(error.data?.message || error.message || 'Failed to delete account');
    } finally {
      setDeleteAccountLoading(false);
    }
  };

  return (
    <section id="profile-section" className="profile-section">
      <div className="container">
        <div className="profile-content">
          <h1>Profile</h1>
          <div className="profile-info-section">
            <h3>Info</h3>
            <div className="profile-info">
              <div className="info-item">
                <span className="info-label">Username:</span>
                <span className="info-value">{userInfo?.username || 'Loading...'}</span>
              </div>
              <div className="info-item">
                <span className="info-label">Email address:</span>
                <span className="info-value">{userInfo?.email || 'Loading...'}</span>
              </div>
              
              <div className="info-item notification-item">
                <div className="info-label">
                  <span>Investment</span>
                  <span>Update Frequency:</span>
                </div>
                <div className="notification-controls">
                  <input
                    type="number"
                    className="notification-input"
                    placeholder="Enter days (e.g., 7 for 7 days)"
                    min="0"
                    value={notificationPeriod}
                    onChange={(e) => setNotificationPeriod(e.target.value)}
                    disabled={notificationLoading}
                  />
                  <span className="notification-unit">day(s)</span>
                  <button 
                    className="btn-set-notification"
                    onClick={handleSetNotification}
                    disabled={notificationLoading}
                  >
                    {notificationLoading ? 'Setting...' : 'Set'}
                  </button>
                </div>
                {notificationMessage && (
                  <div className={`notification-message ${notificationMessage.includes('successfully') ? 'success' : 'error'}`}>
                    {notificationMessage}
                  </div>
                )}
              </div>
              
              <div className="info-item delete-account-item">
                <a 
                  href="#"
                  className="delete-account-link"
                  onClick={(e) => {
                    e.preventDefault();
                    handleDeleteAccountClick();
                  }}
                >
                  Delete Account
                </a>
              </div>
            </div>
          </div>

              <div className="profile-investment-section">
                <h3>Investment</h3>
                <div className="investment-content">
                  <div className="profit-section">
                    <div className="profit-info">
                      {profitLoading && (
                        <div className="loading">Loading profit...</div>
                      )}
                      {/* Error messages intentionally not shown to avoid red text in UI */}
                      {!profitLoading && (
                        <>
                          <div className="last-updated">
                            Last Updated: {profitLastUpdated}
                          </div>
                          <div className="profit-table-wrapper">
                            <table className="profit-table">
                              <thead>
                                <tr>
                                  <th>Symbol</th>
                                  <th>Amount (oz)</th>
                                  <th>Cost (RON)</th>
                                  <th>Current Price (RON)</th>
                                  <th>Value (RON)</th>
                                  <th>Profit (RON)</th>
                                  <th>Date</th>
                                </tr>
                              </thead>
                              <tbody>
                                {profitEntries && profitEntries.length > 0 ? (
                                  profitEntries.map((row, idx) => (
                                    <tr key={idx}>
                                      <td>{row.symbol || '-'}</td>
                                      <td>{row.amount || '-'}</td>
                                      <td>{row.cost || '-'}</td>
                                      <td>{row.currentPrice || '-'}</td>
                                      <td>{row.value || '-'}</td>
                                      <td>{row.profit || '-'}</td>
                                      <td>{row.date || '-'}</td>
                                    </tr>
                                  ))
                                ) : (
                                  null
                                )}
                              </tbody>
                            </table>
                          </div>
                        </>
                      )}
                    </div>
                  </div>
                  <div className="purchase-section">
                    <button className="btn-sell" onClick={handleSellClick}>
                      <svg className="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M3 3h18l-1 7H4L3 3z"/>
                        <path d="M8 21h8"/>
                        <path d="M12 17v4"/>
                      </svg>
                      Sell
                    </button>
                    <button className="btn-purchase" onClick={handlePurchaseClick}>
                      <svg className="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M3 3h18l-1 7H4L3 3z"/>
                        <path d="M8 21h8"/>
                        <path d="M12 17v4"/>
                      </svg>
                      Purchase
                    </button>
                  </div>
                </div>
              </div>

          <div className="profile-alerts-section">
            <h3>Alerts</h3>
            <div className="alerts-content">
              <div className="no-alerts">No alerts configured.</div>
            </div>
          </div>

          <div className="profile-functions-section">
            <h3>Functions</h3>
            <div className="functions-content">
              <div className="no-functions">No functions configured</div>
            </div>
          </div>
        </div>
      </div>

      {/* Purchase Dialog */}
      {showPurchaseDialog && (
        <div className="modal-overlay" onClick={closePurchaseDialog}>
          <div className="modal-content purchase-popup" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Purchase Metal</h3>
              <button className="modal-close" onClick={closePurchaseDialog}>
                ×
              </button>
            </div>
            
            <form onSubmit={handlePurchaseSubmit} className="purchase-form">
              <div className="form-group">
                <label htmlFor="purchase-symbol">Symbol *</label>
                <select
                  id="purchase-symbol"
                  name="symbol"
                  value={purchaseData.symbol}
                  onChange={(e) => setPurchaseData({ ...purchaseData, symbol: e.target.value })}
                  className={purchaseErrors.symbol ? 'error' : ''}
                  required
                >
                  <option value="">Select a metal</option>
                  <option value="AUX">AUX - Gold</option>
                  <option value="AGX">AGX - Silver</option>
                  <option value="PTX">PTX - Platinum</option>
                </select>
                {purchaseErrors.symbol && (
                  <div className="error-message">{purchaseErrors.symbol}</div>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="purchase-amount">Amount (oz) *</label>
                <input
                  type="number"
                  step="0.01"
                  id="purchase-amount"
                  name="amount"
                  value={purchaseData.amount}
                  onChange={(e) => setPurchaseData({ ...purchaseData, amount: e.target.value })}
                  placeholder="Enter amount (e.g., 1.5)"
                  className={purchaseErrors.amount ? 'error' : ''}
                  required
                />
                {purchaseErrors.amount && (
                  <div className="error-message">{purchaseErrors.amount}</div>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="purchase-cost">Cost (RON) *</label>
                <input
                  type="number"
                  step="0.01"
                  id="purchase-cost"
                  name="cost"
                  value={purchaseData.cost}
                  onChange={(e) => setPurchaseData({ ...purchaseData, cost: e.target.value })}
                  placeholder="Enter cost (positive number)"
                  className={purchaseErrors.cost ? 'error' : ''}
                  required
                />
                {purchaseErrors.cost && (
                  <div className="error-message">{purchaseErrors.cost}</div>
                )}
              </div>

              {purchaseMessage && (
                <div className={`message ${purchaseMessage.includes('successfully') ? 'success' : 'error'}`}>
                  {purchaseMessage}
                </div>
              )}

              <div className="form-actions">
                <div className="form-actions-right">
                  <button type="button" className="btn-secondary" onClick={closePurchaseDialog}>
                    Cancel
                  </button>
                  <button type="submit" className="btn-primary">
                    Purchase
                  </button>
                </div>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Sell Dialog */}
      {showSellDialog && (
        <div className="modal-overlay" onClick={closeSellDialog}>
          <div className="modal-content sell-popup" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Sell Metal</h3>
              <button className="modal-close" onClick={closeSellDialog}>
                ×
              </button>
            </div>
            
            <form onSubmit={handleSellSubmit} className="sell-form">
              <div className="form-group">
                <label htmlFor="sell-symbol">Symbol *</label>
                <select
                  id="sell-symbol"
                  name="symbol"
                  value={sellData.symbol}
                  onChange={(e) => setSellData({ ...sellData, symbol: e.target.value })}
                  className={sellErrors.symbol ? 'error' : ''}
                  required
                >
                  <option value="">Select a metal</option>
                  <option value="AUX">AUX-Gold</option>
                  <option value="AGX">AGX-Silver</option>
                  <option value="PTX">PTX-Platinum</option>
                </select>
                {sellErrors.symbol && (
                  <div className="error-message">{sellErrors.symbol}</div>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="sell-amount">Amount (oz) *</label>
                <input
                  type="number"
                  step="0.01"
                  id="sell-amount"
                  name="amount"
                  value={sellData.amount}
                  onChange={(e) => setSellData({ ...sellData, amount: e.target.value })}
                  placeholder="Enter amount to sell"
                  className={sellErrors.amount ? 'error' : ''}
                  required
                />
                {sellErrors.amount && (
                  <div className="error-message">{sellErrors.amount}</div>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="sell-price">Price (RON) *</label>
                <input
                  type="number"
                  step="0.01"
                  id="sell-price"
                  name="price"
                  value={sellData.price}
                  onChange={(e) => setSellData({ ...sellData, price: e.target.value })}
                  placeholder="Enter selling price"
                  className={sellErrors.price ? 'error' : ''}
                  required
                />
                {sellErrors.price && (
                  <div className="error-message">{sellErrors.price}</div>
                )}
              </div>

              {sellMessage && (
                <div className={`message ${sellMessage.includes('successfully') ? 'success' : 'error'}`}>
                  {sellMessage}
                </div>
              )}

              <div className="form-actions">
                <div className="form-actions-right">
                  <button type="button" className="btn-secondary" onClick={closeSellDialog}>
                    Cancel
                  </button>
                  <button type="submit" className="btn-primary">
                    <svg className="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M3 3h18l-1 7H4L3 3z"/>
                      <path d="M8 21h8"/>
                      <path d="M12 17v4"/>
                    </svg>
                    Sell
                  </button>
                </div>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Account Confirmation Dialog */}
      {showDeleteConfirmDialog && (
        <div className="modal-overlay" onClick={closeDeleteConfirmDialog}>
          <div className="modal-content delete-confirm-popup" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Delete Account</h3>
              <button className="modal-close" onClick={closeDeleteConfirmDialog}>
                ×
              </button>
            </div>
            
            <div className="delete-confirm-content">
              <div className="warning-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M12 9v4"/>
                  <path d="M12 17h.01"/>
                  <circle cx="12" cy="12" r="10"/>
                </svg>
              </div>
              <h4>Are you sure you want to delete your account?</h4>
              <p>This action cannot be undone. All your data including:</p>
              <ul>
                <li>Investment records</li>
                <li>Alerts and notifications</li>
                <li>Account settings</li>
              </ul>
              <p><strong>will be permanently deleted.</strong></p>
              
              {deleteAccountMessage && (
                <div className={`message ${deleteAccountMessage.includes('successfully') ? 'success' : 'error'}`}>
                  {deleteAccountMessage}
                </div>
              )}
            </div>
            
            <div className="form-actions">
              <div className="form-actions-right">
                <button type="button" className="btn-secondary" onClick={closeDeleteConfirmDialog}>
                  Cancel
                </button>
                <button 
                  type="button" 
                  className="btn-danger"
                  onClick={handleDeleteConfirmContinue}
                  disabled={deleteAccountLoading}
                >
                  {deleteAccountLoading ? 'Sending...' : 'Continue'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Delete Account Dialog */}
      {showDeleteAccountDialog && (
        <div className="modal-overlay" onClick={closeDeleteAccountDialog}>
          <div className="modal-content delete-account-popup" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Delete Account</h3>
              <button className="modal-close" onClick={closeDeleteAccountDialog}>
                ×
              </button>
            </div>
            
            <div className="delete-account-content">
              <p>Please check your email for the confirmation code and enter your password to confirm account deletion.</p>
            </div>
            
            <form onSubmit={handleDeleteAccountSubmit} className="delete-account-form">
              <div className="form-group">
                <label htmlFor="delete-password">Password *</label>
                <input
                  type="password"
                  id="delete-password"
                  name="password"
                  value={deleteAccountData.password}
                  onChange={(e) => setDeleteAccountData({ ...deleteAccountData, password: e.target.value })}
                  placeholder="Enter your password"
                  className={deleteAccountErrors.password ? 'error' : ''}
                  required
                />
                {deleteAccountErrors.password && (
                  <div className="error-message">{deleteAccountErrors.password}</div>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="delete-code">Confirmation Code *</label>
                <input
                  type="text"
                  id="delete-code"
                  name="code"
                  value={deleteAccountData.code}
                  onChange={(e) => setDeleteAccountData({ ...deleteAccountData, code: e.target.value })}
                  placeholder="Enter 6-digit code from email"
                  className={deleteAccountErrors.code ? 'error' : ''}
                  maxLength="6"
                  required
                />
                {deleteAccountErrors.code && (
                  <div className="error-message">{deleteAccountErrors.code}</div>
                )}
              </div>

              {deleteAccountMessage && (
                <div className={`message ${deleteAccountMessage.includes('successfully') ? 'success' : 'error'}`}>
                  {deleteAccountMessage}
                </div>
              )}

              <div className="form-actions">
                <div className="form-actions-right">
                  <button type="button" className="btn-secondary" onClick={closeDeleteAccountDialog}>
                    Cancel
                  </button>
                  <button 
                    type="submit" 
                    className="btn-danger"
                    disabled={deleteAccountLoading}
                  >
                    {deleteAccountLoading ? 'Deleting...' : 'Delete Account'}
                  </button>
                </div>
              </div>
            </form>
          </div>
        </div>
      )}
    </section>
  );
};

export default Profile;
