# NoteApp — Backend


## Estructura

```
backend/
├── Dockerfile                 ← build multi-stage 
├── docker-compose.yaml        ← DB (MySQL) + backend
├── .dockerignore
├── pom.xml
├── mvnw / mvnw.cmd            ← Maven wrapper 
└── src/
    ├── main/
    │   ├── java/com/hirelens/noteapp/
    │   │   ├── NoteappApplication.java
    │   │   ├── config/            ← SecurityConfig, DataInitializer 
    │   │   ├── controllers/       ← NoteController, UserController
    │   │   ├── dto/                ← NoteDTO, NoteDTONew, NoteDTOEdit, UserDTO, UserDTOEdit, UserDTOPass
    │   │   ├── enums/              ← Role
    │   │   ├── mappers/            ← NoteMapper, UserMapper
    │   │   ├── models/             ← Note, User (entidades JPA)
    │   │   ├── repositories/       ← NoteRepository, UserRepository
    │   │   ├── responses/          ← Response
    │   │   └── services/           ← NoteService, UserService
    │   └── resources/
    │       └── application.properties   ← generado localmente
    └── test/java/com/hirelens/noteapp/
        └── NoteappApplicationTests.java
```

## Dependencias

| Herramienta / librería | Versión |
|---|---|
| Java (JDK) | 21 |
| Maven | 3.9 (via `mvnw`, no requiere instalación) |
| Spring Boot | 4.0.5 |
| Spring Boot Starter Web MVC | 4.0.5 |
| Spring Boot Starter Data JPA | 4.0.5 |
| Spring Boot Starter Security | 4.0.5 |
| Spring Boot Starter Validation | 4.0.5 |
| MySQL Connector/J | 9.x |
| Lombok | 1.18.x |
| MySQL (motor de base de datos) | 8.0 |

Definidas en [`pom.xml`](pom.xml). El paquete base de la aplicación es `com.hirelens.noteapp`.

## Cómo levantarlo

### Opción A — Manual 

1. Crear la base de datos:
   ```sql
   CREATE DATABASE noteapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. Crear `src/main/resources/application.properties`. Podés copiar
   [`src/main/resources/application.properties.example`](src/main/resources/application.properties.example)
   y ajustar los valores. Como mínimo necesitás la conexión a la base y el secreto JWT:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/noteapp
   spring.datasource.username=TU_USUARIO
   spring.datasource.password=TU_PASSWORD

   # Obligatorio: mínimo 32 caracteres. Generá uno con:  openssl rand -base64 48
   app.jwt.secret=UN_SECRETO_LARGO_Y_ALEATORIO_DE_32_O_MAS_CHARS
   ```
   El `application.properties.example` trae un valor por defecto de `app.jwt.secret` **solo
   para desarrollo local**.
3. Arrancar:
   ```bash
   ./mvnw spring-boot:run
   ```

El backend queda disponible en `http://localhost:8080`.

### Autenticación

Todos los endpoints requieren un JWT en el header `Authorization: Bearer <token>`, **excepto**
`POST /api/users/login` y `POST /api/users/register`, que devuelven `{ "token": "...", "user": {...} }`.
El token dura 1 hora (`app.jwt.expiration`); al vencer hay que volver a iniciar sesión.
Hay rate limiting por IP: 5 intentos / 15 min en `/login`, 100 req/min en el resto.


### Opción B — Docker Compose

Desde esta carpeta:

```bash
cd backend
cp .env.example .env          # y completar APP_JWT_SECRET (openssl rand -base64 48)
docker compose up --build -d
```

`docker-compose.yaml` toma las variables de `backend/.env` (o del entorno). Sin un `APP_JWT_SECRET`
válido (≥32 caracteres) el contenedor arranca pero la app corta al validar la config.

Esto levanta:
- **`db`** — MySQL 8, expuesto en el host en el puerto `3307`, con datos persistidos en el volumen `noteapp-db-data`.
- **`backend`** — la API Spring Boot, expuesta en `http://localhost:8080`, conectada a `db` a través de la red `noteapp-network`.

Para detener:
```bash
docker compose down          
docker compose down -v       
```

## Usuario administrador

Solo se crea si está definida la variable `APP_ADMIN_PASSWORD`. Si no, no se siembra ningún admin.

| Campo | Valor |
|---|---|
| Email | `admin@noteapp.com` |
| Password | el valor de `APP_ADMIN_PASSWORD` |
| Rol | `ADMIN` |

