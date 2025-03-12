ALTER TABLE documents.s3_doc
    ADD COLUMN IF NOT EXISTS entity_type TEXT,
    ADD COLUMN IF NOT EXISTS operation_type TEXT;