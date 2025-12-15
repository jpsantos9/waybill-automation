# Lazada Seller Center Selenium Spring Boot Project

This project is a Spring Boot application that uses Selenium WebDriver to automate login to https://sellercenter.lazada.com.ph/. It includes a REST controller to trigger the login process.

## Features
- Spring Boot REST API
- Selenium WebDriver integration
- Endpoint to execute login automation

## Usage
1. Configure your Selenium WebDriver (e.g., ChromeDriver).
2. Start the Spring Boot application.
3. Use the provided REST endpoint to trigger the login automation.

## Directory Structure
- `src/main/java` - Application source code
- `src/main/resources` - Configuration files
- `src/test/java` - Test code

## Requirements
- Java 17 or later
- Maven
- ChromeDriver (or another WebDriver)

## How to Run
1. Install dependencies: `mvn install`
2. Run the application: `mvn spring-boot:run`
3. Access the REST endpoint: `POST /api/login` with credentials in the request body

---
This README will be updated as the project is developed.
