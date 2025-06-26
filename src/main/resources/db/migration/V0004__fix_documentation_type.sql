INSERT INTO documents.documentation_type (id, name, target_entity_type, ttl, folder, doc_type)
SELECT COALESCE(MAX(id), 0) + 1,
       'Описание паттерна',
       'pattern',
       9999,
       'patterns',
       'md'
FROM documents.documentation_type; 