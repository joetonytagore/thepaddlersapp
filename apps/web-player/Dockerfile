# Multi-stage Dockerfile for web-player
FROM node:18-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json* ./
COPY . .
RUN npm ci --silent
RUN npm run build

FROM nginx:stable-alpine AS runner
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

