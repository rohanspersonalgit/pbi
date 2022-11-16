create type phone as ENUM('Cell', 'Home');

create table numbers
(
    id           INT GENERATED ALWAYS AS IDENTITY NOT NULL,
    phone_number VARCHAR(10)                      NOT NULL,
    type         phone                            NOT NULL,
    occurrences  int                              NOT NULL
);