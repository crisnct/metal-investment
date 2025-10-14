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
  AlertTriangle,
  Code,
  FileText,
  Wrench
} from 'lucide-react';
import './TechnicalStack.css';

const TechnicalStack = () => {
  const technologies = [
    {
      name: 'Java 25',
      description: 'Core programming language for the application logic.',
      icon: Coffee,
      color: '#e11d48'
    },
    {
      name: 'Spring Boot 3.5.6',
      description: 'Framework for backend of a webapp with typical starters: web, data‑JPA, security, mail, actuator.',
      icon: Shield,
      color: '#65a30d'
    },
    {
      name: 'React 18.2.0',
      description: 'Frontend library for building user interfaces with component-based architecture.',
      icon: Code,
      color: '#61dafb'
    },
    {
      name: 'Swagger 2.8.13',
      description: 'API documentation and testing interface using SpringDoc OpenAPI for interactive API exploration.',
      icon: FileText,
      color: '#85ea2d'
    },
    {
      name: 'Lombok 1.18.40',
      description: 'Java library that automatically generates boilerplate code like getters, setters, constructors, and equals methods.',
      icon: Wrench,
      color: '#ff6b35'
    },
    {
      name: 'Google Cloud Platform',
      description: 'Cloud deployment platform with Cloud Run, Cloud SQL, and scalable infrastructure.',
      icon: Cloud,
      color: '#4285f4'
    },
    {
      name: 'MySQL 8.0',
      description: 'Database.',
      icon: Database,
      color: '#0ea5e9'
    },
    {
      name: 'Liquibase 4.31.1',
      description: 'Library for tracking, managing and applying database schema changes.',
      icon: Database,
      color: '#0891b2'
    },
    {
      name: 'Resilience4j 2.2.0',
      description: 'Lightweight fault tolerance library that implements resilience patterns, like circuit breakers, rate limiters, and retries.',
      icon: Zap,
      color: '#dc2626'
    },
    {
      name: 'Unirest 3.14.5',
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
      name: 'Hibernate 6.6.25',
      description: 'Caching integration for database.',
      icon: Database,
      color: '#059669'
    },
    {
      name: 'Maven 3.14.0',
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
