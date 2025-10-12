# Build Process Documentation

## Overview
This document explains the build and deployment process for the Metal Investment React UI.

## Build Commands

### Standard Build
```bash
npm run build
```
- Creates optimized production build in `build/` folder
- Generates new files with unique hashes (e.g., `main.abc123.js`)

### Deploy Build (Recommended)
```bash
npm run build:deploy
```
- Builds the React application
- Cleans old static files to prevent accumulation
- Copies new files to `src/main/resources/static/`
- Overwrites existing files instead of creating new ones

## File Management

### Before (Problem)
- Each build created new files: `main.123.js`, `main.456.js`, etc.
- Old files accumulated in static folder
- Wasted disk space and potential conflicts

### After (Solution)
- Old static files are cleaned before deployment
- New files overwrite existing ones
- Clean, organized static folder structure
- No file accumulation

## File Structure After Build
```
src/main/resources/static/
├── asset-manifest.json
├── favicon.ico
├── index.html
└── static/
    ├── css/
    │   ├── main.[hash].css
    │   └── main.[hash].css.map
    └── js/
        ├── main.[hash].js
        ├── main.[hash].js.LICENSE.txt
        └── main.[hash].js.map
```

## Usage
Always use `npm run build:deploy` for production deployments to ensure clean file management.
