FROM node:20-alpine AS build
WORKDIR /app

RUN chown node:node /app
USER node

COPY --chown=node:node package*.json ./
RUN npm install --silent

COPY --chown=node:node . .

ARG VITE_BASE_URL=/api
ENV VITE_BASE_URL=$VITE_BASE_URL
RUN npm run build


FROM nginxinc/nginx-unprivileged:alpine
COPY --from=build /app/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 5173