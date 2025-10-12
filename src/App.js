import React from 'react';
import './App.css';
import Header from './components/Header';
import Hero from './components/Hero';
import TechnicalStack from './components/TechnicalStack';
import Footer from './components/Footer';

function App() {
  return (
    <div className="App">
      <Header />
      <Hero />
      <TechnicalStack />
      <Footer />
    </div>
  );
}

export default App;
