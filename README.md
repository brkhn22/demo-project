# Demo Project - Employee Management System

A comprehensive Spring Boot REST API application for managing employees, departments, companies, and organizational hierarchies with advanced authentication, authorization, and role-based management features.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Authentication](#authentication)
- [Role-Based Access Control](#role-based-access-control)
- [Email Templates](#email-templates)
- [Database Schema](#database-schema)
- [Configuration](#configuration)
- [Deployment](#deployment)
- [Contributing](#contributing)

## 🎯 Overview

This project is a complete enterprise-level employee management system that provides hierarchical department management, role-based user operations, and comprehensive organizational structure management with beautiful email notifications and advanced security features.

## ✨ Features

### 🔐 Authentication & Authorization
- JWT-based authentication with role-based access control
- Beautiful HTML email verification for new users
- Password reset functionality with styled emails
- Account activation via email with modern templates
- Manager-based user registration with department validation
- Three-tier authorization system (Admin, Manager, Employee)

### 👥 Advanced User Management
- **Admin Operations**: Complete user management (CRUD, department/role updates)
- **Manager Operations**: Department-scoped user management with hierarchy validation
- **Employee Operations**: Read-only access to personal data
- User profile updates with role preservation
- Soft delete functionality with role-based restrictions
- Department and role update capabilities with business logic validation

### 🏢 Hierarchical Organization Management
- **Companies**: Multi-company support with types and geographical locations
- **Departments**: Parent-child department relationships with company boundaries
- **Department Hierarchy**: Circular dependency prevention and same-company validation
- **Manager Authority**: Hierarchical permissions for department and child department access
- **Role Management**: Dynamic role assignment with authorization controls

### 🌍 Enhanced Location Management
- Three-tier location system (City → Region → Town)
- Cascading soft delete with business rule validation
- Active location validation and geographical consistency
- UTF-8 support for international characters (Turkish, etc.)

### 📧 Beautiful Email System
- **Account Activation**: Purple gradient design with Demo Project branding
- **Manager Invitation**: Green gradient with department and manager information
- **Password Reset**: Red gradient with security warnings
- Responsive HTML templates with hover effects
- Personalized content with user and manager names

## 🛠 Tech Stack

- **Framework**: Spring Boot 3.x
- **Security**: Spring Security with JWT and role-based authorization
- **Database**: JPA/Hibernate with MySQL (UTF-8 support)
- **Documentation**: OpenAPI 3 (Swagger) with ngrok support
- **Email**: JavaMailSender with HTML templates
- **Build Tool**: Maven
- **Java Version**: 17+
- **Internationalization**: Full UTF-8 support for Turkish characters

## 🏗 Architecture

```
src/
├── main/
│   ├── java/com/example/demo_project/
│   │   ├── auth/                    # Authentication, JWT & Email Templates
│   │   ├── config/                  # Security, CORS & App Configuration
│   │   ├── email/                   # Email services
│   │   └── user/                    # User & Organization modules
│   │       ├── company/             # Company & CompanyType management
│   │       ├── department/          # Department management
│   │       │   └── department_hierarchy/ # Hierarchical relationships
│   │       └── town/               # City/Region/Town management
│   └── resources/
│       └── application.yml         # UTF-8 & Database configuration
```

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL database with UTF-8 support
- SMTP server for email functionality

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd demo-project
   ```

2. **Configure Database with UTF-8 Support**
   ```yaml
   # application.yml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/demo_db?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
       username: your_username
       password: your_password
       hikari:
         connection-init-sql: "SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci"
     jpa:
       properties:
         hibernate:
           dialect: org.hibernate.dialect.MySQL8Dialect
           connection:
             characterEncoding: UTF-8
             useUnicode: true
   ```

3. **Configure Email Settings**
   ```yaml
   spring:
     mail:
       host: smtp.gmail.com
       port: 587
       username: your-email@gmail.com
       password: your-app-password
       properties:
         mail:
           smtp:
             auth: true
             starttls:
               enable: true
   ```

4. **Build and Run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. **Access the Application**
   - API Base URL: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - Production: `https://demo-project-6-env.eba-bub3hufq.eu-north-1.elasticbeanstalk.com`

## 📚 API Documentation

### 🔐 Authentication Endpoints
```
POST /auth/register                    # Admin-only user registration
POST /auth/manager/register            # Manager-based user registration
POST /auth/authenticate                # User login
GET  /auth/activation                  # Account activation via email
POST /auth/activation/confirm          # Account confirmation with password
POST /auth/activation/resend           # Resend activation email
POST /auth/forgot-password             # Password reset request
GET  /auth/activate-forgot-password    # Password reset activation
POST /auth/reset-password              # Password reset completion
```

### 👥 User Management (Role-Based)

#### Admin Operations
```
GET    /admin/get-all-users                    # Get all users (simple)
GET    /admin/get-all-users-detailed           # Get all users (detailed)
POST   /admin/get-users-by-department          # Get users by department
POST   /admin/delete-user                      # Delete any user
POST   /admin/update-user-department           # Update user department (any → any)
POST   /admin/update-user-role                 # Update user role (any → any including Admin)
```

#### Manager Operations
```
GET    /manager/get-users-of-department-by-manager              # Get own department users
GET    /manager/get-users-of-department-and-childs-by-manager   # Get own + child dept users
POST   /manager/delete-user                                     # Delete users in scope
POST   /manager/update-user-department                          # Update to own/child depts
POST   /manager/update-user-role                                # Update to Employee role only
```

#### User Operations
```
GET    /user/get-self                          # Get own profile
POST   /user/update-user                       # Update own profile (no role/dept)
POST   /employee/delete-user                   # Always throws exception
```

### 🏢 Company Management
```
GET    /company/get-all                        # Get all companies
POST   /company/get-id                         # Get company by ID
POST   /company/get-name                       # Get company by name
POST   /company/get-type-id                    # Get companies by type
POST   /company/add                            # Create company
POST   /company/update                         # Update company
POST   /company/delete                         # Soft delete company
```

### 🏢 Company Type Management
```
GET    /admin/company-type/get-all             # Get all company types
POST   /admin/company-type/get-id              # Get company type by ID
POST   /admin/company-type/get-name            # Get company type by name
POST   /admin/company-type/add                 # Create company type
POST   /admin/company-type/update              # Update company type
POST   /admin/company-type/delete              # Soft delete company type
```

### 🏢 Department Management
```
GET    /department/get-all                     # Get all departments
POST   /department/get-id                      # Get department by ID
POST   /department/get-name                    # Get department by name
POST   /department/get-company-id              # Get departments by company
POST   /department/add                         # Create department
POST   /department/update                      # Update department
POST   /department/delete                      # Soft delete department
```

### 🏗 Department Hierarchy (Same-Company Only)
```
GET    /department-hierarchy/get-all           # Get all hierarchies
POST   /department-hierarchy/add               # Create hierarchy (same company validation)
POST   /department-hierarchy/get-children      # Get child departments
POST   /department-hierarchy/get-parents       # Get parent departments
POST   /department-hierarchy/remove            # Remove hierarchy
```

### 🌍 Location Management
```
# Cities
GET    /town/get-all-cities                    # Get all cities
GET    /town/get-city-by-name                  # Get city by name
POST   /town/add-city                          # Create city
POST   /town/update-city                       # Update city
POST   /town/delete-city                       # Soft delete city

# Regions
GET    /town/get-regions-by-city-id            # Get regions by city
POST   /town/get-regions-by-city-name          # Get regions by city name
POST   /town/add-region                        # Create region
POST   /town/update-region                     # Update region
POST   /town/delete-region                     # Soft delete region

# Towns
GET    /town/get-towns-by-region-id-city-id    # Get towns by region and city
POST   /town/add-town                          # Create town
POST   /town/update-town                       # Update town
POST   /town/delete-town                       # Soft delete town
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

## 🔐 Role-Based Access Control

### 👑 Admin Powers
- ✅ **Full System Access**: Manage all users, companies, departments
- ✅ **User Management**: Create, update, delete any user
- ✅ **Role Assignment**: Assign any role including Admin
- ✅ **Department Management**: Move users between any departments
- ✅ **Company Operations**: Full company and department hierarchy control

### 👔 Manager Powers
- ✅ **Department Scope**: Manage own department + child departments
- ✅ **User Registration**: Register users to own/child departments
- ✅ **Limited Role Assignment**: Can only assign Employee role
- ✅ **User Updates**: Update users within scope (not other managers from other depts)
- ❌ **Admin Operations**: Cannot create Admins or manage other managers outside scope

### 👤 Employee Powers
- ✅ **Self Management**: Update own profile (name, email, enabled status)
- ✅ **Read Access**: View own information
- ❌ **Management Operations**: Cannot manage other users or organizational structure

## 📧 Email Templates

### 🎨 Account Activation Email (Purple Gradient)
```html
🚀 Demo Project - Welcome to Our Platform!
- Personalized greeting with user's first name
- Beautiful purple gradient design
- Security note about 24-hour expiration
- Professional footer with copyright
```

### 🎨 Manager Invitation Email (Green Gradient)
```html
🏢 Demo Project - You've Been Invited!
- Manager name and department information
- User details box with role and department
- Green gradient corporate design
- Manager contact information
```

### 🎨 Password Reset Email (Red Gradient)
```html
🔐 Demo Project - Password Reset Request
- Security warnings and notices
- Red gradient for attention
- Clear instructions for password reset
- Automated message disclaimer
```

## 🗄 Database Schema

### Core Entities with Relationships

#### User
```sql
- ID (Primary Key)
- Role_ID (Foreign Key → Role)
- Department_ID (Foreign Key → Department)
- First_Name, Sur_Name, Email, Password (UTF-8 support)
- Enabled, Active
- Created_At, Deleted_At
```

#### Department (with Company Boundaries)
```sql
- ID (Primary Key)
- Company_ID (Foreign Key → Company)
- Type_ID (Foreign Key → DepartmentType)
- Town_ID (Foreign Key → Town)
- Name, Address (UTF-8 support)
- Active, Created_At, Deleted_At
```

#### Department_Hierarchy (Same-Company Validation)
```sql
- ID (Primary Key)
- Parent_Department_ID (Foreign Key → Department)
- Child_Department_ID (Foreign Key → Department)
- Constraint: Both departments must belong to same company
- Circular dependency prevention
```

#### Company
```sql
- ID (Primary Key)
- Company_Type_ID (Foreign Key → CompanyType)
- Town_ID (Foreign Key → Town)
- Name, Short_Name, Address (UTF-8 support)
- Active, Created_At, Deleted_At
```

#### Location Hierarchy
```sql
City → Region → Town
- Cascading soft delete
- Active status tracking
- UTF-8 support for international names
```

## ⚙ Configuration

### Security Configuration
```java
// Role-based endpoint protection
.requestMatchers("/admin/**").hasAuthority("Admin")
.requestMatchers("/manager/**").hasAuthority("Manager")  
.requestMatchers("/user/**").hasAnyAuthority("User", "Admin", "Manager")
.requestMatchers("/auth/register").hasAuthority("Admin")
.requestMatchers("/auth/manager/register").hasAuthority("Manager")
```

### UTF-8 Database Configuration
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/demo_db?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    hikari:
      connection-init-sql: "SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci"
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        connection:
          characterEncoding: UTF-8
          useUnicode: true
  http:
    encoding:
      charset: UTF-8
      enabled: true
      force: true
```

### Email Configuration
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: brkhn2248@gmail.com
    password: hpcqjlrtizfbrzyp
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

### CORS Configuration
```java
// Production-ready CORS setup
configuration.setAllowedOriginPatterns(Arrays.asList(
    "http://localhost:*",
    "http://127.0.0.1:*", 
    "https://*.ngrok-free.app",
    "https://*.ngrok.io",
    "https://demo-project-6-env.eba-bub3hufq.eu-north-1.elasticbeanstalk.com"
));
```

## 🚀 Deployment

### Environment Variables
```bash
# Database with UTF-8
DB_URL=jdbc:mysql://localhost:3306/demo_db?useUnicode=true&characterEncoding=UTF-8
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password

# Email Service
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-app-password

# JWT Configuration
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# Application URL
MAIN_PATH=https://your-domain.com
```

### AWS Elastic Beanstalk
```bash
# Current production deployment
https://demo-project-6-env.eba-bub3hufq.eu-north-1.elasticbeanstalk.com
```

## 🔧 Business Logic Examples

### Manager Registration Flow
```java
// Manager can only register to own department or child departments
1. Validate manager role
2. Check target department is in manager's scope
3. Validate role is "Employee" only
4. Create user with manager invitation email
5. Send beautiful HTML email with manager details
```

### Department Update Authorization
```java
// Role-based department update rules
Admin: Can move any user to any department
Manager: Can move users within scope to own/child departments
Employee: Cannot update departments
```

### Hierarchy Validation
```java
// Department hierarchy business rules
1. Both departments must belong to same company
2. Prevent circular dependencies
3. No self-referencing relationships
4. Company boundary enforcement
```

## 📝 API Examples

### Manager Registration
```bash
curl -X POST http://localhost:8080/auth/manager/register \
  -H "Authorization: Bearer <manager-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Ahmet",
    "surName": "Yılmaz", 
    "email": "ahmet.yilmaz@company.com",
    "roleName": "Employee",
    "departmentId": 5
  }'
```

### Update User Department (Manager)
```bash
curl -X POST http://localhost:8080/manager/update-user-department \
  -H "Authorization: Bearer <manager-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 10,
    "departmentId": 6
  }'
```

### Create Department Hierarchy
```bash
curl -X POST http://localhost:8080/department-hierarchy/add \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "parentDepartmentId": 1,
    "childDepartmentId": 2
  }'
```

### Update User Role (Admin)
```bash
curl -X POST http://localhost:8080/admin/update-user-role \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 10,
    "roleId": 2
  }'
```

## 🌟 Key Features Summary

### ✨ What Makes This Special
- **🎨 Beautiful Emails**: Modern HTML templates with gradients and responsive design
- **🔐 Advanced RBAC**: Three-tier authorization with hierarchical permissions
- **🏢 Business Logic**: Company boundaries, department hierarchies, and manager scopes
- **🌍 Internationalization**: Full UTF-8 support for Turkish and other languages
- **📱 Production Ready**: AWS deployment with proper CORS and security
- **📖 Comprehensive API**: 50+ endpoints with detailed Swagger documentation

### 🚀 Recent Additions
- Manager-based user registration with email invitations
- Department and role update capabilities with business validation
- Same-company department hierarchy validation
- Beautiful HTML email templates with Demo Project branding
- UTF-8 database configuration for international characters
- Enhanced role-based access control with scope validation

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Follow the existing code style and business logic patterns
4. Add appropriate tests for new functionality
5. Update API documentation if needed
6. Commit your changes (`git commit -m 'Add amazing feature'`)
7. Push to the branch (`git push origin feature/amazing-feature`)
8. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support, email brkhn2248@gmail.com or create an issue in the repository.

---

**Made with ❤️ using Spring Boot • Features Beautiful Emails 📧 • Enterprise-Ready 🏢 • UTF-8 Support 🌍**