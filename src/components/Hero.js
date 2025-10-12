import React from 'react';
import { Shield, Mail, BarChart3, Globe } from 'lucide-react';
import './Hero.css';

const Hero = () => {
  return (
    <section id="home" className="hero">
      <div className="container">
        <div className="hero-content">
          <div className="hero-text">
            <h1 className="hero-title">
              Metal Investment
            </h1>
            <p className="hero-description">
              This project is for Revolut users from Romania who invested in gold, silver or platinum. 
              The project is monitoring the precious metals price at Bloomberg/Galmarley, and it's using 
              some formula to approximate the metal price at Revolut. If the price of the metal is so high 
              so your profit would match a logical expression that you provided, then the application would 
              notify you by email. Also the users could check whenever they want their profit by calling an API.
            </p>
            <div className="hero-features">
              <div className="feature">
                <Shield className="feature-icon" />
                <span>Secure Investment Tracking</span>
              </div>
              <div className="feature">
                <Mail className="feature-icon" />
                <span>Email Notifications</span>
              </div>
              <div className="feature">
                <BarChart3 className="feature-icon" />
                <span>Real-time Price Monitoring</span>
              </div>
              <div className="feature">
                <Globe className="feature-icon" />
                <span>Global Market Data</span>
              </div>
            </div>
            <div className="hero-cta">
              <a 
                href="https://metal-investment-635786220311.europe-west1.run.app/actuator/health" 
                target="_blank" 
                rel="noopener noreferrer"
                className="cta-button"
              >
                Check API Health
              </a>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default Hero;
