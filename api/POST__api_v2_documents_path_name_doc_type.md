POST /api/v2/documents/{path_name}/{doc_type}
==============================================================

<h1>Общее описание</h1>

Альтернативный endpoint загрузки документа, который принимает **raw binary data** файла в теле запроса (без
`multipart/form-data`).
Имя файла передаётся в query-параметре `fileName`. Файл сохраняется в **MinIO (S3)**, метаданные — в PostgreSQL (таблица
`documents.s3_doc`).

<h1>Параметры запроса</h1>

**Method:** `POST`  
**Path:** `/api/v2/documents/{path_name}/{doc_type}`

<h2>Path-параметры</h2>

- `path_name` (String) — каталог (folder) для хранения документа
- `doc_type` (String) — тип документации (как правило, расширение файла)

<h2>Query-параметры</h2>

- `fileName` (String, **обязательный**) — оригинальное имя файла **с расширением** (например, `spec.md`)
- `isPublic` (Boolean, опциональный) — публичный доступ к документу (по умолчанию `false`)
- `targetId` (Integer, опциональный) — ID целевой сущности (используется для проверки зарегистрированного типа
  документации)

<h2>Headers</h2>

- `Content-Length` — **обязателен**, должен соответствовать размеру тела запроса
- `Content-Type` — `application/octet-stream` или MIME-тип файла (опционально; если не передан — будет использован
  `application/octet-stream`)
- `user-id` — опциональный (поведение авторизации и исключений аналогично v1 endpoint)

<h2>Тело запроса</h2>

- Raw binary data файла

<h1>Валидация</h1>

- `fileName` должен быть передан и содержать расширение
- `Content-Length` обязателен и должен совпадать с фактическим размером тела
- Если `targetId` передан:
    - `path_name` должен быть зарегистрирован в `documentation_type.folder`
    - расширение из `fileName` и `doc_type` должны совпадать с `documentation_type.doc_type`
- Если `targetId` не передан:
    - если `path_name` зарегистрирован в `documentation_type.folder` — ошибка (требуется `targetId`)
    - `doc_type` должен совпадать с расширением `fileName`

<h1>Авторизация</h1>

Применяются те же правила, что и для существующего v1 endpoint. URI `/api/v2/documents/**` добавлен в исключения
`HeaderInterceptor` наравне с v1.

<h1>Ответ</h1>

<h2>Успех</h2>

HTTP **200** с `DocIdDTO`:

```json
{ "docId": 12345 }
```

<h2>Ошибки</h2>

Такие же, как у v1 endpoint:

- **400** — `ValidationException`, а также ошибки отсутствующих обязательных заголовков
- **403** — `ForbiddenException`
- **404** — `NotFoundException`
- **503** — `S3Exception`

<h1>Интеграции</h1>

- MinIO (S3): `PutObjectArgs` (bucket `aws.s3.bucket.name`)
- PostgreSQL: `documents.s3_doc`, `documents.documentation_type`

<h1>Примеры запросов</h1>

<h2>curl (Windows PowerShell)</h2>

```bash
curl -X POST ^
  "http://localhost:8080/api/v2/documents/patterns/md?fileName=spec.md&isPublic=false&targetId=1" ^
  -H "Content-Type: application/octet-stream" ^
  -H "Content-Length: 12" ^
  --data-binary "@spec.md"
```

