CREATE SEQUENCE documents.s3_doc_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

CREATE TABLE documents.s3_doc (
                                  id integer PRIMARY KEY DEFAULT nextval('documents.s3_doc_id_seq'),
                                  doc_type TEXT,
                                  key TEXT,
                                  source_type TEXT,
                                  source_id integer,
                                  is_public BOOLEAN,
                                  deleted_date TIMESTAMP WITHOUT TIME ZONE,
                                  created_date TIMESTAMP WITHOUT TIME ZONE,
                                  last_modified_date TIMESTAMP WITHOUT TIME ZONE
);