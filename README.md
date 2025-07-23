# Buy Recipes Application

## Overview

The Buy Recipes application extends an e-commerce platform's cart functionality to support purchasing entire recipes (collections of products) in addition to individual products.

**Key Features:**
- Add/remove entire recipes to shopping carts
- Manage cart items with quantity tracking

## Quick Start with Docker

### Prerequisites

- [Docker](https://docs.docker.com/get-docker/) 
- [Docker Compose](https://docs.docker.com/compose/install/)

### Run the Application

1. **Clone the repository:**
   ```bash
   git clone https://github.com/maherasha/buy-recipes.git
   cd buy-recipes
   ```

2. **Start the application with docker:**
   ```bash
   docker-compose up --build
   ```

3. **Access the application:**
   - **API Base URL:** http://localhost:8080
   - **Swagger UI:** http://localhost:8080/swagger-ui.html

### Stop the Application

```bash
docker-compose down
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/buy-recipe/carts` | Get all carts (basic info) |
| GET | `/buy-recipe/carts/{cartId}` | Get cart details with items |
| GET | `/buy-recipe/recipes` | Get all available recipes |
| POST | `/buy-recipe/carts/{cartId}/add_recipe` | Add recipe to cart |
| DELETE | `/buy-recipe/carts/{cartId}/recipes/{recipeId}` | Remove recipe from cart |

## Example Usage

```bash
# Get all carts
curl http://localhost:8080/buy-recipe/carts

# Get all recipes
curl http://localhost:8080/buy-recipe/recipes

# Add recipe to cart
curl -X POST http://localhost:8080/buy-recipe/carts/1/add_recipe \
  -H "Content-Type: application/json" \
  -d '{"recipeId": 1}'
```

## Development

### Architecture
- **Java 17** with **Spring Boot 3.2.2**
- **MySQL 8.0** for data persistence
- **OpenAPI 3** for API documentation
- **Docker** for containerization

### Project Structure
```
src/
├── main/java/com/buyrecipe/demo/
│   ├── controller/     # REST endpoints
│   ├── service/        # Business logic
│   ├── repository/     # Data access layer
│   ├── model/          # JPA entities
│   └── dto/            # Data transfer objects
└── main/resources/
    ├── openapi/        # API specifications
    └── db/migration/   # Database migrations
```

