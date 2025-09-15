# Supply Chain Risk Monitor Backend

This is the Spring Boot backend for your real-time supply chain risk monitoring platform.

## Features
- REST API for suppliers, risk scores, alerts
- PostgreSQL + MongoDB integration
- WebSocket endpoint for real-time alerts
- Ready for local development

## How to Run
1. Install Java 17+ and Maven
2. Configure your DB connection in `src/main/resources/application.properties`
3. Run:
   ```sh
   mvn spring-boot:run
   ```

## Project Structure
- `Supplier`, `RiskScore`, `Alert` entities
- REST controllers in `controller/`
- Data repositories in `repository/`
- Service layer in `service/`
- WebSocket config in `config/`

---
Frontend: See `/src` for React/Supabase dashboard
