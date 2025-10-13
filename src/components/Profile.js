import React, { useEffect, useState } from 'react';
import './Profile.css';
import ApiService from '../services/api';

const Profile = () => {
  const [showPurchaseDialog, setShowPurchaseDialog] = useState(false);
  const [purchaseData, setPurchaseData] = useState({ symbol: '', amount: '', cost: '' });
  const [purchaseErrors, setPurchaseErrors] = useState({});
  const [purchaseMessage, setPurchaseMessage] = useState('');
  const [profitLoading, setProfitLoading] = useState(false);
  const [profitError, setProfitError] = useState('');
  const [profitLastUpdated, setProfitLastUpdated] = useState('');
  const [profitEntries, setProfitEntries] = useState([]);

  useEffect(() => {
    const loadProfit = async () => {
      try {
        setProfitLoading(true);
        setProfitError('');
        const data = await ApiService.getProfit();
        // Try to normalize response
        let entries = [];
        let lastUpdatedText = '';

        if (Array.isArray(data)) {
          entries = data;
        } else if (data && Array.isArray(data.entries)) {
          entries = data.entries;
          lastUpdatedText = data.lastUpdated || '';
        } else if (data && Array.isArray(data.items)) {
          entries = data.items;
          lastUpdatedText = data.lastUpdated || '';
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
      
      // Close dialog after 2 seconds
      setTimeout(() => {
        closePurchaseDialog();
      }, 2000);
      
    } catch (error) {
      setPurchaseMessage(error.data?.message || error.message || 'Failed to record purchase');
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
            </div>
          </div>

              <div className="profile-investment-section">
                <h3>Investment</h3>
                <div className="investment-content">
                  <div className="profit-section">
                    <h4>Profit</h4>
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
                                      <td>{row.symbol || row.metalSymbol || '-'}</td>
                                      <td>{row.amount ?? row.metalAmount ?? '-'}</td>
                                      <td>{row.cost ?? row.totalCost ?? '-'}</td>
                                      <td>{row.currentPrice ?? '-'}</td>
                                      <td>{row.value ?? row.currentValue ?? '-'}</td>
                                      <td>{row.profit ?? row.profitValue ?? '-'}</td>
                                      <td>{row.date || row.timestamp || '-'}</td>
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
                    <button className="btn-purchase" onClick={handlePurchaseClick}>
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
    </section>
  );
};

export default Profile;
