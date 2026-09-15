# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

NoteApp is a personal-notes web app: a Spring Boot 4 REST API (`backend/`, package `com.hirelens.noteapp`)
and a React 19 + Vite SPA (`frontend/`). Auth is JWT bearer tokens; each user only sees their own
notes/account (admins see everything). Most docs and commit messages are in Spanish.

Branch flow: feature branches → `dev` (integration) → `main` via PR. Jenkins deploys `dev` → the DEV
stack and `main` → the PROD stack; any other branch only runs the validation stages.

## Commands

Toolchain (see README for exact dev versions): JDK 21, Maven 3.9+ (wrapper provided), Node 22.13+ (the dev machine and Jenkins agent stay on 22.13.0 — check `engines` before bumping frontend deps)/npm 10+, MySQL 8 (only for non-test runs).

### Backend (`backend/`, run via the Maven wrapper)
- Run the API: `./mvnw spring-boot:run` (serves `http://localhost:8080`)
- Full test suite: `./mvnw test` — uses an in-memory H2 DB (`src/test/resources/application.properties`), no MySQL needed
- Single test class: `./mvnw test -Dtest=JwtServiceTest`
- Single test method: `./mvnw test -Dtest=NoteControllerTest#methodName`
- Package: `./mvnw clean package`
- Use `./mvnw clean compile` (not incremental `compile`) when checking for compile errors — stale
  classes in `target/` can mask a broken source tree.

### Frontend (`frontend/`)
- Dev server: `npm run dev` (`http://localhost:5173`)
- Build: `npm run build` (runs `tsc -b` then `vite build`)
- Lint: `npm run lint` (there is known pre-existing lint debt in `AuthContext.tsx` / `Notes.tsx`)
- Tests (watch): `npm run test` — Vitest + jsdom + Testing Library
- Tests (CI, one-shot): `npm run test:ci`
- Single test file: `npx vitest run src/pages/Login.test.tsx`
- All npm work happens in `frontend/`. The repo-root `package.json`/`package-lock.json` are a stray
  (three test deps, no scripts) — ignore them; don't `npm install` at the root.

### Docker
Each side has its own compose file (`backend/docker-compose.yaml` = `db` + `backend`,
`frontend/docker-compose.yml` = `frontend`), always run **together as one stack**: every host port, the
DB name, the JWT secret and the CORS origins come from an env file, and the same pair of files is
parameterized into a DEV and a PROD stack. Always pass all three of `--env-file`, `-p` and both `-f`s —
a missing `-p` silently targets a different project's containers/volume, and a missing `--env-file`
fails on `${APP_JWT_SECRET:?}` / `${MYSQL_ROOT_PASSWORD:?}`.
```
docker compose --env-file backend/.env.dev  -p noteapp-dev  -f backend/docker-compose.yaml -f frontend/docker-compose.yml up --build -d
docker compose --env-file backend/.env.prod -p noteapp-prod -f backend/docker-compose.yaml -f frontend/docker-compose.yml up --build -d
```
List the backend file first: the frontend service's build context is `../frontend`, resolved relative to
the **first** `-f`'s directory. `noteapp-network` and the `noteapp-db-data` volume are declared in both
files (not external) so Compose namespaces them per `-p` — DEV and PROD get separate networks and
separate MySQL data.

| Stack | project | frontend | backend | MySQL | DB |
|---|---|---|---|---|---|
| DEV  | `noteapp-dev`  | `:5173` | `:8080` | `:3307` | `noteapp_dev` |
| PROD | `noteapp-prod` | `:5174` | `:8082` | `:3308` | `noteapp_prod` |

The `db` healthcheck is `mysqladmin ping --protocol=TCP` (TCP, not the socket — the socket answers
before MySQL really accepts connections); `backend` waits on it via `depends_on: condition: service_healthy`.
In Docker the frontend is nginx serving the built SPA and proxying `/api/` → `backend:8080`, so browser
traffic is same-origin and CORS doesn't apply; nginx also sets `X-Real-IP` (what the rate limiter reads)
and a strict CSP.

