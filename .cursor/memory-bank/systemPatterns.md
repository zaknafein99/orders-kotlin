# System Patterns

## Architecture Overview
The system follows a layered architecture pattern with clear separation of concerns:

1. Presentation Layer (API Controllers)
2. Service Layer (Business Logic)
3. Repository Layer (Data Access)
4. Domain Layer (Entities)

## Design Patterns

### 1. Repository Pattern
- Used for data access abstraction
- Implements JPA repositories
- Provides type-safe database operations

### 2. DTO Pattern
- Separates API models from domain models
- Used for data transfer between layers
- Implements MapStruct for mapping

### 3. Service Layer Pattern
- Encapsulates business logic
- Provides transaction management
- Implements validation rules

### 4. Controller Pattern
- REST API endpoints
- Request/Response handling
- Input validation
- Error handling

### 5. Security Patterns
- JWT-based authentication
- Role-based access control
- Token refresh mechanism
- Password encryption

## Component Relationships

### Core Components
1. Order Management
   - OrderController
   - OrderService
   - OrderRepository
   - Order entity

2. Customer Management
   - CustomerController
   - CustomerService
   - CustomerRepository
   - Customer entity

3. Item Management
   - ItemController
   - ItemService
   - ItemRepository
   - Item entity

4. Truck Management
   - TruckController
   - TruckService
   - TruckRepository
   - Truck entity

5. User Management
   - UserController
   - UserService
   - UserRepository
   - User entity

## Data Flow
1. API Request → Controller
2. Controller → Service
3. Service → Repository
4. Repository → Database
5. Database → Repository
6. Repository → Service
7. Service → DTO
8. DTO → Controller
9. Controller → API Response

## Security Flow
1. Authentication Request
2. JWT Token Generation
3. Token Validation
4. Role Verification
5. Access Control

## Error Handling
1. Global Exception Handler
2. Custom Exceptions
3. Validation Errors
4. Business Logic Errors
5. Database Errors 