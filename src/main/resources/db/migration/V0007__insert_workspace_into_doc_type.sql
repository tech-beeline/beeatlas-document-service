INSERT INTO documents.documentation_type (id, name, target_entity_type, ttl, folder, doc_type)
SELECT COALESCE(MAX(id), 0) + 1,
       'Описание архитектуры',
       'workspace',
       9999,
       'workspace',
       'dsl'
FROM documents.documentation_type;