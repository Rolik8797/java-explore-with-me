CREATE TABLE IF NOT EXISTS endpoint_hit(
                    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    app     VARCHAR(50) NOT NULL,
                    uri     VARCHAR(50) NOT NULL,
                    user_ip VARCHAR(50) NOT NULL,
                    created TIMESTAMP   NOT NULL
    );