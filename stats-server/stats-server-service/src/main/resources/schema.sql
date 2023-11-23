
DROP TABLE IF EXISTS statistics;

CREATE TABLE IF NOT EXISTS statistics (
                                          id INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
                                          app VARCHAR NOT NULL,
                                          uri VARCHAR NOT NULL,
                                          ip VARCHAR NOT NULL,
                                          timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL
);