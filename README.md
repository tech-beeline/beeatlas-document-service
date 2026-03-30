# beeatlas-document-service

Сервис управления документами на Spring Boot с хранением файлов в S3-совместимом хранилище (MinIO) и метаданными в PostgreSQL.

## Что делает сервис

- загружает и отдает документы;
- хранит бинарные файлы в S3 (MinIO);
- хранит метаданные документов в PostgreSQL;
- запускает связанные бизнес-процессы через Camunda;
- отдает служебные endpoint'ы Actuator и метрики Prometheus.

## Технологии

- Java 17
- Spring Boot 2.7.x
- Spring Web, Spring Data JPA
- PostgreSQL + Flyway
- MinIO (S3)
- Spring Actuator + Prometheus (Micrometer)
- Maven
- Docker / Docker Compose

## Структура окружения (docker-compose)

В `docker-compose.yml` поднимаются:

- `document-service` - приложение;
- `document-service-postgres` - PostgreSQL;
- `document-service-minio` - S3-совместимое хранилище MinIO;
- `document-service-minio-init` - init-контейнер для создания bucket.

## Быстрый старт

### 1) Запуск

```bash
docker compose up --build -d
```

После запуска:

- API: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui/`
- OpenAPI spec: `http://localhost:8081/v3/api-docs`
- Health: `http://localhost:8081/actuator/health`
- Prometheus metrics: `http://localhost:8081/actuator/prometheus`
- MinIO API: `http://localhost:9000`
- MinIO Console: `http://localhost:9001`
- PostgreSQL: `localhost:5433`

### 2) Остановка

```bash
docker compose down
```

С удалением томов:

```bash
docker compose down -v
```

## Переменные окружения

Ниже ключевые переменные из `docker-compose.yml` (с дефолтами):

### PostgreSQL

- `DOCUMENT_POSTGRES_DB=document_service`
- `DOCUMENT_POSTGRES_USER=postgres`
- `DOCUMENT_POSTGRES_PASSWORD=postgres`
- `DOCUMENT_POSTGRES_HOST=document-service-postgres`
- `DOCUMENT_SERVICE_POSTGRES_NODEPORT=5433`

### Приложение

- `DOCUMENT_SERVICE_PORT=8081`

### MinIO / S3

- `DOCUMENT_MINIO_ROOT_USER=minioadmin`
- `DOCUMENT_MINIO_ROOT_PASSWORD=minioadmin`
- `DOCUMENT_MINIO_API_PORT=9000`
- `DOCUMENT_MINIO_CONSOLE_PORT=9001`
- `DOCUMENT_MINIO_REGION=us-east-1`
- `DOCUMENT_MINIO_BUCKET_NAME=document-service-documents`
- `DOCUMENT_MINIO_ENDPOINT=http://document-service-minio:9000`

### Интеграции

- `INTEGRATION_CAPABILITY_SERVER_URL=http://capability-service`
- `INTEGRATION_AUTH_SERVER_URL=http://auth-service`
- `INTEGRATION_DOCUMENT_SERVER_URL=http://document-service`
- `INTEGRATION_PRODUCT_SERVER_URL=http://product-service`

Также в коде используются:

- `integration.camunda.server.url`
- `integration.pack-loader-server-url`
- `aws.s3.region`
- `aws.s3.access.key`
- `aws.s3.secret.key`
- `aws.s3.endpoint`
- `aws.s3.bucket.name`

## API (основные endpoint'ы)

Базовый префикс API: `/api/v1`.

Основные маршруты:

- `GET /api/v1/documents/{id}`
- `GET /api/v1/documents/import`
- `GET /api/v1/documents/export`
- `POST /api/v1/import/{entityType}`
- `POST /api/v1/documents/{path_name}/{doc_type}`
- `GET /api/v1/documents/versions/{documentationsTypeId}/{targetId}`
- `GET /api/v1/documents/{documentationTypeId}/{targetId}`
- `PATCH /api/v1/export/{doc_id}`
- `POST /api/v1/export/{entity_type}`
- `GET /api/v1/documentations/{entity-type}`
- `GET /` (welcome endpoint)

## Swagger / OpenAPI

В проекте подключен Springfox (OpenAPI 3). После запуска сервиса используйте:

- Swagger UI: `http://localhost:8081/swagger-ui/`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`

Если UI не открывается по первому адресу, проверьте также:

- `http://localhost:8081/swagger-ui/index.html`

## Как посмотреть файлы в S3 локально

Для локального окружения S3 реализован через MinIO.

### Вариант 1: через браузер (рекомендуется)

1. Откройте MinIO Console: `http://localhost:9001`
2. Войдите с учетными данными:
   - login: `minioadmin` (или `DOCUMENT_MINIO_ROOT_USER`)
   - password: `minioadmin` (или `DOCUMENT_MINIO_ROOT_PASSWORD`)
3. Откройте bucket: `document-service-documents` (или значение `DOCUMENT_MINIO_BUCKET_NAME`)
4. Внутри bucket можно просматривать, скачивать и удалять объекты.

### Вариант 2: проверить через S3 API в браузере

Можно открыть объект напрямую по URL:

- `http://localhost:9000/<bucket>/<object-key>`

Пример:

- `http://localhost:9000/document-service-documents/import/sql.xlsx`

Примечание: путь обязательно должен содержать имя bucket, иначе S3 вернет ошибку.

## База данных и миграции

- Flyway включен.
- Рабочая схема: `documents`.
- Миграции: `src/main/resources/db/migration`.
- Основные таблицы: `documents.s3_doc`, `documents.documentation_type`.

## Наблюдаемость

- Actuator endpoints: `health`, `info`, `metrics`, `prometheus`.
- Health endpoint используется в healthcheck контейнера.

## Полезные команды

Логи сервиса:

```bash
docker compose logs -f document-service
```

Перезапуск только приложения:

```bash
docker compose up -d --build document-service
```