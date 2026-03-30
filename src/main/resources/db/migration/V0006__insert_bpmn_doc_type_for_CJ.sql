INSERT INTO documents.documentation_type (
    id,
    name,
    target_entity_type,
    ttl,
    folder,
    doc_type
)
SELECT
        COALESCE(MAX(id), 0) + 1,
        'CJ в нотации BPMN',
        'CJ',
        9999,
        'CJ_BPMN',
        'bpmn'
FROM documents.documentation_type
WHERE NOT EXISTS (
    SELECT 1 FROM documents.documentation_type
    WHERE name = 'CJ в нотации BPMN'
);
