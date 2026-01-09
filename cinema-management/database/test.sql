psql -U postgres 

\c cinema_management;


CREATE TABLE IF NOT EXISTS test (
    id SERIAL PRIMARY KEY,
    ip VARCHAR(45) NOT NULL,
    port INTEGER NOT NULL
);

INSERT INTO test (ip, port) VALUES
    ('127.0.0.1', 8080),
    ('192.168.1.10', 5432);
SELECT * FROM test;
