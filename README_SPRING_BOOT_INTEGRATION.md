# Spring Boot Integration Guide

## Overview
This guide helps you integrate the React components with a Spring Boot backend for the Supply Chain Risk Monitor.

## Database Setup
1. Use the `database/postgresql_schema.sql` file to set up your PostgreSQL database
2. Configure your Spring Boot application.properties:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/supply_chain_risk_monitor
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=validate
