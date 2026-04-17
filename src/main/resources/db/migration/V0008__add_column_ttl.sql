ALTER TABLE documents.s3_doc
    ADD COLUMN IF NOT EXISTS ttl integer;

UPDATE documents.s3_doc
SET ttl = 60
WHERE documentation_type_id IS NULL
  AND target_entity_id IS NULL;

UPDATE documents.s3_doc
SET ttl = 0
WHERE documentation_type_id IS NOT NULL
  AND target_entity_id IS NOT NULL;