`frontend/nginx.conf` details that are easy to break: nginx listens on `5173` inside the container (the
host port is mapped from `FRONTEND_HOST_PORT`). The upstream is a variable (`set $backend_upstream`) used
with Docker's DNS `resolver 127.0.0.11`, so nginx still starts when `backend` is down and finds the
backend again after that container is recreated. Don't inline it as a literal `proxy_pass http://backend:8080`.
Security headers are repeated in both `location /` and `location /assets/`, because nginx doesn't inherit
`add_header` into a location that sets its own. Add any new header to both. The CSP lives in `$csp`
(`connect-src 'self'`, fonts only from Google Fonts), so a new external script/font/image/API origin
must be allowed there or the browser blocks it silently.

Both Dockerfiles are multi-stage, run as non-root users, and keep dependencies out of the image layers.
The frontend installs in its own layer (`package*.json` → `npm ci`) before copying the source. The
backend has no separate dependency step: with the cache mount it would only add time. Dependency
downloads use BuildKit cache mounts (`RUN --mount=type=cache` on `/home/spring/.m2` and `/home/node/.npm`),
so builds need BuildKit (always used by `docker compose` v2). Those caches live in the host's
builder, and `docker builder prune` wipes them. The mount `uid`/`gid` must match the build user:
`spring` is pinned to 1001, `node` is 1000.

The backend build uses the `maven` image's `mvn` rather than `mvnw`, and compiles with
`-Dmaven.test.skip=true` because Jenkins already ran the tests. It copies `application.properties.example`
→ `application.properties`, so runtime config comes from the env file. The jar is unpacked with
`-Djarmode=tools extract --layers` and started via `JarLauncher`, so a code-only change rewrites just
the `application/` layer. The `.dockerignore` files exclude tests, docs and the Docker files, so editing
them doesn't bust the build cache.

### CI/CD (Jenkins)
Root `Jenkinsfile` is a declarative pipeline on a **Windows** agent with Git/Docker/Java/Node on PATH
(all steps are `bat`/`powershell`), deploying to the same host it builds on. It runs **validation on
every branch**, then deploy stages gated by `when { branch }`:

1. `Checkout`
2. `Backend Test` — `backend/ mvnw.cmd test`, publishes `backend/target/surefire-reports/*.xml` via `junit`
3. `Frontend Validation` — `npm ci` → `npm run test:ci` on every branch, then `npm run build` only on
   branches other than `dev`/`main` (there `Docker Build` compiles the frontend inside the image instead). Most of this stage's time is cold disk reads of
   the freshly installed `node_modules` (jsdom startup), not the tests; Vitest worker count doesn't change it.
   Vitest 4 gives each fork a hard-coded 60s to start, so on a new/cold workspace the forks can fail with
   `Timeout waiting for worker to respond` ("no tests"); the stage pre-imports `jsdom`/`vitest`/`@testing-library/react`
   in one `node` process first to warm the disk cache. Keep that step if you touch the stage.
4. `Configurar entorno Docker` (`dev`/`main` only) — writes `backend/.env.dev` or `backend/.env.prod`
   from three per-environment `Secret text` credentials (`noteapp-{dev,prod}-{jwt-secret,admin-password,mysql-root-password}`)
   plus branch-derived DB name/ports/CORS origin, so every later compose call resolves.
5. `Docker Build` → `Stop Previous Version` (`down`, **no `-v`**, so MySQL data survives) → `Deploy`
   (`up --no-build -d`) → `Health Check`, each duplicated as a `DEV` (branch `dev`) and `PROD` (branch `main`) variant.
6. `GitHub Release PROD` (`main` only, after the PROD health check) — via the GitHub REST API (`Secret text`
   credential `github-release-token`, fine-grained, Contents: write) creates a Release + tag on the deployed
   commit, bumping the patch of the latest Release (`v1.0.0` → `v1.0.1`) with auto-generated notes. Skips if the
   commit is already the latest Release. Minor/major bumps are done by hand in GitHub; the pipeline continues from there.

