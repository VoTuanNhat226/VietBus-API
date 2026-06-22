# VietBus API

VietBus is a comprehensive Backend API designed for managing a passenger transport company. It provides a robust set of features to handle core business operations including ticketing, trip management, vehicles, employees, and secure payment integrations.

## 🚀 Features

- **User & Role Management**: Manage accounts for Employees, Passengers, and System Administrators with secure Role-Based Access Control (RBAC).
- **Authentication & Security**: Integrated Spring Security with JWT (JSON Web Tokens) for access control and refresh token families for enhanced security.
- **Trip & Route Management**: Define routes, stations, and schedule trips. Keep track of trip histories and assigned employees (drivers, assistants).
- **Vehicle & Seat Management**: Manage vehicle details and dynamically track seat availability (`TripSeatEntity`) for specific trips.
- **Ticketing System**: Complete ticket booking workflow, supporting ticket reservations, payment tracking, and QR code generation for e-tickets.
- **Payment Gateway Integration**: Seamless integration with **VNPay** and **MoMo** payment gateways, including Webhook/IPN handlers for real-time payment status updates.
- **Caching**: High-performance data retrieval using **Redis** caching.
- **Email Notifications**: Automated email sending for ticket confirmations and system alerts.
- **API Documentation**: Interactive REST API documentation powered by Swagger/OpenAPI 3.

## 🛠️ Technology Stack

- **Core**: Java 17, Spring Boot 3.3.5
- **Database**: PostgreSQL (via Spring Data JPA / Hibernate)
- **Caching**: Redis (Spring Data Redis)
- **Security**: Spring Security, jjwt (0.11.5)
- **Utilities**: 
  - Lombok (Boilerplate reduction)
  - ZXing (QR Code generation)
  - Spring Boot Mail (SMTP Integration)
  - Apache HttpClient 5
- **Documentation**: Springdoc OpenAPI (Swagger UI)

## ⚙️ Configuration & Environment Variables

The application relies on several environment variables for configuration. You can configure them in your environment or update the `application-dev.yml` file.

| Variable Name | Description | Default / Example |
|---|---|---|
| `PORT` | Application server port | `8088` |
| `DB_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/vietbus` |
| `DB_USERNAME` | PostgreSQL username | `postgres` |
| `DB_PASSWORD` | PostgreSQL password | `password` |
| `REDIS_URL` | Redis connection URL | `redis://localhost:6379` |
| `JWT_SECRET` | Secret key for JWT signing | *(Base64 encoded secret)* |
| `MAIL_USERNAME` | SMTP Email address | `your-email@gmail.com` |
| `MAIL_PASSWORD` | SMTP Email App Password | `xxxx xxxx xxxx xxxx` |
| `VNPAY_HASH_SECRET` | Secret key for VNPay hashing | *(Provided by VNPay)* |
| `MOMO_SECRET_KEY` | Secret key for MoMo | *(Provided by MoMo)* |

## 🚀 Getting Started

### Prerequisites

- Java Development Kit (JDK) 17
- Maven 3.8+
- PostgreSQL database
- Redis server

### Installation & Setup

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd VietBus-API
   ```

2. **Configure Database & Redis:**
   Ensure your PostgreSQL and Redis instances are running. Update the connection details in `src/main/resources/application-dev.yml` or set the appropriate environment variables.

3. **Build the project:**
   ```bash
   mvn clean install
   ```

4. **Run the application:**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

   The application will start on `http://localhost:8088`.

## 📚 API Documentation

Once the application is running, you can access the interactive Swagger UI documentation at:
```
http://localhost:8088/swagger-ui.html
```
*(Note: Ensure your `server.port` configuration is matched if you changed it)*

## 📂 Project Structure

```
src/main/java/com/vtn/
├── config/        # Application configurations (Security, Redis, Swagger, etc.)
├── constant/      # System-wide constants
├── controller/    # REST API Endpoints
├── dto/           # Data Transfer Objects for API requests/responses
├── entity/        # JPA Domain Models (Account, Trip, Ticket, etc.)
├── enumdef/       # Enums representing statuses, types, and roles
├── exception/     # Global exception handlers and custom exception classes
├── repository/    # Spring Data JPA repositories
├── security/      # Security filters, JWT providers, and UserDetails
├── service/       # Business logic layer (including VNPay/MoMo integrations)
└── utils/         # Helper utility classes (e.g., Signature generation)
```

## 🤝 Contributing

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the appropriate commercial or open-source license. Please refer to the specific license file included in the repository (if applicable).