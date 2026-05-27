# Personal Finance Manager

A comprehensive web service built using Java and Spring Boot 3.x to help users manage their personal finances. The system supports secure session-based cookie authentication, dynamic categories, transactions tracking with filters, savings goals with progress calculations, and financial reports.

## Features
- **User Management & Authentication**: Programmatic registration, session-based authentication with secure cookies, and session invalidation on logout.
- **Strict Data Isolation**: Users only have access to their own custom categories, transactions, savings goals, and financial reports.
- **Dynamic Categories**: Predefined default categories (INCOME: Salary; EXPENSE: Food, Rent, Transportation, Entertainment, Healthcare, Utilities) and custom category creation/deletion with usage protection.
- **Transaction Management**: Full CRUD on transactions, date restriction on updates (date field cannot be changed after creation), and rich query filters (by date range, category, categoryId, or type).
- **Savings Goals**: Dynamic progress tracking (current progress calculated as total income minus total expenses since the goal's start date), progress percentage, and remaining amount.
- **Financial Reports**: Grouped monthly and yearly summaries of income and expenses by category with net savings.

---

## Getting Started

### Prerequisites
- **Java 17** or higher
- **Maven 3.6+**

### Running Locally with Maven
1. **Clone or navigate** to the project directory:
   ```bash
   cd "c:\Users\harsh\Desktop\finance manger"
   ```
2. **Build the project** and run the unit tests:
   ```bash
   mvn clean package
   ```
3. **Run the Spring Boot application**:
   ```bash
   mvn spring-boot:run
   ```
   The application will start on **port 8000** with the context path `/api` (base URL: `http://localhost:8000/api`).

---

### Running with Docker
A multi-stage `Dockerfile` is provided for containerized compilation and execution.
1. **Build the Docker image**:
   ```bash
   docker build -t personal-finance-manager .
   ```
2. **Run the container**:
   ```bash
   docker run -p 8000:8000 personal-finance-manager
   ```

---

## API Specification

All endpoints are protected and require a valid session cookie (`JSESSIONID`) except registration and login.

### 1. Authentication
* **Register User** (`POST /api/auth/register`)
  - Payload: `{ "username": "user@example.com", "password": "password123", "fullName": "John Doe", "phoneNumber": "+1234567890" }`
  - Response: `201 Created` with `{ "message": "User registered successfully", "userId": 1 }`
* **Login** (`POST /api/auth/login`)
  - Payload: `{ "username": "user@example.com", "password": "password123" }`
  - Response: `200 OK` with `{ "message": "Login successful" }` (returns session cookie)
* **Logout** (`POST /api/auth/logout`)
  - Response: `200 OK` with `{ "message": "Logout successful" }`

### 2. Categories
* **Get All Categories** (`GET /api/categories`)
  - Response: `200 OK` with `{ "categories": [ { "name": "Salary", "type": "INCOME", "isCustom": false, "custom": false }, ... ] }`
* **Create Custom Category** (`POST /api/categories`)
  - Payload: `{ "name": "Freelance", "type": "INCOME" }`
  - Response: `201 Created` with `{ "name": "Freelance", "type": "INCOME", "isCustom": true, "custom": true }`
* **Delete Custom Category** (`DELETE /api/categories/{name}`)
  - Response: `200 OK` with `{ "message": "Category deleted successfully" }`

### 3. Transactions
* **Create Transaction** (`POST /api/transactions`)
  - Payload: `{ "amount": 50000.00, "date": "2024-01-15", "category": "Salary", "description": "January Salary" }`
  - Response: `201 Created` with `{ "id": 1, "amount": 50000.00, "date": "2024-01-15", "category": "Salary", "description": "January Salary", "type": "INCOME" }`
* **Get Transactions** (`GET /api/transactions`)
  - Parameters (optional): `startDate`, `endDate`, `categoryId`, `category` (name), `type`
  - Response: `200 OK` with `{ "transactions": [ ... ] }` sorted newest first
* **Update Transaction** (`PUT /api/transactions/{id}`)
  - Payload (ignores date updates): `{ "amount": 60000.00, "description": "Updated description" }`
  - Response: `200 OK` with updated transaction
* **Delete Transaction** (`DELETE /api/transactions/{id}`)
  - Response: `200 OK` with `{ "message": "Transaction deleted successfully" }`

### 4. Savings Goals
* **Create Goal** (`POST /api/goals`)
  - Payload: `{ "goalName": "Emergency Fund", "targetAmount": 5000.00, "targetDate": "2027-01-01", "startDate": "2024-01-01" }`
  - Response: `201 Created` with `{ "id": 1, "goalName": "Emergency Fund", "targetAmount": 5000.00, "targetDate": "2027-01-01", "startDate": "2024-01-01", "currentProgress": 1000.00, "progressPercentage": 20.0, "remainingAmount": 4000.00 }`
* **Get All Goals** (`GET /api/goals`)
  - Response: `200 OK` with list of goals and computed dynamic progress metrics
* **Update Goal** (`PUT /api/goals/{id}`)
  - Payload: `{ "targetAmount": 6000.00, "targetDate": "2027-02-01" }`
  - Response: `200 OK` with updated goal and recalculated progress
* **Delete Goal** (`DELETE /api/goals/{id}`)
  - Response: `200 OK` with `{ "message": "Goal deleted successfully" }`

### 5. Financial Reports
* **Monthly Report** (`GET /api/reports/monthly/{year}/{month}`)
  - Response: `200 OK` with `{ "month": 1, "year": 2024, "totalIncome": { "Salary": 3000.00 }, "totalExpenses": { "Food": 400.00 }, "netSavings": 2600.00 }`
* **Yearly Report** (`GET /api/reports/yearly/{year}`)
  - Response: `200 OK` with aggregated annual category sums and net savings

---

## Design Decisions
1. **Layered Architecture**: Designed cleanly with Controller, Service, and Repository patterns to make it modular and easily understandable.
2. **Session-based Security with Cookies**: Used Spring Security's native context and HTTP session storage to serve authenticated users and keep sessions marked `HttpOnly` and `Secure`.
3. **Double Serialization Mismatch Handling**: The category response includes both `isCustom` (requested in the assignment specifications) and `custom` (expected by the test script assertions) to guarantee 100% test compatibility.
4. **Calculated Savings Progress Capping**: Capped dynamic savings progress at 0 if the net calculated savings is negative, preserving the logical meaning of personal savings.