Health checks poll `docker inspect` for the `db` container's `healthy` state, then the backend's
`/actuator/health` and the frontend root, dumping `docker compose logs --tail=100` before throwing.
Changing ports, credential ids or the env-file layout means editing the Jenkinsfile's `Configurar entorno
Docker` script, the `.env.*.example` templates and the README table together.
See the README's "Jenkins" section for local-Jenkins setup (runs on `:8081` — `:8080`/`:8082` are the app).

## Configuration you must provide

- **Backend (local run)**: `backend/src/main/resources/application.properties` is gitignored — copy it
  from `application.properties.example`. Every value there is `${ENV_VAR:default}`, so the same file
  works locally and in Docker. Only `app.jwt.secret` (env `APP_JWT_SECRET`, min 32 chars) has no
  default and is required. Optional `APP_ADMIN_PASSWORD` seeds `admin@noteapp.com` as an `ADMIN` on
  first boot; if unset, no admin exists.
- **Backend (Docker)**: `backend/.env.dev` / `backend/.env.prod`, both gitignored — copy from
  `.env.dev.example` / `.env.prod.example` (`.env` / `.env.example` is the older single-stack variant).
  They carry `APP_JWT_SECRET`, `MYSQL_ROOT_PASSWORD` (both required, no default), `MYSQL_DATABASE`,
  `MYSQL_HOST_PORT`, `BACKEND_HOST_PORT`, `FRONTEND_HOST_PORT`, `APP_CORS_ALLOWED_ORIGINS`, `APP_ADMIN_PASSWORD`.
- **Frontend**: `.env` with `VITE_BASE_URL` (template: `frontend/.env.example`; e.g.
  `http://localhost:8080/api` locally). In Docker it's a build arg baked in at image build time
  (`frontend/Dockerfile`, default `/api` for the nginx proxy) — not a runtime variable, so changing it
  requires a rebuild.

## Backend architecture

- **Spring Boot 4.0.5 / Spring 7 / Security 7 / Jackson 3.** Several packages and starters differ from
  Boot 3 — see the `spring-boot-4-gotchas` memory. Key ones: web starter is `spring-boot-starter-webmvc`,
  the runtime `ObjectMapper` is `tools.jackson.databind.ObjectMapper`, error props are `spring.web.error.*`,
  test slices are split starters (`spring-boot-starter-webmvc-test`, `-data-jpa-test`, `-security-test`).
- **Auth flow**: `UserController` `/api/users/login` + `/register` are the only public endpoints; both
  return `AuthResponse` (`{ token, user }`) and `register` auto-logs-in. `JwtService` mints HS256 tokens
  (1h, no refresh) with claims `sub`=user id, `role`, `email`, `nickname`, `jti`. `SecurityConfig` is a
  stateless oauth2-resource-server: CSRF off, `role` claim mapped to `ROLE_*` authorities, `sub` as
  principal name. Also permitted without a token: all `OPTIONS` and `GET /actuator/health` (what the
  Jenkins health checks hit); everything else is `authenticated()`.
- **CORS**: `corsConfigurationSource` bean in `SecurityConfig`, origins from `app.cors.allowed-origins`
  (env `APP_CORS_ALLOWED_ORIGINS`, comma-separated, no trailing slash). Credentials off — auth is a
  Bearer header, not cookies — so only `Authorization`/`Content-Type` are allowed headers. Only matters
  for the local Vite dev server; the Docker stack is same-origin behind nginx.
- **Authorization**: method-level via `@EnableMethodSecurity`. Controllers use
  `@PreAuthorize("@authz.isSelfOrAdmin(#userId, authentication)")` (or `#id`) — the `authz` bean is
  `security/AuthorizationService`. The `#userId`/`#id` SpEL depends on `-parameters` bytecode, configured
  in `pom.xml`'s compiler plugin — don't remove it. Notes are additionally scoped at the query layer:
  `NoteService.getNoteForUser(noteId, userId)` filters by owner so a mismatched path can't leak a note.
- **Rate limiting**: `security/RateLimitFilter` (Bucket4j + Caffeine, per client IP via
  `security/ClientIpResolver` / `X-Real-IP`): 5 `/login` attempts per 15 min, 100 req/min otherwise.
  Wired in the security chain via `addFilterAfter(..., CorsFilter.class)`; the `FilterRegistrationBean`
  is disabled so it isn't also registered as a plain servlet filter. No per-account lockout (deferred).
