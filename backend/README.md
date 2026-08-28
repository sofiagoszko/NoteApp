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
2. Crear `src/main/resources/application.properties` (no está versionado):
   ```properties
   spring.application.name=noteapp
   server.port=8080

   spring.datasource.url=jdbc:mysql://localhost:3306/noteapp
   spring.datasource.username=TU_USUARIO
   spring.datasource.password=TU_PASSWORD
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   spring.jpa.hibernate.ddl-auto=update
   spring.docker.compose.enabled=false
   ```
3. Arrancar:
   ```bash
   ./mvnw spring-boot:run
   ```

El backend queda disponible en `http://localhost:8080`.


### Opción B — Docker Compose

Desde esta carpeta:

```bash
cd backend
docker compose up --build
```

Esto levanta:
- **`db`** — MySQL 8, expuesto en el host en el puerto `3307` (para no chocar con un MySQL local en `3306`), con datos persistidos en el volumen `noteapp-db-data`.
- **`backend`** — la API Spring Boot, expuesta en `http://localhost:8080`, conectada a `db` a través de la red `noteapp-network`.

Para detener:
```bash
docker compose down          # conserva los datos de la DB
docker compose down -v       # borra también los datos de la DB
```

## Usuario administrador por defecto

Al arrancar por primera vez, `DataInitializer` crea automáticamente:

| Campo | Valor |
|---|---|
| Email | `admin@noteapp.com` |
| Password | `admin123` |
| Rol | `ADMIN` |

