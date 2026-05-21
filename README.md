# SGarden - Inventory Management API

A REST API for inventory and product management. Available in two implementations:

- **Java** (Spring Boot) - `java/` directory
- **Python** (FastAPI) - `python/` directory

Both implementations expose the **same API contract** and connect to the same MongoDB database.

## Prerequisites

- MongoDB (running locally on port 27017, or set `DATABASE_URL` env var)
- **Java track:** JDK 17+, Maven 3.8+
- **Python track:** Python 3.10+, pip

## Quick Start

### Java (Spring Boot)

```bash
cd java
cp ../env.sample ../.env   # Edit with your settings
mvn spring-boot:run
```

The server starts on `http://localhost:4000`.

### Python (FastAPI)

```bash
cd python
cp ../env.sample ../.env   # Edit with your settings
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 4000 --reload
```

The server starts on `http://localhost:4000`.

## API Endpoints

### Authentication (Pre-built)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and receive JWT token |

### Products (Pre-built CRUD)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | List all products |
| GET | `/api/products/:id` | Get product by ID |
| POST | `/api/products` | Create a product (auth required) |
| PUT | `/api/products/:id` | Update a product (auth required) |
| DELETE | `/api/products/:id` | Delete a product (auth required) |

### Test Users

The application seeds two test users on startup:

- **Admin:** username: `admin`, password: `admin123` (role: admin)
- **User:** username: `user`, password: `user1234` (role: user)

## Environment Variables

| Variable | Default | Description |

|----------|---------|-------------|
| `DATABASE_URL` | `mongodb://localhost:27017/sgarden` | MongoDB connection string |
| `PORT` | `4000` | Server port |
| `SERVER_SECRET` | `sgarden-secret-key` | JWT signing secret |
