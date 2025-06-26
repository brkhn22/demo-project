# Demo Project - Employee Management System

A comprehensive Spring Boot REST API application for managing employees, departments, companies, and organizational hierarchies with authentication and authorization features.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Authentication](#authentication)
- [Database Schema](#database-schema)
- [Configuration](#configuration)
- [Deployment](#deployment)
- [Contributing](#contributing)

## 🎯 Overview

This project is a complete enterprise-level employee management system that provides:
- User authentication and authorization
- Hierarchical department management
- Company and location management
- Role-based access control
- Email verification system
- Comprehensive REST API endpoints

## ✨ Features

### 🔐 Authentication & Authorization
- JWT-based authentication
- Email verification for new users
- Password reset functionality
- Role-based access control (Admin, Manager, Employee)
- Account activation via email

### 👥 User Management
- User registration and profile management
- Soft delete functionality
- Department-based user organization
- Manager-specific user access

### 🏢 Organization Management
- **Companies**: Multi-company support with types and locations
- **Departments**: Hierarchical department structure
- **Locations**: City/Region/Town geographical organization
- **Department Hierarchy**: Parent-child department relationships

### 🌍 Location Management
- Three-tier location system (City → Region → Town)
- Soft delete with cascading rules
- Active location validation

## 🛠 Tech Stack

- **Framework**: Spring Boot 3.x
- **Security**: Spring Security with JWT
- **Database**: JPA/Hibernate with MySQL/PostgreSQL
- **Documentation**: OpenAPI 3 (Swagger)
- **Email**: JavaMailSender
- **Build Tool**: Maven
- **Java Version**: 17+

## 🏗 Architecture

```
src/
├── main/
│   ├── java/com/example/demo_project/
│   │   ├── auth/                    # Authentication & JWT
│   │   ├── config/                  # Security & App Configuration
│   │   ├── email/                   # Email services
│   │   └── user/                    # User & Organization modules
│   │       ├── company/             # Company management
│   │       ├── department/          # Department management
│   │       │   └── department_hierarchy/ # Department relationships
│   │       └── town/               # Location management
│   └── resources/
│       └── application.yml         # Application configuration
```

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL/PostgreSQL database
- SMTP server for email functionality

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd demo-project
   ```

2. **Configure Database**
   ```yaml
   # application.yml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/demo_db
       username: your_username
       password: your_password
   ```

3. **Configure Email Settings**
   ```yaml
   spring:
     mail:
       host: smtp.gmail.com
       port: 587
       username: your-email@gmail.com
       password: your-app-password
   ```

4. **Build and Run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. **Access the Application**
   - API Base URL: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`

## 📚 API Documentation

### Authentication Endpoints
```
POST /auth/register              # User registration
POST /auth/authenticate          # User login
GET  /auth/activation            # Account activation
POST /auth/forgot-password       # Password reset request
POST /auth/reset-password        # Password reset
```

### User Management
```
GET    /admin/get-all-users                        # Get all users (simple)
GET    /admin/get-all-users-detailed               # Get all users (detailed)
POST   /admin/get-users-by-department              # Get users by department
GET    /admin/get-users-of-department-by-manager   # Manager's department users
GET    /admin/get-users-of-department-and-childs-by-manager # Manager + child dept users
POST   /admin/delete-user                          # Soft delete user
```

### Company Management
```
GET    /admin/company/get-all          # Get all companies
POST   /admin/company/get-id           # Get company by ID
POST   /admin/company/get-name         # Get company by name
POST   /admin/company/get-type-id      # Get companies by type
POST   /admin/company/add              # Create company
POST   /admin/company/update           # Update company
POST   /admin/company/delete           # Soft delete company
```

### Department Management
```
GET    /admin/department/get-all       # Get all departments
POST   /admin/department/get-id        # Get department by ID
POST   /admin/department/get-name      # Get department by name
POST   /admin/department/add           # Create department
POST   /admin/department/update        # Update department
POST   /admin/department/delete        # Soft delete department
```

### Department Hierarchy
```
GET    /admin/department-hierarchy/get-all         # Get all hierarchies
POST   /admin/department-hierarchy/add             # Create hierarchy
POST   /admin/department-hierarchy/get-children    # Get child departments
POST   /admin/department-hierarchy/get-parents     # Get parent departments
POST   /admin/department-hierarchy/remove          # Remove hierarchy
```

### Location Management
```
# Cities
GET    /admin/town/get-all-cities      # Get all cities
GET    /admin/town/get-city-by-name    # Get city by name
POST   /admin/town/add-city            # Create city
POST   /admin/town/update-city         # Update city
POST   /admin/town/delete-city         # Soft delete city

# Regions
GET    /admin/town/get-regions-by-city-id    # Get regions by city
POST   /admin/town/add-region               # Create region
POST   /admin/town/update-region            # Update region
POST   /admin/town/delete-region            # Soft delete region

# Towns
GET    /admin/town/get-towns-by-region-id   # Get towns by region
POST   /admin/town/add-town                 # Create town
POST   /admin/town/update-town              # Update town
POST   /admin/town/delete-town              # Soft delete town
```

## 🔑 Authentication

### JWT Token Structure
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Authorization Header
```
Authorization: Bearer <jwt-token>
```

### Role-based Access
- **Admin**: Full system access
- **Manager**: Department and subordinate management
- **Employee**: Limited read access

## 🗄 Database Schema

### Core Entities

#### User
```sql
- ID (Primary Key)
- Role_ID (Foreign Key → Role)
- Department_ID (Foreign Key → Department)
- Name, Surname, Email, Password
- Enabled, Active
- Created_At, Deleted_At
```

#### Department
```sql
- ID (Primary Key)
- Company_ID (Foreign Key → Company)
- Type_ID (Foreign Key → DepartmentType)
- Town_ID (Foreign Key → Town)
- Name, Address
- Active, Created_At, Deleted_At
```

#### Company
```sql
- ID (Primary Key)
- Company_Type_ID (Foreign Key → CompanyType)
- Town_ID (Foreign Key → Town)
- Name, Short_Name, Address
- Active, Created_At, Deleted_At
```

#### Location Hierarchy
```sql
City → Region → Town
- Cascading soft delete
- Active status tracking
```

## ⚙ Configuration

### Security Configuration
- CORS enabled for all origins
- JWT token validation
- Role-based endpoint protection
- Session management: STATELESS

### Email Configuration
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${EMAIL_USERNAME:your-email}
    password: ${EMAIL_PASSWORD:your-password}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

### Database Configuration
```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/demo_db}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
```

## 🚀 Deployment

### Environment Variables
```bash
# Database
DB_URL=jdbc:mysql://localhost:3306/demo_db
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password

# Email
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-app-password

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000
```

### Docker Deployment
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/demo-project-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🔧 Development

### Code Style
- Follow Spring Boot best practices
- Use Lombok for boilerplate code reduction
- Implement proper exception handling
- Maintain comprehensive API documentation

### Testing
```bash
# Run tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## 📝 API Examples

### Register User
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "surName": "Doe", 
    "email": "john.doe@example.com",
    "roleName": "Employee",
    "departmentName": "IT"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123"
  }'
```

### Create Company
```bash
curl -X POST http://localhost:8080/admin/company/add \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tech Corp",
    "shortName": "TC",
    "address": "123 Tech Street",
    "typeId": 1,
    "townId": 1
  }'
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support, email your-email@example.com or create an issue in the repository.

---

**Made with ❤️ using Spring Boot**