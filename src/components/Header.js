import React from 'react';
import { TrendingUp, Menu, X } from 'lucide-react';
import './Header.css';

const Header = () => {
  const [isMenuOpen, setIsMenuOpen] = React.useState(false);

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
            <a href="#contact" className="nav-link">Contact</a>
          </nav>
          
          <button 
            className="menu-toggle"
            onClick={() => setIsMenuOpen(!isMenuOpen)}
            aria-label="Toggle menu"
          >
            {isMenuOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>
      </div>
    </header>
  );
};

export default Header;
