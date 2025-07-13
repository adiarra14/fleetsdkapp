# Fleet Monitor UI

This is a React-based web dashboard for monitoring connected devices in your Fleet Gateway system.

## Features
- Device list and status dashboard
- Fetches device data from the backend REST API (`/api/reports`)
- Modern Material UI design

## Getting Started

1. Install dependencies:
   ```sh
   npm install
   ```
2. Start the development server:
   ```sh
   npm start
   ```
   The app will run at http://localhost:3000 by default.

3. To build for production:
   ```sh
   npm run build
   ```
   The static files will be in the `build/` directory.

## Deployment
- The production build can be served with Nginx, or copied to the backend's `static/` folder for Spring Boot to serve.
- Or, deploy as a separate container and reverse-proxy with Nginx or Traefik.

---
