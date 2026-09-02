# NoteApp
 
Aplicación web para crear, editar, archivar y filtrar notas personales.
 
## Requisitos

Se necesitan tener las siguientes herramientas con sus versiones mínimas:
 
| Herramienta | Versión mínima | Verificar con |
|-------------|---------------|---------------|
| Java (JDK)  | 21            | `java -version` |
| Maven       | 3.9           | `mvn -version` |
| Node.js     | 20.x          | `node --version` |
| npm         | 10.x          | `npm -version` |
| MySQL       | 8.0           | `mysql --version` |
 
### Versiones usadas en desarrollo
 
| Herramienta | Versión exacta |
|-------------|---------------|
| Java (JDK)  | 21.0.4 |
| Maven       | 3.9.9 |
| Spring Boot | 4.0.5   |
| Node.js     | 22.13.0 |
| npm         | 11.0.0 |
| MySQL       | 8.0.37 |

### Dependencias del frontend
 
| Paquete | Versión |
|---------|---------|
| React | 19.2.4 |
| TypeScript | 6.0.2 |
| Vite | 8.0.4 |
| Tailwind CSS | 4.2.2 |
| React Router DOM | 7.14.1 |
| React Hot Toast | 2.6.0 |
| Lucide React | 1.8.0 |
 
### Dependencias del backend
 
| Paquete | Versión |
|---------|---------|
| Spring Boot Starter Web MVC | 4.0.5 |
| Spring Boot Starter Data JPA | 4.0.5 |
| Spring Boot Starter Security | 4.0.5 |
| Spring Boot Starter Validation | 4.0.5 |
| MySQL Connector/J | 9.x |
| Lombok | 1.18.x |

---
 
## Estructura del proyecto
 
```
NoteApp/
├── README.md                   
├── backend/
│    ├── README.md
│    ├── Dockerfile
│    ├── docker-compose.yaml    
│    ├── mvnw
│    ├── pom.xml
│    └── src/
│        ├── main/
│        │   ├── java/com/hirelens/noteapp/
│        │   │   ├── config/          ← SecurityConfig, DataInitializer, *Properties
│        │   │   ├── controllers/
│        │   │   ├── dto/
│        │   │   ├── enums/
│        │   │   ├── exceptions/      ← GlobalExceptionHandler
│        │   │   ├── mappers/
│        │   │   ├── models/
│        │   │   ├── repositories/
│        │   │   ├── responses/
│        │   │   ├── security/        ← JwtService, RateLimitFilter, AuthorizationService, handlers REST
│        │   │   ├── services/
│        │   │   └── NoteappApplication.java
│        │   └── resources/
│        │       └── application.properties 
│        └── test/
│            ├── java/com/hirelens/noteapp/   ← services/, controllers/, security/, integration/
│            └── resources/application.properties  ← perfil de test (H2 en memoria)
└── frontend/
    ├── README.md
    ├── Dockerfile
    ├── docker-compose.yml     
    ├── nginx.conf
    ├── .env.example
    └── src/
        ├── components/        (+ *.test.tsx)
        ├── context/           (+ *.test.tsx)
        ├── pages/             (+ *.test.tsx)
        ├── types/
        ├── test/setup.ts      ← setup de Vitest
        ├── App.tsx
        ├── index.css
        └──main.tsx
```
 
---
 
## Inicio rápido
 
### Docker Compose

Cada servicio tiene su propio `docker-compose` ([`backend/docker-compose.yaml`](backend/docker-compose.yaml), [`frontend/docker-compose.yml`](frontend/docker-compose.yml)), pensados para levantarse tanto por separado como en conjunto.

> **Requisito**: el backend necesita `APP_JWT_SECRET` (mínimo 32 caracteres). Creá `backend/.env`
> a partir del ejemplo y completá el secreto:
> ```bash
> cp backend/.env.example backend/.env
> # editá backend/.env:  APP_JWT_SECRET=$(openssl rand -base64 48)
> ```
> `docker compose` lee ese `backend/.env` para las variables `${...}` del `docker-compose.yaml`.

