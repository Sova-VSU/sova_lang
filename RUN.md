# Запуск проекта Sova (локально)

Фронтенд ходит в **api-gateway** на порту **8080**. Gateway проксирует запросы в core-service (8081) и subscription-service (8082). Обе службы используют **MongoDB** на `localhost:27017`.

## 1. MongoDB

У вас уже может быть контейнер:

```powershell
docker run -d --name mongodb-sova -p 27017:27017 mongo:6
```

Проверка: `docker ps` — контейнер должен быть `Up`.

## 2. Переменные окружения (одинаковый JWT для всех сервисов)

В **каждом** терминале перед запуском Go/Java:

```powershell
$env:JWT_SECRET = "sova-dev-jwt-secret-key-min-32-chars"
```

**Важно:** один и тот же `JWT_SECRET` для core-service, api-gateway и subscription-service. Если gateway запущен со старым значением `secret`, вход будет работать, а профиль — нет (401 на `/users/me`).

## 3. Core-service (Java, порт 8081)

Нужны **Java 21** и Gradle (или `./gradlew` в папке `core-service`).

```powershell
cd core-service
$env:JWT_SECRET = "sova-dev-jwt-secret-key-min-32-chars"
$env:MONGO_URI = "mongodb://localhost:27017/core_service"
.\gradlew.bat bootRun
```

Дождитесь строки вроде `Started CoreServiceApplication`.

## 4. Subscription-service (Go, порт 8082)

```powershell
cd subscription-service
$env:JWT_SECRET = "sova-dev-jwt-secret-key-min-32-chars"
$env:MONGO_URI = "mongodb://localhost:27017"
go run .
```

## 5. API Gateway (Go, порт 8080)

```powershell
cd api-gateway
$env:JWT_SECRET = "sova-dev-jwt-secret-key-min-32-chars"
$env:CORE_SERVICE_URL = "http://localhost:8081"
$env:SUBSCRIPTION_SERVICE_URL = "http://localhost:8082"
go run .
```

## 6. Фронтенд

```powershell
cd sova-front
npm install
npm run dev
```

Откройте адрес из консоли Vite (обычно http://localhost:5173). Запросы к API идут через прокси Vite на gateway — CORS настраивать не нужно.

## Проверка регистрации

- Пароль на бэкенде: **минимум 8 символов**.
- Email должен быть валидным.
- Если email уже есть — сообщение `Email already taken`.

Прямой тест gateway (PowerShell):

```powershell
$body = '{"name":"Test","email":"test@example.com","password":"password123"}'
Invoke-WebRequest -Uri "http://localhost:8080/auth/register" -Method POST -ContentType "application/json" -Body $body
```

Ожидается статус **201** и JSON с `tokens` и `user`.

## Частые ошибки

| Симптом | Причина |
|--------|---------|
| «Сервер недоступен…» | Не запущен api-gateway (8080) или core-service (8081) |
| «Ошибка регистрации» без текста | Раньше: CORS или нет ответа от сервера — перезапустите gateway после обновления |
| `password: size must be between 8 and...` | Пароль короче 8 символов |
| `Internal server error` | MongoDB не запущен или неверный `MONGO_URI` |

## Порядок запуска

1. MongoDB  
2. core-service (8081)  
3. subscription-service (8082)  
4. api-gateway (8080)  
5. sova-front (`npm run dev`)
