import React from 'react';
import { 
  Coffee, 
  Database, 
  Shield, 
  Zap, 
  Globe, 
  GitBranch, 
  Cloud,
  CheckCircle,
  AlertTriangle
} from 'lucide-react';
import './TechnicalStack.css';

const TechnicalStack = () => {
  const technologies = [
    {
      name: 'JDK 22',
      description: 'Core programming language for the application logic.',
      icon: Coffee,
      color: '#e11d48'
    },
    {
      name: 'Spring Boot',
      description: 'Framework for backend of a webapp with typical starters: web, data‑JPA, security, mail, actuator.',
      icon: Shield,
      color: '#65a30d'
    },
    {
      name: 'Liquibase',
      description: 'Library for tracking, managing and applying database schema changes.',
      icon: Database,
      color: '#0891b2'
    },
    {
      name: 'Resilience4j',
      description: 'Lightweight fault tolerance library that implements resilience patterns, like circuit breakers, rate limiters, and retries.',
      icon: Zap,
      color: '#dc2626'
    },
    {
      name: 'Unirest',
      description: 'HTTP client library for making API calls to fetch real-time metal prices.',
      icon: Globe,
      color: '#7c3aed'
    },
    {
      name: 'External APIs',
      description: 'Bloomberg and Galmarley APIs for fetching real-time precious metal prices with market data integration.',
      icon: Globe,
      color: '#ea580c'
    },
    {
      name: 'Google Cloud Platform',
      description: 'Cloud deployment platform with Cloud Run, Cloud SQL, and scalable infrastructure.',
      icon: Cloud,
      color: '#4285f4'
    },
    {
      name: 'Hibernate/Ehcache',
      description: 'Caching integration for database.',
      icon: Database,
      color: '#059669'
    },
    {
      name: 'MySQL',
      description: 'Database.',
      icon: Database,
      color: '#0ea5e9'
    },
    {
      name: 'Maven',
      description: 'Build automation tool for project management and dependency handling.',
      icon: GitBranch,
      color: '#dc2626'
    },
    {
      name: 'Git',
      description: 'Version control system for collaborative development and code management.',
      icon: GitBranch,
      color: '#f59e0b'
    }
  ];

  return (
    <section id="tech" className="tech-section">
      <div className="container">
        <div className="material-card">
          <h2 className="tech-title">Technical Stack</h2>
          <div className="tech-grid">
            {technologies.map((tech, index) => (
              <div key={index} className="tech-card">
                <div className="tech-icon" style={{ color: tech.color }}>
                  <tech.icon size={32} />
                </div>
                <h3 className="tech-name">{tech.name}</h3>
                <p className="tech-description">{tech.description}</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
};

export default TechnicalStack;
