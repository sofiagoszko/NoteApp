FROM node:20-alpine AS build
WORKDIR /app

RUN chown node:node /app
USER node

COPY --chown=node:node package*.json ./
RUN npm ci --silent

COPY --chown=node:node . .

ARG VITE_BASE_URL=/api
ENV VITE_BASE_URL=$VITE_BASE_URL
RUN npm run build


FROM nginxinc/nginx-unprivileged:alpine
COPY --from=build --chown=101:101 /app/dist /usr/share/nginx/html

COPY --chown=101:101 nginx.conf /etc/nginx/conf.d/default.conf
USER 101

EXPOSE 5173