**Por separado**:

```bash
cd backend && docker compose up --build -d   # MySQL + backend en :8080 (MySQL expuesto en :3307)
cd frontend && docker compose up --build -d  # frontend (nginx) en :5173
```

**Stack completo**, desde la raíz del repo (el backend debe listarse primero, para que las rutas de build de ambos `Dockerfile` se resuelvan bien):

```bash
git clone <url-del-repositorio>
cd <nombre-del-repo>
cp backend/.env.example backend/.env      # y completar APP_JWT_SECRET

docker compose -f backend/docker-compose.yaml -f frontend/docker-compose.yml up --build -d
```

Con Docker Compose:
- MySQL 8 se levanta automáticamente con usuario `root` / password `root`
- El backend se conecta a la DB dentro de la red interna de Docker (`noteapp-network`, compartida por ambos archivos)
- El frontend se sirve con nginx en el puerto `5173`, proxyando `/api/` hacia el backend
- Los datos de la DB persisten en un volumen Docker entre reinicios

Para detener:
```bash
docker compose -f backend/docker-compose.yaml -f frontend/docker-compose.yml down
# Para también borrar los datos de la DB:
docker compose -f backend/docker-compose.yaml -f frontend/docker-compose.yml down -v
```
 
---
 
## Configuración manual
 
 
### 1. Base de datos
 
```sql
CREATE DATABASE noteapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
 
### 2. Backend — application.properties
 
Copiá la plantilla y completá tus datos de MySQL y el secreto JWT:
 
```bash
cd backend
cp src/main/resources/application.properties.example src/main/resources/application.properties
```
 
Editá al menos:
 
```properties
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_PASSWORD

# Obligatorio, mínimo 32 caracteres.  Generar:  openssl rand -base64 48
app.jwt.secret=UN_SECRETO_LARGO_Y_ALEATORIO
```
 
### 3. Arrancar el backend
 
```bash
./mvnw spring-boot:run
```
 
### 4. Arrancar el frontend
 
```bash
cd frontend
npm install
npm run dev
```
 La app queda disponible en `http://localhost:5173`.
---
 
## Pruebas

### Backend

```bash
cd backend
./mvnw test
```

Los tests corren contra una base **H2 en memoria** (`src/test/resources/application.properties`),
así que **no necesitan MySQL ni `application.properties` local ni `APP_JWT_SECRET`**. Cubren:

| Suite | Qué prueba |
|-------|------------|
| `services/` | `UserService` y `NoteService` con Mockito (unitarios) |
| `controllers/` | `NoteController` / `UserController` vía `MockMvc` + JWT simulado |
| `security/` | emisión/validación de JWT, autorización self-or-admin, rate limiting por IP |
| `integration/` | flujo end-to-end (`@SpringBootTest`): registro → login → CRUD de notas |

Un test puntual: `./mvnw test -Dtest=NoteControllerTest` · un método: `./mvnw test -Dtest=NoteControllerTest#getNoteByIdReturnsNoteForUser`

### Frontend

```bash
cd frontend
npm run test:ci     # una corrida (CI)
npm run test        # modo watch
```

Vitest + Testing Library + jsdom. Cubren `AuthContext`, `ProtectedRoute`, `Login` y `Notes`
(mockeando `fetch`), incluyendo el envío del header `Authorization: Bearer` y el logout automático al recibir 401.

---
 
## Endpoints principales
 
