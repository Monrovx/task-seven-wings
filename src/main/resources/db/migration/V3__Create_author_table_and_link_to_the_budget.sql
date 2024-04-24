CREATE TABLE Author(
    id SERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    date_of_creation TIMESTAMP NOT NULL DEFAULT now()
);

ALTER TABLE Budget
ADD COLUMN author_id INT REFERENCES Author(id);