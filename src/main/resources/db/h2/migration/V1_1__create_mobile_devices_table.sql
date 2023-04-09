CREATE TABLE IF NOT EXISTS mobile_devices
(
    ID            VARCHAR DEFAULT CONCAT('md_', TO_CHAR(RANDOM_UUID())) PRIMARY KEY,
    model_code    VARCHAR NOT NULL,
    model         VARCHAR NOT NULL,
    booked        BOOLEAN NOT NULL,
    book_time     TIMESTAMP,
    booking_owner VARCHAR
);

CREATE INDEX IF NOT EXISTS mobile_devices_model_code_ndx ON mobile_devices (model_code);