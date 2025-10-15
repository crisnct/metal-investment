import React from 'react';
import { Cloud, GitBranch, Code, Wrench } from 'lucide-react';
import './DevOps.css';

const DevOps = () => {
  const devopsTechnologies = [
    {
      name: 'Google Cloud Platform',
      description: 'Cloud deployment platform with Cloud Run, Cloud SQL, and scalable infrastructure.',
      icon: Cloud,
      color: '#4285f4'
    },
    {
      name: 'Git',
      description: 'Version control system for collaborative development and code management.',
      icon: GitBranch,
      color: '#f59e0b'
    },
    {
      name: 'Maven 3.14.0',
      description: 'Build automation tool for project management and dependency handling.',
      icon: Wrench,
      color: '#dc2626'
    },
    {
      name: 'IntelliJ IDEA',
      description: 'Integrated Development Environment (IDE) for Java development with advanced code analysis and debugging tools.',
      icon: Code,
      color: '#000000'
    }
  ];

  return (
    <section id="devops" className="devops-section">
      <div className="container">
        <div className="material-card">
          <h2 className="devops-title">DEVOPS</h2>
          <div className="devops-grid">
            {devopsTechnologies.map((tech, index) => (
              <div key={index} className="devops-card">
                <div className="devops-icon" style={{ color: tech.color }}>
                  <tech.icon size={32} />
                </div>
                <h3 className="devops-name">{tech.name}</h3>
                <p className="devops-description">{tech.description}</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
};

export default DevOps;
