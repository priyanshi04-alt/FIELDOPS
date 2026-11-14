# Multi-stage Frontend Build
FROM node:20-alpine AS build
WORKDIR /app

# Cache dependency layer
COPY package*.json ./
RUN npm install

# Build production bundle
COPY . .
RUN npm run build

# Production Nginx Runtime
FROM nginx:alpine

# Copy compiled static assets
COPY --from=build /app/dist /usr/share/nginx/html

# Copy production reverse proxy configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Health check using lightweight wget
HEALTHCHECK --interval=15s --timeout=3s --retries=3 \
    CMD wget -qO- http://localhost/healthz || exit 1

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
