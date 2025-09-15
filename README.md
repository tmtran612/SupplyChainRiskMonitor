# Supply Chain Risk Monitor

A full-stack, weather-driven supplier risk scoring dashboard for supply chain resilience and alerting.

## Features
- Automated supplier risk scoring (baseline + weather impact)
- Real-time weather integration (OpenWeather API)
- Geocoding for supplier locations
- PostgreSQL backend (Supabase compatible)
- Spring Boot 3.2.x backend (Java)
- Next.js/React frontend
- REST API endpoints for risk, events, alerts, admin
- Dashboard with top risk suppliers, recent events, and alert deduplication
- Supports exactly five canonical suppliers (sample set), easily extensible

## Quick Start

### Backend (Spring Boot)
1. **Configure database:**
   - PostgreSQL (Supabase recommended)
   - Set credentials in `backend/src/main/resources/application.properties`
2. **Seed suppliers:**
   - Use provided SQL or admin endpoints to insert sample suppliers
3. **Run backend:**
   ```powershell
   cd backend
   mvn spring-boot:run
   ```
4. **Trigger baseline and weather fetch:**
   ```powershell
   Invoke-RestMethod -Method POST -Uri http://localhost:8080/api/admin/manual-initialize-baseline-scores
   Invoke-RestMethod -Method POST -Uri http://localhost:8080/api/admin/trigger-weather-fetch
   ```

### Frontend (Next.js)
1. **Install dependencies:**
   ```powershell
   pnpm install
   ```
2. **Run frontend:**
   ```powershell
   pnpm dev
   ```
3. **Access dashboard:**
   - Visit [http://localhost:3000](http://localhost:3000) or [http://localhost:5173](http://localhost:5173)

## API Endpoints
- `/api/suppliers/risk` — Get latest risk scores for all suppliers
- `/api/admin/manual-initialize-baseline-scores` — Create baseline scores
- `/api/admin/trigger-weather-fetch` — Fetch weather and adjust scores
- `/api/admin/debug-status` — Get counts for suppliers, scores, alerts, events
- `/api/alerts/active` — List active alerts

## Customization
- Add suppliers via SQL or admin endpoints
- Adjust weather impact weights in `ExternalDataFetcherService.java`
- Change refresh frequency via `@Scheduled` annotation
- Extend dashboard UI in `src/components`

## Development Notes
- Backend: Java 17+, Maven
- Frontend: Node.js 18+, pnpm
- Database: PostgreSQL (UUID PKs)
- Weather API: OpenWeather (API key required)

## License
MIT

---
For questions or contributions, open an issue or pull request on GitHub.
