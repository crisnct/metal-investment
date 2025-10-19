# Metal Investment - React UI

A modern, responsive React UI for the Metal Investment application that displays the project information and technical stack.

## Features

- **Modern Design**: Clean, glassmorphism-inspired design with gradient backgrounds
- **Responsive Layout**: Works perfectly on desktop, tablet, and mobile devices
- **Interactive Components**: Smooth animations and hover effects
- **Technical Stack Display**: Beautiful cards showing all technologies used
- **API Integration**: Direct links to the backend API endpoints
- **Health Monitoring**: Real-time API health status indicator

## Components

### Header
- Responsive navigation with mobile menu
- Logo with trending up icon
- Smooth scroll navigation

### Hero Section
- Eye-catching title with gradient text
- Project description
- Feature highlights with icons
- Call-to-action button for API health check

### Technical Stack
- Grid layout of technology cards
- Each technology has its own icon and color
- Hover effects and animations
- Deployment information with GCP status

### Footer
- Quick links to main sections
- API endpoint links
- Real-time status indicator
- Responsive design

## Technologies Used

- **React 18**: Modern React with hooks
- **Lucide React**: Beautiful, customizable icons
- **CSS3**: Modern CSS with flexbox, grid, and animations
- **Responsive Design**: Mobile-first approach

## Getting Started

### Prerequisites
- Node.js 16+ 
- npm or yarn

### Installation

1. Install dependencies:
```bash
npm install
```

2. Start the development server:
```bash
npm start
```

3. Open [http://localhost:3000](http://localhost:3000) to view it in the browser.

### Building for Production

#### Standard Build
```bash
npm run build
```
- Creates optimized production build in `build/` folder
- Generates new files with unique hashes (e.g., `main.abc123.js`)

#### Deploy Build (Recommended)
```bash
npm run build:deploy
```
- Builds the React application
- Cleans old static files to prevent accumulation
- Copies new files to `src/main/resources/static/`
- Overwrites existing files instead of creating new ones

#### File Management

**Before (Problem)**
- Each build created new files: `main.123.js`, `main.456.js`, etc.
- Old files accumulated in static folder
- Wasted disk space and potential conflicts

**After (Solution)**
- Old static files are cleaned before deployment
- New files overwrite existing ones
- Clean, organized static folder structure
- No file accumulation

#### File Structure After Build
```
src/main/resources/static/
├── asset-manifest.json
├── index.html
└── static/
    ├── css/
    │   ├── main.[hash].css
    │   └── main.[hash].css.map
    └── js/
    │   ├── main.[hash].js
    │   ├── main.[hash].js.LICENSE.txt
    │   └── main.[hash].js.map
    └── images/        
        └── metal-investment-icon.svg
```

**Usage**: Always use `npm run build:deploy` for production deployments to ensure clean file management.

## Deployment

The UI can be deployed to any static hosting service like:
- Netlify
- Vercel
- GitHub Pages
- AWS S3 + CloudFront
- Google Cloud Storage

## API Integration

The UI includes direct links to the backend API:
- Health Check: `/actuator/health`
- User Registration: `/userRegistration`
- Login: `/login`
- Account Validation: `/validateAccount`

## Customization

### Colors
The main color scheme uses:
- Primary: Gold gradient (#fbbf24 to #f59e0b)
- Background: Blue-purple gradient (#667eea to #764ba2)
- Text: White and dark gray variants

### Icons
All icons are from Lucide React and can be easily customized by changing the icon components.

### Layout
The layout is fully responsive and uses CSS Grid and Flexbox for modern layouts.

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)
- Mobile browsers (iOS Safari, Chrome Mobile)

## Performance

- Optimized bundle size
- Lazy loading ready
- Smooth animations with CSS transforms
- Efficient re-renders with React hooks
