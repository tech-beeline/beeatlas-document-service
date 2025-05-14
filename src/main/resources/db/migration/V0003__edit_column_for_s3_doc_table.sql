-- Create documentation_type table in documents schema
CREATE TABLE documents.documentation_type (
                                              id INT PRIMARY KEY,
                                              name TEXT NOT NULL,
                                              target_entity_type TEXT NOT NULL,
                                              ttl INT NOT NULL,
                                              folder TEXT NOT NULL,
                                              doc_type TEXT NOT NULL
);

-- Add columns to s3_doc table in documents schema
ALTER TABLE documents.s3_doc
    ADD COLUMN target_entity_id INT NOT NULL,
ADD COLUMN documentation_type_id INT NOT NULL,
ADD CONSTRAINT fk_documentation_type
    FOREIGN KEY (documentation_type_id)
    REFERENCES documents.documentation_type(id);