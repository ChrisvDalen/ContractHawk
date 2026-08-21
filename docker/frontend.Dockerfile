FROM node:24-alpine AS build
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm ci --no-audit --no-fund
COPY frontend/ .
COPY contracts /contracts
RUN npm run build

FROM nginx:1.31-alpine
COPY --from=build /app/dist/contract-hawk/browser /usr/share/nginx/html
EXPOSE 80
