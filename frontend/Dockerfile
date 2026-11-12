# Build stage
FROM docker.io/library/node:22-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

ARG VITE_API_URL=http://localhost:8080
ARG VITE_AI_URL=http://localhost:8000
ARG VITE_AI_API_KEY=
ARG VITE_SUPPORT_EMAIL=support@your-domain.example
ENV VITE_API_URL=$VITE_API_URL \
    VITE_AI_URL=$VITE_AI_URL \
    VITE_AI_API_KEY=$VITE_AI_API_KEY \
    VITE_SUPPORT_EMAIL=$VITE_SUPPORT_EMAIL

RUN npm run build

# Runtime stage
FROM docker.io/library/nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 \
  CMD wget -qO- http://127.0.0.1/ >/dev/null || exit 1
