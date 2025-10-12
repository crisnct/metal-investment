import React from 'react';
import { Bell, TrendingUp, BarChart3, Smartphone, Globe, Mail } from 'lucide-react';
import './Features.css';

const Features = () => {
  const features = [
    {
      icon: <Bell className="feature-icon" />,
      title: "Smart Alerts",
      description: "Set up custom alerts for price changes and market movements. Get notified instantly when your target prices are reached.",
      color: "#3B82F6",
      gradient: "linear-gradient(135deg, #3B82F6, #1D4ED8)"
    },
    {
      icon: <TrendingUp className="feature-icon" />,
      title: "Profit Tracking",
      description: "Monitor your investment performance with real-time profit calculations and detailed analytics.",
      color: "#10B981",
      gradient: "linear-gradient(135deg, #10B981, #059669)"
    },
    {
      icon: <Mail className="feature-icon" />,
      title: "Email Notifications",
      description: "Receive instant email notifications when your target prices are reached or market conditions change.",
      color: "#F59E0B",
      gradient: "linear-gradient(135deg, #F59E0B, #D97706)"
    },
    {
      icon: <BarChart3 className="feature-icon" />,
      title: "Market Analysis",
      description: "Access real-time market data from Bloomberg and Galmarley to make informed investment decisions.",
      color: "#8B5CF6",
      gradient: "linear-gradient(135deg, #8B5CF6, #7C3AED)"
    },
    {
      icon: <Smartphone className="feature-icon" />,
      title: "Mobile Ready",
      description: "Responsive design that works perfectly on all devices - desktop, tablet, and mobile.",
      color: "#EF4444",
      gradient: "linear-gradient(135deg, #EF4444, #DC2626)"
    },
    {
      icon: <Globe className="feature-icon" />,
      title: "Real-time Price Monitoring",
      description: "Track precious metals prices from global markets with real-time updates and historical data.",
      color: "#06B6D4",
      gradient: "linear-gradient(135deg, #06B6D4, #0891B2)"
    }
  ];

  return (
    <section id="features" className="features">
      <div className="container">
        <div className="material-card">
          <div className="features-header">
            <h2 className="features-title">Key Features</h2>
            <p className="features-subtitle">
              Everything you need to manage your precious metals investments effectively
            </p>
          </div>
          
          <div className="features-grid">
            {features.map((feature, index) => (
              <div key={index} className="feature-card">
                <div 
                  className="feature-card-icon"
                  style={{ background: feature.gradient }}
                >
                  {feature.icon}
                </div>
                <div className="feature-card-content">
                  <h3 className="feature-card-title">{feature.title}</h3>
                  <p className="feature-card-description">{feature.description}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
};

export default Features;
