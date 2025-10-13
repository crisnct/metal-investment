import React from 'react';
import './Profile.css';

const Profile = () => {
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
                  <div className="last-updated">
                    Last Updated: 10/13/2025, 10:03:55 AM
                  </div>
                  <div className="no-investments">
                    No metal investments recorded yet.
                  </div>
                </div>
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
    </section>
  );
};

export default Profile;
