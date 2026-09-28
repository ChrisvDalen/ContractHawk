FROM node:24-alpine AS build
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm ci --no-audit --no-fund
COPY frontend/ .
COPY contracts /contracts
RUN npm run build

FROM nginx:1.31-alpine
COPY docker/frontend-nginx.conf /etc/nginx/nginx.conf
# Run the workers as the unprivileged built-in "nginx" user; give it the
# cache/log dirs the master process normally writes to.
RUN chown -R nginx:nginx /var/cache/nginx /var/log/nginx /var/lib/nginx \
    && mkdir -p /tmp/nginx && chown nginx:nginx /tmp/nginx
USER nginx
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget -qO- http://127.0.0.1:8080/ >/dev/null 2>&1 || exit 1
