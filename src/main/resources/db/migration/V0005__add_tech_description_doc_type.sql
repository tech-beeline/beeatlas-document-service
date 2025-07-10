INSERT INTO documents.documentation_type (id, name, target_entity_type, ttl, folder, doc_type)
SELECT COALESCE(MAX(id), 0) + 1, 'Подробное описание технологии', 'tech', 9999, 'tech_description', 'md'
FROM documents.documentation_type;