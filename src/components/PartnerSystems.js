import React from 'react';
import './PartnerSystems.css';

const PartnerSystems = () => {
  const partners = [
    {
      name: 'Bloomberg',
      description: 'Financial data and analytics platform providing real-time market data and professional tools for financial professionals.',
      logo: null, // Bloomberg uses text logo
      website: 'https://www.bloomberg.com'
    },
    {
      name: 'Galmarley',
      description: 'Precious metals trading platform specializing in gold, silver, and other precious metals with real-time pricing and market insights.',
      logo: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjgwIiB2aWV3Qm94PSIwIDAgMTAwIDgwIiBmaWxsPSJub25lIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPgo8IS0tIE91dGVyIGZyYW1lIC0tPgo8cGF0aCBkPSJNMjAgMTBMMjAgNzBMMTAgODBMMTAgMjBMMjAgMTBaIiBzdHJva2U9IiNjY2NjY2MiIHN0cm9rZS13aWR0aD0iMiIgZmlsbD0ibm9uZSIvPgo8cGF0aCBkPSJNODAgMTBMODAgNzBMOTAgODBMOTAgMjBMODAgMTBaIiBzdHJva2U9IiNjY2NjY2MiIHN0cm9rZS13aWR0aD0iMiIgZmlsbD0ibm9uZSIvPgo8cGF0aCBkPSJNMjAgMTBMODAgMTBMOTAgMjBMMTAgMjBMMjAgMTBaIiBzdHJva2U9IiNjY2NjY2MiIHN0cm9rZS13aWR0aD0iMiIgZmlsbD0ibm9uZSIvPgo8IS0tIElubmVyIGN1YmUgLS0+CjxyZWN0IHg9IjM1IiB5PSIzMCIgd2lkdGg9IjMwIiBoZWlnaHQ9IjIwIiBmaWxsPSIjRkZEMDAwIi8+CjwhLS0gQXJyb3dzIC0tPgo8cGF0aCBkPSJNNzUgMTVMMzUgNDUiIHN0cm9rZT0iIzAwMDAwMCIgc3Ryb2tlLXdpZHRoPSIzIiBzdHJva2UtbGluZWNhcD0icm91bmQiLz4KPHBhdGggZD0iTTcwIDIwTDc1IDE1TDgwIDIwIiBzdHJva2U9IiMwMDAwMDAiIHN0cm9rZS13aWR0aD0iMyIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIi8+CjxwYXRoIGQ9Ik0zMCA2NUw3MCAzNSIgc3Ryb2tlPSIjMDAwMDAwIiBzdHJva2Utd2lkdGg9IjMiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIvPgo8cGF0aCBkPSJNMzUgNjBMMzAgNjVMMjUgNjAiIHN0cm9rZT0iIzAwMDAwMCIgc3Ryb2tlLXdpZHRoPSIzIiBzdHJva2UtbGluZWNhcD0icm91bmQiLz4KPC9zdmc+',
      website: 'https://www.galmarley.com'
    }
  ];

  return (
    <section id="partners" className="partners-section">
      <div className="container">
        <div className="material-card">
          <h2 className="partners-title">Partner Systems</h2>
          <div className="partners-grid">
            {partners.map((partner, index) => (
              <div key={index} className="partner-card">
                <div className="partner-logo">
                  {partner.logo ? (
                    <img 
                      src={partner.logo} 
                      alt={`${partner.name} logo`}
                      className="partner-logo-img"
                    />
                  ) : (
                    <div className="partner-text-logo">{partner.name}</div>
                  )}
                </div>
                <h3 className="partner-name">{partner.name}</h3>
                <p className="partner-description">{partner.description}</p>
                <a 
                  href={partner.website} 
                  target="_blank" 
                  rel="noopener noreferrer"
                  className="partner-link"
                >
                  Visit Website
                </a>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
};

export default PartnerSystems;