### Usuarios
 
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/users/register` | Registrar usuario (devuelve token + user) |
| `POST` | `/api/users/login` | Iniciar sesión (devuelve token + user) |
| `GET` | `/api/users/{id}` | Obtener usuario |
| `PUT` | `/api/users/{id}` | Editar nickname y email |
| `PATCH` | `/api/users/{id}/password` | Cambiar contraseña |
| `DELETE` | `/api/users/{id}` | Eliminar usuario |
 
### Notas
 
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/notes/users` | Listar todas las notas del sistema (solo `ADMIN`) |
| `GET` | `/api/notes/users/{userId}/active?active=true` | Notas activas |
| `GET` | `/api/notes/users/{userId}/active?active=false` | Notas archivadas |
| `GET` | `/api/notes/users/{userId}/notes/{noteId}` | Obtener una nota |
| `POST` | `/api/notes/users/{userId}/notes` | Crear nota |
| `PUT` | `/api/notes/users/{userId}/notes/{noteId}` | Editar nota |
| `PATCH` | `/api/notes/users/{userId}/notes/{noteId}/toggle-active` | Archivar/desarchivar |
| `DELETE` | `/api/notes/users/{userId}/notes/{noteId}` | Eliminar nota |
 

> Todos los endpoints (excepto `login` y `register`) requieren el header `Authorization: Bearer {token}`.
> El token se obtiene del `login`/`register`, dura 1 hora y al vencer hay que volver a iniciar sesión.
> Un usuario solo puede operar sobre su propia cuenta y sus propias notas (los `ADMIN` pueden sobre cualquiera).
 
---
 
## Usuario administrador
 
Si se define la variable de entorno `APP_ADMIN_PASSWORD`, al iniciar por primera vez se crea
automáticamente un usuario administrador. Si no se define, no se crea ninguno.
 
| Campo | Valor |
|-------|-------|
| Email | `admin@noteapp.com` |
| Password | el valor de `APP_ADMIN_PASSWORD` |
| Rol | `ADMIN` |
 
---
 
## Futuras mejoras
 
- **Panel de administración** — la separación de roles `USER` / `ADMIN` ya está implementada en el backend. Como mejora futura se puede agregar un panel web para que el administrador gestione todos los usuarios y notas del sistema desde una interfaz dedicada.
- **Refresh tokens** — hoy al vencer el token (1 h) hay que volver a iniciar sesión.
- **Bloqueo de cuenta** — sumar bloqueo tras N intentos fallidos, además del rate limiting por IP.
- Paginación en el listado de notas
- Búsqueda de notas por texto libre
- Categorías personalizadas por usuario
- Filtros por categorías

## Jenkins

El proyecto incluye un `Jenkinsfile` en la raíz del repositorio para ejecutar el pipeline de integración continua.

### Requisitos

Antes de iniciar Jenkins es necesario tener instalado:

- Java 21 o superior
- Docker Desktop
- Git
- Node.js
- npm

Docker Desktop debe estar iniciado antes de ejecutar el pipeline.

### Iniciar Jenkins localmente

Descargar `Jenkins.war` desde el sitio oficial de Jenkins.

Desde la carpeta donde se encuentre el archivo ejecutar:

```bash
java -jar Jenkins.war --httpPort=8081

(acá usamos este puerto porque el 8080 lo ocupa la app)

Luego acceder desde el navegador a:

http://localhost:8081

La primera vez que se inicia Jenkins solicitará una contraseña inicial de administrador.

En Windows puede obtenerse desde PowerShell con:

Get-Content "$env:USERPROFILE\.jenkins\secrets\initialAdminPassword"

Luego se debe completar la configuración inicial de Jenkins, instalar los plugins recomendados y crear un usuario administrador.

Configuración del Job

Desde el panel principal de Jenkins seleccionar:

New Item

Ingresar un nombre para el Job, por ejemplo:

NoteApp

Seleccionar el tipo:

Pipeline

y crear el Job.

En la configuración del Pipeline seleccionar:

Definition: Pipeline script from SCM
SCM: Git

Configurar como repositorio:

https://github.com/sofiagoszko/NoteApp.git

En la sección Branches to build indicar la rama sobre la cual se ejecutará Jenkins.

Durante el desarrollo y prueba del pipeline se utilizó:

*/Feature/Jenkins

Una vez que el Jenkinsfile haya sido validado y mergeado, el Job puede configurarse para trabajar sobre:

*/dev

En el campo Script Path indicar:

Jenkinsfile

Guardar.

Para ejecutar el job le damos a "Build now"