INSERT INTO documents.documentation_type (id, name, target_entity_type, ttl, folder, doc_type)
SELECT COALESCE(MAX(id), 0) + 1,
       'Архитектурное описание паттерна',
       'pattern-arch',
       9999,
       'pattern_arch_description',
       'dsl'
FROM documents.documentation_type;