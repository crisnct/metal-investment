import React, { useEffect, useState } from 'react';
import './Profile.css';
import ApiService from '../services/api';

const Profile = () => {
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
            amount: Math.round(item.amountPurchased),
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
          // Extract number from message like "The notification period is 60 seconds"
          const match = data.message.match(/(\d+)/);
          if (match) {
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
    } else if (isNaN(purchaseData.amount) || parseInt(purchaseData.amount) <= 0) {
      errors.amount = 'Amount must be a positive integer';
    }
    
    if (!purchaseData.cost.trim()) {
      errors.cost = 'Cost is required';
    } else if (isNaN(purchaseData.cost) || parseFloat(purchaseData.cost) <= 0) {
      errors.cost = 'Cost must be a positive number';
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

  return (
    <section id="profile-section" className="profile-section">
      <div className="container">
        <div className="profile-header">
          <h2>Profile</h2>
        </div>
        
        <div className="profile-content">
          <div className="profile-info-section">
            <h3>Info</h3>
            <div className="profile-info">
              <div className="info-item">
                <span className="info-label">Username:</span>
                <span className="info-value">nelucristian</span>
              </div>
              <div className="info-item">
                <span className="info-label">Email address:</span>
                <span className="info-value">nelucristian2005@gmail.com</span>
              </div>
              
              <div className="info-item notification-item">
                <span className="info-label">Notification interval:</span>
                <div className="notification-controls">
                  <input
                    type="number"
                    className="notification-input"
                    placeholder="Enter days"
                    min="0"
                    value={notificationPeriod}
                    onChange={(e) => setNotificationPeriod(e.target.value)}
                    disabled={notificationLoading}
                  />
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
                                  <th>Amount</th>
                                  <th>Cost</th>
                                  <th>Current Price</th>
                                  <th>Value</th>
                                  <th>Profit</th>
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
                <label htmlFor="purchase-amount">Amount *</label>
                <input
                  type="number"
                  id="purchase-amount"
                  name="amount"
                  value={purchaseData.amount}
                  onChange={(e) => setPurchaseData({ ...purchaseData, amount: e.target.value })}
                  placeholder="Enter amount (positive integer)"
                  className={purchaseErrors.amount ? 'error' : ''}
                  required
                />
                {purchaseErrors.amount && (
                  <div className="error-message">{purchaseErrors.amount}</div>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="purchase-cost">Cost *</label>
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
                <label htmlFor="sell-amount">Amount *</label>
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
                <label htmlFor="sell-price">Price *</label>
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
    </section>
  );
};

export default Profile;
