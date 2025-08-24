# Batch Processing of Financial Transactions

## Overview
This project implements a **Batch Processing System** for **financial transactions** using **Spring Boot** and **Spring Batch**. The system processes large volumes of financial transactions, validates them, detects fraudulent transactions, and generates detailed reports. It uses **Spring Data JPA** for database interactions and **Springdoc OpenAPI** for API documentation.

## Features
- Batch processing of financial transactions.
- Validation of transactions based on business rules.
- Fraud detection with scoring for suspicious activities.
- Generation of reports and email notifications on job completion.
- RESTful API for manual job execution and status monitoring.
- Swagger UI for interactive API documentation.
  
## Technologies Used
- **Spring Boot 3.x**
- **Spring Batch**
- **Spring Data JPA**
- **Springdoc OpenAPI** for Swagger UI
- **H2 Database** for development (MySQL for production)
- **JUnit** for testing
- **JavaMailSender** for email notifications

## Setup and Installation

### Prerequisites
- **Java 17** or higher.
- **Maven** for dependency management.
- **MySQL** (for production) or **H2 Database** (for development).

