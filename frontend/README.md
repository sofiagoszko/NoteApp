# NoteApp — Frontend

SPA en React + Vite para la interfaz de NoteApp (login, registro, y gestión de notas activas/archivadas).


## Estructura

```
frontend/
├── Dockerfile              ← build 
├── docker-compose.yml      ← contenedor del frontend
├── .dockerignore
├── nginx.conf              ← sirve la SPA y proxya /api/ hacia el backend
├── .env.example            ← plantilla de variables de entorno
├── vite.config.ts
├── index.html
├── package.json
└── src/
    ├── main.tsx
    ├── App.tsx
    ├── index.css
    ├── assets/
    ├── components/         ← Navbar, GuestNavbar, NoteCard, NoteModal, ConfirmModal, ProtectedRoute
    ├── context/            ← AuthContext
    ├── pages/              ← HomePage, Login, Register, Notes
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
docker compose up --build
```

Esto compila el frontend y lo sirve con nginx en `http://localhost:5173`. `nginx.conf` proxy a todo lo que llega a `/api/` hacia `http://backend:8080/api/`, así que este contenedor necesita compartir red con el contenedor `backend`. Si el backend no está corriendo en la misma red `noteapp-network`, las peticiones a `/api/` van a fallar.