- **Responses & errors**: success/error envelope is `responses/Response<T>` (`{ success, message, data }`),
  though several handlers still return bare entities/strings. `exceptions/GlobalExceptionHandler`
  (`@RestControllerAdvice`) maps exceptions to that envelope; 401/403 come from
  `RestAuthenticationEntryPoint` / `RestAccessDeniedHandler`.
- **Persistence**: JPA/Hibernate against MySQL (`ddl-auto=update`). Entities `models/User` and `models/Note`
  (`@ManyToOne` user, `@JsonIgnore`d). DTOs in `dto/`, hand-written mappers in `mappers/`.
- **Config properties**: `config/JwtProperties` (`app.jwt.*`), `config/RateLimitProperties` (`app.ratelimit.*`),
  bound via `@EnableConfigurationProperties` in `SecurityConfig`. `config/DataInitializer` seeds the admin.
- **Hardening in `application.properties`**: `spring.web.error.*` all off (no message/stacktrace/exception
  leakage in error bodies — tests asserting on error text must not rely on it) plus anti-DoS Tomcat caps
  (`server.tomcat.max-connections`/`threads.max`/`connection-timeout`, `server.max-http-request-header-size`).
  `spring.docker.compose.enabled=false` — the `spring-boot-docker-compose` dependency is on the classpath,
  so without this the app would try to start its own Compose stack on boot.
- **Tests** live under `src/test/.../{services,controllers,security,integration}` and run on H2
  (`src/test/resources/application.properties`, own JWT secret) — no MySQL/env needed. `services/*`
  are Mockito unit tests; `controllers/*` are `@SpringBootTest` + `MockMvc` + `SecurityMockMvcRequestPostProcessors.jwt()`
  (not `@WebMvcTest` — the JWT `SecurityConfig` pulls in the rate-limit filter and decoder and won't
  slice cleanly); `security/*` + `integration/*` are full `@SpringBootTest`. Cross-user note access:
  wrong path → 403 (`@PreAuthorize`), own path but someone else's note → 404 (`getNoteForUser` filters).
  The test properties raise the global rate limit to 10000/min so unrelated tests don't hit 429, but keep
  the login limit at 5/15min for `security/RateLimitTest` — don't "fix" a 429 by raising that one.

## Frontend architecture

- **React 19, Vite 8, React Router 7, Tailwind 4, TypeScript.** Entry `src/main.tsx` → `src/App.tsx`.
  UI uses `react-hot-toast` for notifications and `lucide-react` for icons.
- `context/AuthContext.tsx` holds `{ user, token, isAdmin }`, persisted to `localStorage` (`user`, `token`).
  `components/ProtectedRoute.tsx` gates `/notes`.
- API calls go through an `authFetch` wrapper (in `pages/Notes.tsx`) that adds `Authorization: Bearer <token>`
  and, on `401`, logs out + redirects to `/login`. Base URL from `import.meta.env.VITE_BASE_URL`.
- Routes: `/` (HomePage), `/login`, `/register`, `/notes` (protected); unknown paths redirect to `/`.
- `vite.config.ts` doubles as the Vitest config; `src/test/setup.ts` registers jest-dom and auto-cleanup.

## API endpoints

All require `Authorization: Bearer <token>` except `POST /api/users/{login,register}`.
- Users: `GET|PUT|DELETE /api/users/{id}`, `PATCH /api/users/{id}/password`
- Notes: `GET /api/notes/users/{userId}/active` (`?active=true|false` optional — omit for all notes),
  `GET|PUT|DELETE /api/notes/users/{userId}/notes/{noteId}`, `POST /api/notes/users/{userId}/notes`,
  `PATCH .../{noteId}/toggle-active`, `GET /api/notes/users` (ADMIN only)

## History note

Before JWT, the API identified users with an `X-User-Id` header. Code or tests on older branches that
use that header, `UserService.isAdmin` or `authenticateUser` are pre-JWT and need porting (merge
4dc4850 broke `NoteController` this way; fixed 2026-09-02). Today `UserService.authenticate` returns
`Optional<User>`.
