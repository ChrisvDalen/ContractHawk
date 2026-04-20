FROM node:20-alpine AS build
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm install --no-audit --no-fund
COPY frontend/ .
COPY contracts /contracts
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /app/dist/contract-hawk/browser /usr/share/nginx/html
EXPOSE 80
