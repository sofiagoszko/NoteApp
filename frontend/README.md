# NoteApp — Frontend

SPA en React + Vite para la interfaz de NoteApp (login, registro, y gestión de notas activas/archivadas).


## Estructura

```
frontend/
├── Dockerfile              ← build 
├── docker-compose.yml      ← contenedor del frontend
├── .dockerignore
├── nginx.conf              ← sirve la SPA, proxya /api/, caché + cabeceras de seguridad
├── .env.example            ← plantilla de variables de entorno
├── vite.config.ts          ← config de Vite y de Vitest (bloque `test`)
├── index.html
├── package.json
└── src/
    ├── main.tsx
    ├── App.tsx
    ├── index.css
    ├── assets/
    ├── components/         ← Navbar, GuestNavbar, NoteCard, NoteModal, ConfirmModal, ProtectedRoute
    │                          (+ ProtectedRoute.test.tsx)
    ├── context/            ← AuthContext (+ AuthContext.test.tsx)
    ├── pages/              ← HomePage, Login, Register, Notes
    │                          (+ Login.test.tsx, Notes.test.tsx)
    ├── test/setup.ts       ← setup de Vitest (jest-dom + cleanup)
    └── types/              ← Note, User
```

## Dependencias

| Paquete | Versión |
|---|---|
| React | 19.2.4 |
| React DOM | 19.2.4 |
| TypeScript | 6.0.2 |
| Vite | 8.0.4 |
| Tailwind CSS | 4.2.2 |
| React Router DOM | 7.14.1 |
| React Hot Toast | 2.6.0 |
| Lucide React | 1.8.0 |
| Node.js (runtime/build) | 20.x |


## Cómo levantarlo

### Opción A — Manual

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

La app queda disponible en `http://localhost:5173` (puerto por defecto de Vite).

### Opción B — Docker Compose

```bash
cd frontend
docker compose up --build -d
```

Esto compila el frontend y lo sirve con nginx en `http://localhost:5173`. `nginx.conf` proxy a todo lo que llega a `/api/` hacia `http://backend:8080/api/`, así que este contenedor necesita compartir red con el contenedor `backend`. Si el backend no está corriendo en la misma red `noteapp-network`, las peticiones a `/api/` van a fallar.

> El `Dockerfile` fija `VITE_BASE_URL=/api` en build, de modo que la SPA usa el proxy de nginx (sin CORS).
> En modo manual, `VITE_BASE_URL` sale de `.env` (por defecto `http://localhost:8080/api`).


## Pruebas

```bash
npm run test:ci     # una corrida (para CI)
npm run test        # modo watch
```

Vitest + Testing Library + jsdom (config en `vite.config.ts`, setup en `src/test/setup.ts`).
`fetch` se mockea en cada test — **no hace falta levantar el backend**.

| Archivo | Cubre |
|---|---|
| `context/AuthContext.test.tsx` | persistencia de `user` + `token` en `localStorage`, estado derivado `isAdmin`, logout |
| `components/ProtectedRoute.test.tsx` | redirección a `/login` cuando no hay sesión |
| `pages/Login.test.tsx` | submit del formulario, respuesta `{ token, user }`, toasts de error |
| `pages/Notes.test.tsx` | carga/creación/edición/archivado/borrado de notas, header `Authorization: Bearer` en cada request |



