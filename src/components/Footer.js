import React from 'react';
import { Heart, Github, ExternalLink } from 'lucide-react';
import './Footer.css';

const Footer = () => {
  return (
    <footer className="footer">
      <div className="container">
        <div className="footer-content">
          <div className="footer-section">
            <h3 className="footer-title">Metal Investment</h3>
            <p className="footer-description">
              Monitor precious metals prices and track your investments with real-time data 
              from Bloomberg and Galmarley.
            </p>
            <div className="github-link">
              <a 
                href="https://github.com/crisnct/metal-investment" 
                target="_blank" 
                rel="noopener noreferrer"
                className="github-repo-link"
              >
                <Github className="github-icon" />
                View on GitHub
                <ExternalLink className="external-icon" />
              </a>
            </div>
          </div>
          
          <div className="footer-section">
            <h4 className="footer-subtitle">Quick Links</h4>
            <ul className="footer-links">
              <li><a href="#home">Home</a></li>
              <li><a href="#features">Key Features</a></li>
              <li><a href="#tech">Technology</a></li>
              <li><a href="https://metal-investment-635786220311.europe-west1.run.app/swagger-ui.html" target="_blank" rel="noopener noreferrer">Swagger</a></li>
            </ul>
          </div>
          
          <div className="footer-section">
            <h4 className="footer-subtitle">Contact</h4>
            <div className="contact-info">
              <p><strong>Cristian Țone</strong></p>
              <p>
                <a href="mailto:nelucristian2005@gmail.com" className="contact-link">
                  nelucristian2005@gmail.com
                </a>
              </p>
              <p>
                <a href="https://www.cristiantone.me" target="_blank" rel="noopener noreferrer" className="contact-link">
                  www.cristiantone.me
                </a>
              </p>
            </div>
          </div>
          
        </div>
        
        <div className="footer-bottom">
          <div className="footer-bottom-content">
            <p className="footer-copyright">
              Made with <Heart className="heart-icon" /> for precious metals investors
            </p>
            <div className="footer-status">
              <div className="status-indicator">
                <div className="status-dot"></div>
                <span>API Available</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
