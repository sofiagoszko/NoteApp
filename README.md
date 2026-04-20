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
 
## Inicio rápido
 
### Opción A — Script bash (requiere Java, Node y MySQL instalados)
 
```bash
git clone <url-del-repositorio>
cd <nombre-del-repo>
bash start.sh
```
 
El script:
1. Verifica que las dependencias estén instaladas
2. Pide usuario y password de MySQL
3. Crea la base de datos `noteapp` si no existe
4. Genera `application.properties` automáticamente
5. Compila y arranca el backend en el puerto `8080`
6. Instala dependencias npm y arranca Vite en el puerto `5173`
7. Mata ambos procesos limpiamente al presionar `Ctrl+C`
### Opción B — Docker Compose (requiere solo Docker)
 
```bash
git clone <url-del-repositorio>
cd <nombre-del-repo>
 
# Copiar los Dockerfiles a sus carpetas correspondientes
cp Dockerfile.backend backend/Dockerfile
cp Dockerfile.frontend frontend/Dockerfile
cp nginx.conf frontend/nginx.conf
 
docker compose up --build
```
 
Con Docker Compose:
- MySQL 8 se levanta automáticamente con usuario `root` / password `root`
- El backend se conecta a la DB dentro de la red interna de Docker
- El frontend se sirve con nginx en el puerto `5173`
- Los datos de la DB persisten en un volumen Docker entre reinicios
Para detener:
```bash
docker compose down
# Para también borrar los datos de la DB:
docker compose down -v
```
 
---
 
## Estructura del proyecto
 
```
NoteApp/
├── start.sh                  ← script de inicio
├── docker-compose.yml         ← Docker Compose (Opción B)
├── README.md
├── backend/
│    ├── mvnw
│    ├── Dockerfile
│    ├── pom.xml
│    └── src/main/
│        ├── java/com/hirelens/noteapp/
│        │   ├── config/
│        │   ├── controllers/
│        │   ├── dto/
│        │   ├── enums/
│        │   ├── mappers/
│        │   ├── models/
│        │   ├── repositories/
│        │   ├── responses/
│        │   ├── services/
│        │   └── NoteappApplication.java
│        └── resources/
│            └── application.properties  ← generado por start.sh
└── frontend/
    ├── Dockerfile
    ├── nginx.conf
    └── src/
        ├── components/
        ├── context/
        ├── pages/
        ├── types/
        ├── App.tsx
        ├── index.css
        └──main.tsx
```
 
---
 
## Configuración manual
 
Si no se usa el script, se recomienda seguir estos pasos:
 
### 1. Base de datos
 
```sql
CREATE DATABASE noteapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
 
### 2. Backend — application.properties
 
Creá el archivo en `backend/noteapp/src/main/resources/application.properties`:
 
```properties
spring.application.name=noteapp

server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/noteapp
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql: true

logging.level.org.springframework=DEBUG
logging.level.com.ap.sgri=DEBUG
spring.servlet.multipart.enabled=true
spring.docker.compose.enabled=false
```
 
### 3. Arrancar el backend
 
```bash
cd backend/
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
 
## Endpoints principales
 
### Usuarios
 
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/users/register` | Registrar usuario |
| `POST` | `/api/users/login` | Iniciar sesión |
| `GET` | `/api/users/{id}` | Obtener usuario |
| `PUT` | `/api/users/{id}` | Editar nickname y email |
| `PATCH` | `/api/users/{id}/password` | Cambiar contraseña |
| `DELETE` | `/api/users/{id}` | Eliminar usuario |
 
### Notas
 
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/notes/users/{userId}/active?active=true` | Notas activas |
| `GET` | `/api/notes/users/{userId}/active?active=false` | Notas archivadas |
| `POST` | `/api/notes/users/{userId}/notes` | Crear nota |
| `PUT` | `/api/notes/users/{userId}/notes/{noteId}` | Editar nota |
| `PATCH` | `/api/notes/users/{userId}/notes/{noteId}/toggle-active` | Archivar/desarchivar |
| `DELETE` | `/api/notes/users/{userId}/notes/{noteId}` | Eliminar nota |
 

> Todos los endpoints (excepto login y register) requieren el header `X-User-Id: {id}`.
 
---
 
## Usuario administrador
 
Al iniciar la aplicación por primera vez se crea automáticamente un usuario administrador:
 
| Campo | Valor |
|-------|-------|
| Email | `admin@noteapp.com` |
| Password | `admin123` |
| Rol | `ADMIN` |
 
---
 
## Futuras mejoras
 
- **Panel de administración** — la separación de roles `USER` / `ADMIN` ya está implementada en el backend. Como mejora futura se puede agregar un panel web para que el administrador gestione todos los usuarios y notas del sistema desde una interfaz dedicada.
- Autenticación con JWT para reemplazar el header `X-User-Id`
- Paginación en el listado de notas
- Búsqueda de notas por texto libre
- Categorías personalizadas por usuario
- Filtros por categorías
 