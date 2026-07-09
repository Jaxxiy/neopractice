# 🚀 NeoPractice - Микросервисная система резервирования товаров

## 📋 Описание проекта

Проект представляет собой систему бронирования/резервирования товаров, состоящую из двух микросервисов, взаимодействующих через RabbitMQ. Каждый сервис имеет свою базу данных PostgreSQL.

## 🛠️ Технологический стек

| Компонент | Технология | Версия |
|-----------|------------|--------|
| Язык | Java | 17 |
| Фреймворк | Spring Boot | 3.1.5 |
| ORM | Hibernate (JPA) | 6.2.13 |
| База данных | PostgreSQL | 17 |
| Очереди | RabbitMQ | 3-management |
| Миграции | Liquibase | 4.23.1 |
| Контейнеризация | Docker | latest |
| Трассировка | OpenTelemetry + Jaeger | 1.31.0 |
| API Документация | OpenAPI 3 + Swagger UI | - |
| Маппинг | MapStruct | 1.5.5 |
| Сборка | Maven | - |

## 🚀 Запуск проекта

### Требования
- Docker & Docker Compose
- Java 17 (для локальной разработки)
- Maven 3.8+

### 1. Клонирование репозитория

```bash
git clone https://github.com/your-username/neopractice.git
cd neopractice

2. Сборка проекта
bash
# Сборка через Maven
mvn clean package

# Или через Docker
docker-compose build

3. Запуск всех сервисов
bash
docker-compose up -d

4. Проверка работы
Сервис	URL	Описание
Reservation Service	http://localhost:8081	Основной сервис
Product Service	http://localhost:8082	Сервис товаров
Swagger UI	http://localhost:8081/swagger-ui.html	Документация API
RabbitMQ UI	http://localhost:15672	Управление очередями (admin/admin)
pgAdmin	http://localhost:5050	Управление БД (admin@admin.com/admin)
Jaeger UI	http://localhost:16686	Трассировка запросов
5. Остановка
bash
docker-compose down

🔧 Конфигурация
Основные переменные окружения
Переменная	Значение по умолчанию	Описание
SPRING_DATASOURCE_URL	jdbc:postgresql://postgres-reservation:5432/reservation_db	URL БД
SPRING_RABBITMQ_HOST	rabbitmq	Хост RabbitMQ
OPENTELEMETRY_JAEGER_ENDPOINT	http://jaeger:14250	Endpoint Jaeger
Порты
Сервис	Порт (внешний)	Порт (внутренний)
Reservation Service	8081	8081
Product Service	8082	8082
PostgreSQL (Reservation)	5432	5432
PostgreSQL (Product)	5433	5432
RabbitMQ	5672	5672
RabbitMQ UI	15672	15672
pgAdmin	5050	80
Jaeger UI	16686	16686

🧪 Тестирование
Локальный запуск
bash
# Запуск только одного сервиса
docker-compose up reservation-service

# Запуск с пересборкой
docker-compose up -d --build
Отправка тестового запроса
bash
curl -X POST http://localhost:8081/api/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "idProduct": "PROD-001",
    "count": 2,
    "idUser": "USER-001"
  }'
Проверка БД
bash
# Подключение к Reservation DB
docker exec -it postgres-reservation psql -U reservation_user -d reservation_db

# Подключение к Product DB
docker exec -it postgres-product psql -U product_user -d product_db
🔧 Разработка
Сборка из IDEA
Откройте проект в IDEA

Запустите mvn clean compile

Запустите ReservationServiceApplication.java или ProductServiceApplication.java

Добавление нового API
Обновите OpenAPI спецификацию reservation-api.yaml

Запустите генерацию: mvn openapi-generator:generate

Реализуйте новый метод в контроллере

📊 Схема взаимодействия
text
1. POST /api/reservations
   ↓
2. Reservation Service создает заявку
   ↓
3. Отправляет сообщение в RabbitMQ (order.created)
   ↓
4. Product Service получает сообщение
   ↓
5. Проверяет наличие товара
   ↓
6. Обновляет количество
   ↓
7. Отправляет ответ (order.response)
   ↓
8. Reservation Service обновляет статус заявки
   ↓
9. Ответ пользователю
👨‍💻 Автор
Разработчик: jaxxiy
GitHub: Ваш профиль
Email: jaxx004@yandex.ru

📝 License
Этот проект распространяется под лицензией MIT.

text

---

## Как добавить README в проект:

### 1. Создайте файл в корне проекта

```bash
# В терминале
touch README.md

# Или создайте через IDEA/VS Code
