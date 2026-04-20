#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────
#  start.sh — Inicia NoteApp (backend + frontend)
#  Uso: bash start.sh
#  Estructura esperada:
#    /backend/   ← Spring Boot (Maven)
#    /frontend/  ← React + Vite
# ─────────────────────────────────────────────────────────────

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

info()    { echo -e "${GREEN}[NoteApp]${NC} $1"; }
warning() { echo -e "${YELLOW}[NoteApp]${NC} $1"; }
error()   { echo -e "${RED}[NoteApp]${NC} $1"; exit 1; }

# ── Verificar que estamos en la raíz del repo ─────────────────
[ -d "backend" ] || error "Ejecutá este script desde la raíz del repositorio."
[ -d "frontend" ] || error "No se encontró la carpeta frontend/."

# ── Verificar dependencias ────────────────────────────────────
info "Verificando dependencias..."
command -v java  >/dev/null 2>&1 || error "Java no encontrado. Instalá Java 21+."
command -v node  >/dev/null 2>&1 || error "Node.js no encontrado. Instalá Node 20+."
command -v npm   >/dev/null 2>&1 || error "npm no encontrado."
command -v mysql >/dev/null 2>&1 || error "MySQL client no encontrado. Instalá MySQL 8+."
[ -f "backend/mvnw" ]            || error "No se encontró backend/mvnw."
info "Dependencias OK."

# ── Credenciales MySQL ────────────────────────────────────────
echo ""
warning "Ingresá tus credenciales de MySQL:"
read -p "  Usuario MySQL [root]: " DB_USER
DB_USER="${DB_USER:-root}"
read -s -p "  Password MySQL: " DB_PASS
echo ""
DB_NAME="noteapp"

# ── Crear base de datos si no existe ─────────────────────────
info "Verificando base de datos '${DB_NAME}'..."
mysql -u "$DB_USER" -p"$DB_PASS" \
  -e "CREATE DATABASE IF NOT EXISTS ${DB_NAME} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" \
  2>/dev/null || error "No se pudo conectar a MySQL. Verificá usuario y password."
info "Base de datos lista."

# ── Generar application.properties ───────────────────────────
PROPS_DIR="backend/src/main/resources"
mkdir -p "$PROPS_DIR"
PROPS_FILE="${PROPS_DIR}/application.properties"

info "Generando application.properties..."
cat > "$PROPS_FILE" << EOF
spring.application.name=noteapp


spring.datasource.url=jdbc:mysql://localhost:3306/${DB_NAME}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql: true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
logging.level.org.springframework=DEBUG
logging.level.com.ap.sgri=DEBUG
spring.servlet.multipart.enabled=true
spring.docker.compose.enabled=false

# Puerto
server.port=8080

EOF
info "application.properties generado."

# ── Build y arranque del backend ──────────────────────────────
info "Compilando backend..."
cd backend
chmod +x mvnw
./mvnw clean package -DskipTests -q || error "Error al compilar el backend."

info "Iniciando backend en puerto 8080..."
./mvnw spring-boot:run -q &
BACKEND_PID=$!
cd ..

# Esperar a que el backend responda
info "Esperando que el backend arranque (máx. 90s)..."
COUNT=0
until curl -s http://localhost:8080/api/categories >/dev/null 2>&1; do
  sleep 3
  COUNT=$((COUNT + 3))
  if [ $COUNT -ge 90 ]; then
    kill $BACKEND_PID 2>/dev/null
    error "El backend no arrancó en 90 segundos. Revisá los logs."
  fi
done
info "Backend listo."

# ── Frontend ──────────────────────────────────────────────────
info "Instalando dependencias del frontend..."
cd frontend
npm install --silent || error "Error al instalar dependencias del frontend."

info "Iniciando frontend en puerto 5173..."
npm run dev &
FRONTEND_PID=$!
cd ..

# ── Listo ─────────────────────────────────────────────────────
echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}  NoteApp corriendo${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "  Frontend  →  ${YELLOW}http://localhost:5173${NC}"
echo -e "  Backend   →  ${YELLOW}http://localhost:8080${NC}"
echo ""
echo -e "  Admin por defecto:"
echo -e "    Email:    ${YELLOW}admin@noteapp.com${NC}"
echo -e "    Password: ${YELLOW}admin123${NC}"
echo ""
echo -e "  Presioná ${RED}Ctrl+C${NC} para detener todo."
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

trap "info 'Deteniendo servicios...'; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; info 'Hasta luego.'" EXIT INT TERM
wait